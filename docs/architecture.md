stock-management-system/
├── .github/                   # For GitHub Actions CI/CD workflows (or .gitlab-ci/ for GitLab CI/CD)
│   └── workflows/
│       ├── frontend-ci-cd.yaml        # CI/CD for the React frontend
│       ├── user-service-ci-cd.yaml    # CI/CD for the User Service
│       ├── account-service-ci-cd.yaml # CI/CD for the Account Service
│       ├── subscribe-service-ci-cd.yaml # CI/CD for the Subscribe Service
│       ├── backtest-service-ci-cd.yaml # CI/CD for the Backtest Service
│       ├── api-gateway-ci-cd.yaml     # CI/CD for the API Gateway
│       └── k8s-infra-ci-cd.yaml       # CI/CD for shared K8s configurations (optional, but good for changes to k8s/ itself)
│
├── frontend/                  # React Frontend Application
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   └── App.js
│   ├── package.json
│   ├── Dockerfile             # Dockerfile for building the frontend image
│   └── k8s/                   # Kubernetes configurations for the frontend
│       ├── frontend-deployment.yaml
│       ├── frontend-service.yaml
│       └── frontend-ingress.yaml  # If frontend is served directly via Ingress
│
├── backend/                   # Backend Microservices
│   ├── user-service/          # User Management Service
│   │   ├── src/               # Core logic files (e.g., Java .java, Python .py)
│   │   ├── pom.xml (or build.gradle, requirements.txt, package.json)
│   │   ├── Dockerfile         # Dockerfile for User Service
│   │   └── k8s/               # K8s configs specific to User Service
│   │       ├── user-service-deployment.yaml
│   │       ├── user-service-service.yaml
│   │       └── user-service-configmap.yaml # For non-sensitive configs (e.g., DB connection details)
│   │       └── user-service-secret.yaml    # For sensitive configs (e.g., DB password)
│   │
│   ├── account-service/       # Account Information Service
│   │   ├── src/
│   │   ├── ...
│   │   ├── Dockerfile
│   │   └── k8s/
│   │       ├── account-service-deployment.yaml
│   │       └── account-service-service.yaml
│   │       └── ...
│   │
│   ├── subscribe-service/     # Subscription Management Service
│   │   ├── src/
│   │   ├── ...
│   │   ├── Dockerfile
│   │   └── k8s/
│   │       ├── subscribe-service-deployment.yaml
│   │       └── subscribe-service-service.yaml
│   │       └── ...
│   │
│   ├── backtest-service/      # Backtesting and Simulation Service
│   │   ├── src/
│   │   ├── ...
│   │   ├── Dockerfile
│   │   └── k8s/
│   │       ├── backtest-service-deployment.yaml
│   │       └── backtest-service-service.yaml
│   │       └── ...
│   │
│   └── api-gateway/           # Central API Gateway (e.g., Spring Cloud Gateway, Kong)
│       ├── src/
│       ├── ...
│       ├── Dockerfile
│       └── k8s/               # K8s configs specific to API Gateway
│           ├── api-gateway-deployment.yaml
│           ├── api-gateway-service.yaml
│           └── api-gateway-ingress.yaml # External entry point for backend APIs
│
├── databases/                 # Docker Compose files for local database setup (outside K8s)
│   └── docker-compose.db.yaml  # Defines all local databases (e.g., MySQL for each service)
│
├── k8s/                       # Shared/Common Kubernetes Configurations
│   ├── cluster-wide-rbac.yaml     # Roles, RoleBindings for cluster-wide access
│   ├── common-configmap.yaml      # General configurations used by multiple services
│   ├── ingress-controller-deployment.yaml # Deployment for Nginx/Traefik Ingress Controller (if not using Minikube addon)
│   └── namespaces.yaml            # Definitions for Kubernetes Namespaces
│
├── docker/                    # Optional: Common Docker assets or shared Dockerfiles
│   └── base-images/               # If you use custom base images
│
├── docs/                      # Project documentation (architecture, API specs, setup guides)
│   ├── architecture.md
│   ├── getting-started.md
│   └── api-specifications/
│
├── .gitignore
├── .env.example               # Example .env file for local development
└── README.md                  # Main project README, providing an overview of the entire system