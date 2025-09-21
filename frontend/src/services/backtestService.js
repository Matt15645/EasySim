import axios from './axiosConfig';

// 使用硬編碼的相對路徑，確保在 Kubernetes 環境中正常工作
const API_URL = '/api';

const backtestService = {
  // 執行完整回測分析
  performBacktest: async (backtestRequest) => {
    try {
      const response = await axios.post(`${API_URL}/backtest/analyze`, backtestRequest);
      return response.data;
    } catch (error) {
      console.error('回測請求失敗:', error);
      throw error;
    }
  },

  // 舊的分析端點（如果需要保留）
  analyzeStocks: async (backtestRequest) => {
    try {
      const response = await axios.post(`${API_URL}/backtest/analyze`, backtestRequest);
      return response.data;
    } catch (error) {
      console.error('股票分析請求失敗:', error);
      throw error;
    }
  },

  // 健康檢查
  healthCheck: async () => {
    try {
      const response = await axios.get(`${API_URL}/backtest/health`);
      return response.data;
    } catch (error) {
      console.error('健康檢查失敗:', error);
      throw error;
    }
  }
};

export default backtestService;