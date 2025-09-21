import React from 'react';
import { render } from '@testing-library/react';
import App from './App';

// Mock localStorage properly
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: jest.fn((key) => store[key] || null),
    setItem: jest.fn((key, value) => { store[key] = value.toString(); }),
    removeItem: jest.fn((key) => { delete store[key]; }),
    clear: jest.fn(() => { store = {}; }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

// Mock all the page components to avoid dependency issues
jest.mock('./pages/Login', () => {
  return function MockLogin() {
    return <div>Login Page</div>;
  };
});

jest.mock('./pages/Register', () => {
  return function MockRegister() {
    return <div>Register Page</div>;
  };
});

jest.mock('./pages/Dashboard', () => {
  return function MockDashboard() {
    return <div>Dashboard Page</div>;
  };
});

jest.mock('./pages/Account', () => {
  return function MockAccount() {
    return <div>Account Page</div>;
  };
});

jest.mock('./pages/BacktestPage', () => {
  return function MockBacktestPage() {
    return <div>Backtest Page</div>;
  };
});

jest.mock('./pages/ScannerPage', () => {
  return function MockScannerPage() {
    return <div>Scanner Page</div>;
  };
});

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders without crashing', () => {
    localStorageMock.getItem.mockReturnValue(null);
    
    render(<App />);
    
    // The app should render without crashing
    expect(true).toBe(true);
  });
});
