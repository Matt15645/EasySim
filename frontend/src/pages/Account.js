import React, { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { getPortfolioPieChart } from '../services/accountService';
import { Box, Typography, CircularProgress, Paper } from '@mui/material';

const COLORS = [
  "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
  "#FF9F40", "#C9CBCF", "#4BC0C0", "#FF6384", "#CCCCCC"
];

export default function Account() {
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPortfolioPieChart()
      .then(res => {
        setData(res.positions);
        setTotal(res.totalValue);
      })
      .catch(error => {
        console.error('Failed to fetch portfolio pie chart:', error);
        console.error('Error details:', error.response);
        setData([]);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <Box sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>持股圓餅圖</Typography>
      {loading ? (
        <CircularProgress />
      ) : (
        <Paper sx={{ p: 2 }}>
          <Typography variant="subtitle1">總市值：{total}</Typography>
          <ResponsiveContainer width="100%" height={400}>
            <PieChart>
              <Pie
                data={data}
                dataKey="value"
                nameKey="label"
                cx="50%"
                cy="50%"
                outerRadius={150}
                label
              >
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </Paper>
      )}
    </Box>
  );
}