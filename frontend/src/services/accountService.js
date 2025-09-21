import axios from './axiosConfig';

// const API_BASE = process.env.REACT_APP_ACCOUNT_API || 'http://localhost:8082/api/account';

const API_BASE = '/api/account';

export const getPortfolioPieChart = async () => {
  const res = await axios.get(`${API_BASE}/portfolio/pie-chart`);
  return res.data;
};

export const getPortfolio = async () => {
  const res = await axios.get(`${API_BASE}/portfolio`);
  return res.data;
};

export const getPortfolioSummary = async () => {
  const res = await axios.get(`${API_BASE}/portfolio/summary`);
  return res.data;
};