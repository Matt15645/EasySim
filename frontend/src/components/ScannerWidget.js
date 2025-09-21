import React, { useState } from 'react';
import {
  Paper,
  Typography,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
  List,
  ListItem,
  ListItemText
} from '@mui/material';
import scannerService from '../services/scannerService';

const ScannerWidget = () => {
  const [scannerType, setScannerType] = useState('ChangePercentRank');
  const [loading, setLoading] = useState(false);
  const [quickData, setQuickData] = useState(null);

  const handleQuickScan = async () => {
    setLoading(true);
    try {
      const today = new Date().toISOString().split('T')[0];
      const request = {
        scanner_type: scannerType,  // ✅ 改為下劃線
        date: today,
        count: 5,
        ascending: false
      };

      const response = await scannerService.getScannerData(request);
      setQuickData(response);
    } catch (error) {
      console.error('快速掃描失敗:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom>
        快速掃描
      </Typography>
      
      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>掃描類型</InputLabel>
        <Select
          value={scannerType}
          label="掃描類型"
          onChange={(e) => setScannerType(e.target.value)}
        >
          <MenuItem value="ChangePercentRank">漲跌幅排序</MenuItem>
          <MenuItem value="VolumeRank">成交量排序</MenuItem>
          <MenuItem value="AmountRank">成交金額排序</MenuItem>
        </Select>
      </FormControl>

      <Button 
        variant="contained" 
        fullWidth 
        onClick={handleQuickScan}
        disabled={loading}
      >
        {loading ? <CircularProgress size={24} /> : '快速掃描 Top 5'}
      </Button>

      {quickData && quickData.data && (
        <List sx={{ mt: 2 }}>
          {quickData.data.slice(0, 5).map((item, index) => (
            <ListItem key={item.code}>
              <ListItemText
                primary={`${index + 1}. ${item.code}`}
                secondary={`漲跌幅: ${item.change_percent?.toFixed(2)}%`}
              />
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  );
};

export default ScannerWidget;