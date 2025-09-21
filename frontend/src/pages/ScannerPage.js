import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  TextField,
  Button,
  Paper,
  Grid,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Switch,
  FormControlLabel,
  Alert,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import scannerService from '../services/scannerService';

const ScannerPage = () => {
  // 表單狀態
  const [scannerType, setScannerType] = useState('ChangePercentRank');
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [count, setCount] = useState(50);
  const [ascending, setAscending] = useState(false);
  
  // 結果狀態
  const [scannerData, setScannerData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 掃描器類型選項
  const scannerTypes = scannerService.getScannerTypes();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      // 格式化日期
      const formattedDate = selectedDate.toISOString().split('T')[0];
      
      // 驗證輸入
      if (count < 1 || count > 200) {
        setError('回傳數量必須在 1-200 之間');
        setLoading(false);
        return;
      }

      // 建立請求
      const request = {
        scanner_type: scannerType,  // ✅ 改為下劃線
        date: formattedDate,
        count: parseInt(count),
        ascending
      };

      console.log('發送掃描器請求:', request);

      const response = await scannerService.getScannerData(request);
      
      setScannerData(response);
      setSuccess(`成功取得 ${response.count} 筆${response.scanner_type}資料`);

    } catch (err) {
      console.error('掃描器請求錯誤:', err);
      setError('取得掃描器資料失敗: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const formatNumber = (value) => {
    return parseFloat(value).toLocaleString('zh-TW', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  const formatPercent = (value) => {
    const num = parseFloat(value);
    const color = num >= 0 ? 'green' : 'red';
    const sign = num >= 0 ? '+' : '';
    return (
      <span style={{ color }}>
        {sign}{num.toFixed(2)}%
      </span>
    );
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          股票掃描器
        </Typography>

        {/* 查詢表單 */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            掃描條件設定
          </Typography>
          
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <FormControl fullWidth>
                  <InputLabel>掃描類型</InputLabel>
                  <Select
                    value={scannerType}
                    label="掃描類型"
                    onChange={(e) => setScannerType(e.target.value)}
                  >
                    {scannerTypes.map((type) => (
                      <MenuItem key={type.value} value={type.value}>
                        {type.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <DatePicker
                  label="查詢日期"
                  value={selectedDate}
                  onChange={(newValue) => setSelectedDate(newValue)}
                  renderInput={(params) => <TextField {...params} fullWidth />}
                  maxDate={new Date()}
                />
              </Grid>

              <Grid item xs={12} sm={6} md={2}>
                <TextField
                  fullWidth
                  label="回傳數量"
                  type="number"
                  value={count}
                  onChange={(e) => setCount(e.target.value)}
                  inputProps={{ min: 1, max: 200 }}
                />
              </Grid>

              <Grid item xs={12} sm={6} md={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={ascending}
                      onChange={(e) => setAscending(e.target.checked)}
                    />
                  }
                  label="升序排列"
                />
              </Grid>

              <Grid item xs={12} md={2}>
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={loading}
                  sx={{ height: '56px' }}
                >
                  {loading ? <CircularProgress size={24} /> : '開始掃描'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </Paper>

        {/* 錯誤和成功訊息 */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        {/* 掃描結果 */}
        {scannerData && scannerData.data && (
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              掃描結果
            </Typography>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                掃描類型: {scannerTypes.find(t => t.value === scannerData.scanner_type)?.label} | 
                查詢日期: {scannerData.date} | 
                資料筆數: {scannerData.count}
              </Typography>
            </Box>

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>排名</TableCell>
                    <TableCell>股票代碼</TableCell>
                    <TableCell>股票名稱</TableCell>
                    <TableCell align="right">收盤價</TableCell>
                    <TableCell align="right">漲跌幅</TableCell>
                    <TableCell align="right">漲跌價</TableCell>
                    <TableCell align="right">成交量</TableCell>
                    <TableCell align="right">成交金額</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {scannerData.data.map((item, index) => (
                    <TableRow key={item.code || index}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell>{item.code}</TableCell>
                      <TableCell>{item.name || '-'}</TableCell>
                      <TableCell align="right">
                        {item.close_price ? formatNumber(item.close_price) : '-'}
                      </TableCell>
                      <TableCell align="right">
                        {item.change_percent !== undefined ? formatPercent(item.change_percent) : '-'}
                      </TableCell>
                      <TableCell align="right">
                        {item.change_price ? formatNumber(item.change_price) : '-'}
                      </TableCell>
                      <TableCell align="right">
                        {item.volume ? parseInt(item.volume).toLocaleString() : '-'}
                      </TableCell>
                      <TableCell align="right">
                        {item.amount ? formatNumber(item.amount) : '-'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        )}
      </Container>
    </LocalizationProvider>
  );
};

export default ScannerPage;