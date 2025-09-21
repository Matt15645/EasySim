import axios from './axiosConfig'; // 改用 axiosConfig

// 使用硬編碼的相對路徑，確保在 Kubernetes 環境中正常工作
const API_URL = '/api';

const authService = {
  register: async (userData) => {
    const res = await axios.post(`${API_URL}/auth/register`, userData);
    if (res.data && res.data.token) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data));
    }
    return res.data;
  },
  login: async (credentials) => {
    const res = await axios.post(`${API_URL}/auth/login`, credentials);
    if (res.data && res.data.token) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data));
    }
    return res.data;
  },
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  setUser: (user) => {
    localStorage.setItem('user', JSON.stringify(user));
  },
  logout: () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }
};

export default authService;