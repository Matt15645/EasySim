import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, AppBar, Toolbar, Typography, Button, Container, Grid, Card, CardContent } from '@mui/material';
import authService from '../services/authService';

export default function Dashboard() {
  const navigate = useNavigate();
  const user = authService.getCurrentUser();

  if (!user) {
    // 如果用戶未登入，重定向到登入頁面
    navigate('/login');
    return null;
  }

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            股票管理系統
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            登出
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          儀表板
        </Typography>
        <Typography paragraph>
          歡迎回來，{user.username || '用戶'}！這裡是您的個人儀表板。
        </Typography>
        <Typography paragraph>
          您可以在此管理您的股票投資組合、查看市場動態並進行回測。
        </Typography>

        {/* ====== 這裡是按鈕區塊，您可以調整 Grid/Card/Button 的屬性 ====== */}
        <Grid
          container
          spacing={4} // 調整卡片間距
          justifyContent="center"
          alignItems="stretch"
          sx={{
            mt: 8, // 調整與上方的距離
            mb: 4, // 調整與下方的距離
            minHeight: '40vh', // 調整區塊高度
          }}
        >
          {/* ====== Account Service 卡片 ====== */}
          <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex' }}>
            <Card
              elevation={12} // 調整浮空陰影深度（1~24，數字越大陰影越明顯）
              sx={{
                minWidth: 220, // 卡片最小寬度
                maxWidth: 320, // 卡片最大寬度
                mx: 'auto',    // 水平置中
                borderRadius: 4, // 圓角
                flex: 1,         // 讓卡片自動填滿高度
                display: 'flex',
                flexDirection: 'column',
              }}
            >
              <CardContent
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'stretch',
                  flex: 1,
                  height: '100%',
                }}
              >
                <Typography variant="h6" gutterBottom align="center">
                  Account Service  <br />
                  查詢帳戶餘額、交易紀錄、股票資訊
                </Typography>
                <Box sx={{ flexGrow: 1 }} />
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  size="large" // 按鈕大小：small, medium, large
                  sx={{
                    boxShadow: 6, // 按鈕本身的浮空感
                    borderRadius: 3, // 按鈕圓角
                    py: 2, // 垂直內距
                    fontWeight: 'bold', // 字體粗細
                    minHeight: 56, // 按鈕最小高度（可調整）
                  }}
                  onClick={() => navigate('/account')}
                >
                  前往 Account Service
                </Button>
              </CardContent>
            </Card>
          </Grid>
          {/* ====== Backtest Service 卡片 ====== */}
          <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex' }}>
            <Card elevation={12} sx={{ minWidth: 220, maxWidth: 320, mx: 'auto', borderRadius: 4, flex: 1, display: 'flex', flexDirection: 'column' }}>
              <CardContent
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'stretch',
                  flex: 1,
                  height: '100%',
                }}
              >
                <Typography variant="h6" gutterBottom align="center">
                  Backtest Service  <br />
                  回測股票策略、分析歷史資料
                </Typography>
                <Box sx={{ flexGrow: 1 }} />
                <Button
                  variant="contained"
                  color="secondary"
                  fullWidth
                  size="large"
                  sx={{ boxShadow: 6, borderRadius: 3, py: 2, fontWeight: 'bold', minHeight: 56 }}
                  onClick={() => navigate('/backtest')}
                >
                  前往 Backtest Service
                </Button>
              </CardContent>
            </Card>
          </Grid>
          {/* ====== Subscribe Service 卡片 ====== */}
          <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex' }}>
            <Card elevation={12} sx={{ minWidth: 220, maxWidth: 320, mx: 'auto', borderRadius: 4, flex: 1, display: 'flex', flexDirection: 'column' }}>
              <CardContent
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'stretch',
                  flex: 1,
                  height: '100%',
                }}
              >
                <Typography variant="h6" gutterBottom align="center">
                  Subscribe Service  <br />
                  訂閱股票資訊、接收即時通知
                </Typography>
                <Box sx={{ flexGrow: 1 }} />
                <Button
                  variant="contained"
                  color="success"
                  fullWidth
                  size="large"
                  sx={{ boxShadow: 6, borderRadius: 3, py: 2, fontWeight: 'bold', minHeight: 56 }}
                  onClick={() => navigate('/subscribe')}
                >
                  前往 Subscribe Service
                </Button>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
        {/* ====== 按鈕區塊結束 ====== */}
      </Container>
    </Box>
  );
}