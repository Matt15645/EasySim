import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  MenuItem,
  Grid,
  Typography,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';

const TradeActionForm = ({ tradeActions, setTradeActions, symbols }) => {
  const [newTrade, setNewTrade] = useState({
    date: '',
    symbol: '',
    action: 'BUY',
    shares: ''
  });

  const handleAddTrade = () => {
    if (newTrade.date && newTrade.symbol && newTrade.shares) {
      setTradeActions([...tradeActions, {
        ...newTrade,
        shares: parseInt(newTrade.shares)
      }]);
      setNewTrade({
        date: '',
        symbol: '',
        action: 'BUY',
        shares: ''
      });
    }
  };

  const handleDeleteTrade = (index) => {
    const updatedTrades = tradeActions.filter((_, i) => i !== index);
    setTradeActions(updatedTrades);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        交易策略設定
      </Typography>
      
      {/* 新增交易表單 */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="subtitle1" gutterBottom>
          新增交易動作
        </Typography>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={3}>
            <TextField
              fullWidth
              type="date"
              label="交易日期"
              value={newTrade.date}
              onChange={(e) => setNewTrade({ ...newTrade, date: e.target.value })}
              InputLabelProps={{ shrink: true }}
              size="small"
            />
          </Grid>
          <Grid item xs={12} sm={2}>
            <TextField
              fullWidth
              select
              label="股票"
              value={newTrade.symbol}
              onChange={(e) => setNewTrade({ ...newTrade, symbol: e.target.value })}
              size="small"
            >
              {symbols.map((symbol) => (
                <MenuItem key={symbol} value={symbol}>
                  {symbol}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={2}>
            <TextField
              fullWidth
              select
              label="動作"
              value={newTrade.action}
              onChange={(e) => setNewTrade({ ...newTrade, action: e.target.value })}
              size="small"
            >
              <MenuItem value="BUY">買入</MenuItem>
              <MenuItem value="SELL">賣出</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={2}>
            <TextField
              fullWidth
              type="number"
              label="股數"
              value={newTrade.shares}
              onChange={(e) => setNewTrade({ ...newTrade, shares: e.target.value })}
              size="small"
              inputProps={{ min: 1 }}
            />
          </Grid>
          <Grid item xs={12} sm={1}>
            <Button
              variant="contained"
              onClick={handleAddTrade}
              startIcon={<AddIcon />}
              size="small"
            >
              新增
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* 交易清單 */}
      {tradeActions.length > 0 && (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>日期</TableCell>
                <TableCell>股票</TableCell>
                <TableCell>動作</TableCell>
                <TableCell align="right">股數</TableCell>
                <TableCell align="center">操作</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tradeActions
                .sort((a, b) => a.date.localeCompare(b.date))
                .map((trade, index) => (
                <TableRow key={index}>
                  <TableCell>{trade.date}</TableCell>
                  <TableCell>{trade.symbol}</TableCell>
                  <TableCell>
                    <Box
                      component="span"
                      sx={{
                        color: trade.action === 'BUY' ? 'success.main' : 'error.main',
                        fontWeight: 'bold'
                      }}
                    >
                      {trade.action === 'BUY' ? '買入' : '賣出'}
                    </Box>
                  </TableCell>
                  <TableCell align="right">{trade.shares.toLocaleString()}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleDeleteTrade(index)}
                      color="error"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};

export default TradeActionForm;