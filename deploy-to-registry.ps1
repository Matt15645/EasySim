# Stock Management System - Registry Deployment Script
# 此腳本會構建所有服務的Docker映像，推送到Docker Hub，然後部署到Kubernetes

param(
    [string]$Registry = "matt15456",  # Docker Hub用戶名
    [string]$Tag = "latest",          # 映像標籤
    [switch]$SkipBuild,               # 跳過構建步驟
    [switch]$SkipPush,                # 跳過推送步驟
    [switch]$SkipDeploy               # 跳過部署步驟
)

$ErrorActionPreference = "Stop"

# 服務列表
$services = @(
    @{Name="data-provider"; Path="backend/data-provider"; HasMaven=$false},
    @{Name="api-gateway"; Path="backend/api_gateway"; HasMaven=$true},
    @{Name="auth-service"; Path="backend/auth_service"; HasMaven=$true},
    @{Name="account-service"; Path="backend/account_service"; HasMaven=$true},
    @{Name="backtest-service"; Path="backend/backtest_service"; HasMaven=$true},
    @{Name="subscribe-service"; Path="backend/subscribe_service"; HasMaven=$true}
)

Write-Host "=== Stock Management System Registry Deployment ===" -ForegroundColor Green
Write-Host "Registry: $Registry"
Write-Host "Tag: $Tag"
Write-Host ""

# 切換到主機Docker環境
Write-Host "切換到主機Docker環境..." -ForegroundColor Yellow
docker context use default

# 確保已登入Docker Hub
Write-Host "檢查Docker Hub登入狀態..." -ForegroundColor Yellow
$loginResult = docker info 2>&1 | Select-String "Username:"
if (-not $loginResult) {
    Write-Host "請先登入Docker Hub: docker login" -ForegroundColor Red
    exit 1
}

foreach ($service in $services) {
    $serviceName = $service.Name
    $servicePath = $service.Path
    $imageName = "$Registry/$serviceName"
    $fullImageName = "${imageName}:${Tag}"
    
    Write-Host "處理服務: $serviceName" -ForegroundColor Cyan
    
    # 切換到服務目錄
    Push-Location $servicePath
    
    try {
        # 構建Maven項目（如果需要）
        if ($service.HasMaven -and -not $SkipBuild) {
            Write-Host "  構建Maven項目..." -ForegroundColor Yellow
            .\mvnw clean package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                throw "Maven構建失敗"
            }
        }
        
        # 構建Docker映像
        if (-not $SkipBuild) {
            Write-Host "  構建Docker映像: $fullImageName" -ForegroundColor Yellow
            docker build -t $fullImageName .
            if ($LASTEXITCODE -ne 0) {
                throw "Docker構建失敗"
            }
        }
        
        # 推送映像到registry
        if (-not $SkipPush) {
            Write-Host "  推送映像到Docker Hub..." -ForegroundColor Yellow
            docker push $fullImageName
            if ($LASTEXITCODE -ne 0) {
                throw "Docker推送失敗"
            }
        }
        
        # 部署到Kubernetes
        if (-not $SkipDeploy) {
            Write-Host "  部署到Kubernetes..." -ForegroundColor Yellow
            kubectl apply -f k8s/
            if ($LASTEXITCODE -ne 0) {
                throw "Kubernetes部署失敗"
            }
            
            # 重啟Pod以拉取新映像
            kubectl delete pod -l app=$serviceName -n stock-management 2>$null
        }
        
        Write-Host "  ✓ $serviceName 處理完成" -ForegroundColor Green
        
    } catch {
        Write-Host "  ✗ $serviceName 處理失敗: $_" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    
    Pop-Location
}

if (-not $SkipDeploy) {
    Write-Host ""
    Write-Host "等待所有服務就緒..." -ForegroundColor Yellow
    
    foreach ($service in $services) {
        $serviceName = $service.Name
        Write-Host "等待 $serviceName..." -ForegroundColor Yellow
        kubectl wait --for=condition=ready pod -l app=$serviceName -n stock-management --timeout=120s
    }
    
    Write-Host ""
    Write-Host "檢查所有服務狀態:" -ForegroundColor Yellow
    kubectl get pods -n stock-management
    
    Write-Host ""
    Write-Host "服務地址:" -ForegroundColor Yellow
    kubectl get services -n stock-management
}

Write-Host ""
Write-Host "=== 部署完成 ===" -ForegroundColor Green
Write-Host "所有服務現在會自動從Docker Hub拉取最新映像"
Write-Host "要更新服務，只需運行此腳本即可"
