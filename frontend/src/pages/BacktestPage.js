import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  TextField,
  Button,
  Paper,
  Grid,
  Chip,
  Alert,
  CircularProgress,
  Divider,
  InputAdornment
} from '@mui/material';
import { AttachMoney } from '@mui/icons-material';
import backtestService from '../services/backtestService';
import TradeActionForm from '../components/TradeActionForm';
import BacktestResults from '../components/BacktestResults';
import PortfolioChart from '../components/PortfolioChart';

const BacktestPage = () => {
  // 基本設定
  const [symbols, setSymbols] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [initialCapital, setInitialCapital] = useState('1000000');
  
  // 交易策略
  const [tradeActions, setTradeActions] = useState([]);
  
  // 回測結果
  const [backtestResult, setBacktestResult] = useState(null);
  const [portfolioHistory, setPortfolioHistory] = useState([]);
  const [stockPrices, setStockPrices] = useState([]);
  
  // UI 狀態
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      // 處理股票代號輸入
      const symbolList = symbols.split(',').map(s => s.trim()).filter(s => s);
      
      if (symbolList.length === 0) {
        setError('請輸入至少一個股票代號');
        setLoading(false);
        return;
      }

      if (!startDate || !endDate) {
        setError('請選擇開始日期和結束日期');
        setLoading(false);
        return;
      }

      if (!initialCapital || parseFloat(initialCapital) <= 0) {
        setError('請輸入有效的起始資金');
        setLoading(false);
        return;
      }

      // 建立回測請求
      const backtestRequest = {
        symbols: symbolList,
        startDate,
        endDate,
        initialCapital: parseFloat(initialCapital),
        tradeActions: tradeActions
      };

      console.log('發送回測請求:', backtestRequest);

      const response = await backtestService.performBacktest(backtestRequest);

      // 檢查 message 欄位來判斷是否成功
      if (response.message && response.message.includes('成功')) {
        setBacktestResult(response);
        setPortfolioHistory(response.portfolioHistory || []);
        setSuccess('回測執行完成');
        
        // 如果需要顯示股價資料，可以另外取得
        // setStockPrices(response.stockPrices || []);
      } else {
        setError('回測執行失敗：' + (response.message || '未知錯誤'));
      }

    } catch (err) {
      console.error('回測錯誤:', err);
      setError('執行回測時發生錯誤: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleSymbolChipDelete = (symbolToDelete) => {
    const symbolList = symbols.split(',').map(s => s.trim()).filter(s => s);
    const updatedSymbols = symbolList.filter(symbol => symbol !== symbolToDelete);
    setSymbols(updatedSymbols.join(', '));
  };

  const getSymbolList = () => {
    return symbols.split(',').map(s => s.trim()).filter(s => s);
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        股票回測系統
      </Typography>

      {/* 基本設定 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          基本設定
        </Typography>
        
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid size={12}>
              <TextField
                fullWidth
                label="股票代號 (用逗號分隔，例如: 2330, 0050)"
                value={symbols}
                onChange={(e) => setSymbols(e.target.value)}
                placeholder="2330, 0050"
                variant="outlined"
              />
              <Box sx={{ mt: 1 }}>
                {getSymbolList().map((symbol, index) => (
                  <Chip
                    key={index}
                    label={symbol}
                    onDelete={() => handleSymbolChipDelete(symbol)}
                    sx={{ mr: 1, mb: 1 }}
                    color="primary"
                    variant="outlined"
                  />
                ))}
              </Box>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                type="date"
                label="開始日期"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
                variant="outlined"
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                type="date"
                label="結束日期"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
                variant="outlined"
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                type="number"
                label="起始資金"
                value={initialCapital}
                onChange={(e) => setInitialCapital(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <AttachMoney />
                    </InputAdornment>
                  ),
                }}
                inputProps={{ min: 1000, step: 1000 }}
                variant="outlined"
              />
            </Grid>
          </Grid>

          <Divider sx={{ my: 3 }} />

          {/* 交易策略設定 */}
          {getSymbolList().length > 0 && (
            <TradeActionForm 
              tradeActions={tradeActions}
              setTradeActions={setTradeActions}
              symbols={getSymbolList()}
            />
          )}

          <Box sx={{ mt: 3 }}>
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} /> : null}
            >
              {loading ? '執行回測中...' : '執行回測'}
            </Button>
          </Box>
        </form>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mt: 2 }}>
            {success}
          </Alert>
        )}
      </Paper>

      {/* 回測結果 */}
      {backtestResult && (
        <Paper sx={{ p: 3, mb: 3 }}>
          <BacktestResults 
            result={backtestResult}
            portfolioHistory={portfolioHistory}
          />
        </Paper>
      )}

      {/* 歷史走勢圖 */}
      {portfolioHistory.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <PortfolioChart 
            portfolioHistory={portfolioHistory}
            stockPrices={stockPrices}
          />
        </Paper>
      )}
    </Container>
  );
};

export default BacktestPage;