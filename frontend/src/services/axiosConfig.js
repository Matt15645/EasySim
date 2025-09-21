import axios from 'axios';

const instance = axios.create();

instance.interceptors.request.use(
  config => {
    // 登入和註冊請求不需要 Authorization header
    const isAuthRequest = config.url?.includes('/auth/login') || config.url?.includes('/auth/register');
    
    if (!isAuthRequest) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    }
    return config;
  },
  error => Promise.reject(error)
);

export default instance;
