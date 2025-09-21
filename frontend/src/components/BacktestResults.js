import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  AccountBalance,
  ShowChart
} from '@mui/icons-material';

const BacktestResults = ({ result, portfolioHistory }) => {
  if (!result) return null;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('zh-TW', {
      style: 'currency',
      currency: 'TWD',
      minimumFractionDigits: 0
    }).format(value);
  };

  const formatPercentage = (value) => {
    return `${parseFloat(value).toFixed(2)}%`;
  };

  const getReturnColor = (value) => {
    return parseFloat(value) >= 0 ? 'success.main' : 'error.main';
  };

  const getLatestPortfolio = () => {
    if (!portfolioHistory || portfolioHistory.length === 0) return null;
    return portfolioHistory[portfolioHistory.length - 1];
  };

  const latestPortfolio = getLatestPortfolio();

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        回測結果
      </Typography>

      {/* 績效指標卡片 */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <AccountBalance color="primary" sx={{ mr: 1 }} />
                <Box>
                  <Typography color="textSecondary" variant="body2">
                    起始資金
                  </Typography>
                  <Typography variant="h6">
                    {formatCurrency(result.initialCapital)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <ShowChart color="primary" sx={{ mr: 1 }} />
                <Box>
                  <Typography color="textSecondary" variant="body2">
                    最終價值
                  </Typography>
                  <Typography variant="h6">
                    {formatCurrency(result.finalValue)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                {parseFloat(result.totalReturn) >= 0 ? 
                  <TrendingUp color="success" sx={{ mr: 1 }} /> : 
                  <TrendingDown color="error" sx={{ mr: 1 }} />
                }
                <Box>
                  <Typography color="textSecondary" variant="body2">
                    總報酬
                  </Typography>
                  <Typography 
                    variant="h6" 
                    sx={{ color: getReturnColor(result.totalReturn) }}
                  >
                    {formatCurrency(result.totalReturn)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                {parseFloat(result.returnRate) >= 0 ? 
                  <TrendingUp color="success" sx={{ mr: 1 }} /> : 
                  <TrendingDown color="error" sx={{ mr: 1 }} />
                }
                <Box>
                  <Typography color="textSecondary" variant="body2">
                    報酬率
                  </Typography>
                  <Typography 
                    variant="h6" 
                    sx={{ color: getReturnColor(result.returnRate) }}
                  >
                    {formatPercentage(result.returnRate)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 風險指標 */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" variant="body2">
                年化 Sharpe Ratio
              </Typography>
              <Typography variant="h6">
                {parseFloat(result.annualizedSharpeRatio).toFixed(4)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" variant="body2">
                最大回撤
              </Typography>
              <Typography variant="h6" sx={{ color: 'error.main' }}>
                {formatPercentage(result.maxDrawdown)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" variant="body2">
                交易天數
              </Typography>
              <Typography variant="h6">
                {result.tradingDays} 天
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 最終投資組合狀況 */}
      {latestPortfolio && (
        <Paper sx={{ p: 2, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            最終投資組合
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="textSecondary">
                現金餘額
              </Typography>
              <Typography variant="h6">
                {formatCurrency(latestPortfolio.cash)}
              </Typography>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="textSecondary">
                持股價值
              </Typography>
              <Typography variant="h6">
                {formatCurrency(latestPortfolio.totalValue - latestPortfolio.cash)}
              </Typography>
            </Grid>
          </Grid>
          
          {Object.keys(latestPortfolio.holdings).filter(symbol => latestPortfolio.holdings[symbol] > 0).length > 0 && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="textSecondary" gutterBottom>
                持股明細
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {Object.entries(latestPortfolio.holdings)
                  .filter(([symbol, shares]) => shares > 0)
                  .map(([symbol, shares]) => (
                    <Chip
                      key={symbol}
                      label={`${symbol}: ${shares.toLocaleString()}股`}
                      variant="outlined"
                      size="small"
                    />
                  ))}
              </Box>
            </Box>
          )}
        </Paper>
      )}
    </Box>
  );
};

export default BacktestResults;