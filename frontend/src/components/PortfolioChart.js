import React, { useState } from 'react';
import {
  Box,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
  Paper
} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';

const PortfolioChart = ({ portfolioHistory, stockPrices }) => {
  const [chartType, setChartType] = useState('portfolio');

  if (!portfolioHistory || portfolioHistory.length === 0) {
    return null;
  }

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('zh-TW', {
      style: 'currency',
      currency: 'TWD',
      minimumFractionDigits: 0
    }).format(value);
  };

  const formatPercentage = (value) => {
    if (value === null || value === undefined) return 'N/A';
    return `${(parseFloat(value) * 100).toFixed(2)}%`;
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <Paper sx={{ p: 2 }}>
          <Typography variant="body2" sx={{ mb: 1 }}>
            日期: {label}
          </Typography>
          {payload.map((entry, index) => (
            <Typography
              key={index}
              variant="body2"
              sx={{ color: entry.color }}
            >
              {entry.name}: {
                chartType === 'portfolio' ? 
                formatCurrency(entry.value) : 
                entry.dataKey === 'dailyReturn' ? 
                formatPercentage(entry.value) : 
                entry.value
              }
            </Typography>
          ))}
        </Paper>
      );
    }
    return null;
  };

  const prepareStockPriceData = () => {
    if (!stockPrices || stockPrices.length === 0) return [];
    
    // 假設 stockPrices 包含多支股票的價格資料
    const allDates = new Set();
    stockPrices.forEach(stock => {
      stock.dataPoints.forEach(point => allDates.add(point.date));
    });
    
    const sortedDates = Array.from(allDates).sort();
    
    return sortedDates.map(date => {
      const dataPoint = { date };
      stockPrices.forEach(stock => {
        const pointData = stock.dataPoints.find(p => p.date === date);
        if (pointData) {
          dataPoint[stock.symbol] = parseFloat(pointData.closePrice);
        }
      });
      return dataPoint;
    });
  };

  const stockColors = ['#8884d8', '#82ca9d', '#ffc658', '#ff7300', '#00ff00'];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          歷史走勢圖
        </Typography>
        <ToggleButtonGroup
          value={chartType}
          exclusive
          onChange={(e, newType) => newType && setChartType(newType)}
          size="small"
        >
          <ToggleButton value="portfolio">
            投資組合價值
          </ToggleButton>
          <ToggleButton value="returns">
            日報酬率
          </ToggleButton>
          {stockPrices && stockPrices.length > 0 && (
            <ToggleButton value="stocks">
              股價走勢
            </ToggleButton>
          )}
        </ToggleButtonGroup>
      </Box>

      <Paper sx={{ p: 2 }}>
        <Box sx={{ width: '100%', height: 400 }}>
          <ResponsiveContainer>
            {chartType === 'portfolio' ? (
              <AreaChart data={portfolioHistory}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis tickFormatter={formatCurrency} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Area
                  type="monotone"
                  dataKey="totalValue"
                  stroke="#8884d8"
                  fill="#8884d8"
                  fillOpacity={0.3}
                  name="投資組合總值"
                />
              </AreaChart>
            ) : chartType === 'returns' ? (
              <LineChart data={portfolioHistory}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis tickFormatter={(value) => `${(value * 100).toFixed(1)}%`} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="dailyReturn"
                  stroke="#82ca9d"
                  strokeWidth={2}
                  name="日報酬率"
                  connectNulls={false}
                />
              </LineChart>
            ) : (
              <LineChart data={prepareStockPriceData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                {stockPrices && stockPrices.map((stock, index) => (
                  <Line
                    key={stock.symbol}
                    type="monotone"
                    dataKey={stock.symbol}
                    stroke={stockColors[index % stockColors.length]}
                    strokeWidth={2}
                    name={stock.symbol}
                    connectNulls={false}
                  />
                ))}
              </LineChart>
            )}
          </ResponsiveContainer>
        </Box>
      </Paper>
    </Box>
  );
};

export default PortfolioChart;