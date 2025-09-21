import axios from './axiosConfig';

// 使用硬編碼的相對路徑，確保在 Kubernetes 環境中正常工作
const API_URL = '/api';

const scannerService = {
  // 取得掃描器資料
  getScannerData: async (scannerRequest) => {
    try {
      const response = await axios.post(`${API_URL}/scanner/data`, scannerRequest);
      return response.data;
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error('請重新登入');
      } else if (error.response?.status === 400) {
        throw new Error('請求參數錯誤');
      } else if (error.response?.status === 500) {
        throw new Error('伺服器錯誤，請稍後再試');
      } else {
        throw new Error('網路錯誤，請檢查連線');
      }
    }
  },

  // 取得可用的掃描器類型（前端定義）
  getScannerTypes: () => {
    return [
      {
        value: 'ChangePercentRank',
        label: '漲跌幅排序',
        description: '依股價漲跌幅百分比排序'
      },
      {
        value: 'ChangePriceRank',
        label: '漲跌價排序', 
        description: '依股價漲跌金額排序'
      },
      {
        value: 'VolumeRank',
        label: '成交量排序',
        description: '依成交量大小排序'
      },
      {
        value: 'AmountRank',
        label: '成交金額排序',
        description: '依成交金額大小排序'
      },
      {
        value: 'DayRangeRank',
        label: '振幅排序',
        description: '依當日股價振幅排序'
      }
    ];
  }
};

export default scannerService;