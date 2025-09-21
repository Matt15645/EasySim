import React, { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { Formik, Form } from 'formik';
import * as Yup from 'yup';
import {
  Avatar,
  Button,
  CssBaseline,
  TextField,
  Link,
  Grid,
  Box,
  Typography,
  Container,
  Alert
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import authService from '../services/authService';

function Copyright(props) {
  return (
    <Typography variant="body2" color="text.secondary" align="center" {...props}>
      {'Copyright © '}
      <Link color="inherit" href="https://yourdomain.com/">
        Stock Management System
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

const defaultTheme = createTheme();

const validationSchema = Yup.object({
  username: Yup.string()
    .required('使用者名稱為必填欄位')
    .min(3, '使用者名稱至少需要3個字元'),
  email: Yup.string()
    .email('請輸入有效的電子郵件')
    .required('電子郵件為必填欄位'),
  password: Yup.string()
    .required('密碼為必填欄位')
    .min(8, '密碼至少需要8個字元')
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/,
      '密碼需包含至少一個大寫字母、一個小寫字母和一個數字'
    ),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password'), null], '密碼不匹配')
    .required('確認密碼為必填欄位')
});

export default function SignUp() {
  const [notification, setNotification] = useState(null);
  const navigate = useNavigate();

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      const userData = {
        username: values.username,
        email: values.email,
        password: values.password
      };
      
      await authService.register(userData);
      
      setNotification({
        type: 'success',
        message: '註冊成功！即將轉到登入頁面...'
      });
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setNotification({
        type: 'error',
        message: err.response?.data?.message || '註冊失敗，請稍後再試'
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <ThemeProvider theme={defaultTheme}>
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
            <LockOutlinedIcon />
          </Avatar>
          <Typography component="h1" variant="h5">
            註冊
          </Typography>
          
          {notification && (
            <Alert severity={notification.type} sx={{ mt: 2, width: '100%' }}>
              {notification.message}
            </Alert>
          )}
          
          <Box sx={{ mt: 3 }}>
            <Formik
              initialValues={{
                username: '',
                email: '',
                password: '',
                confirmPassword: ''
              }}
              validationSchema={validationSchema}
              onSubmit={handleSubmit}
            >
              {({ isSubmitting, errors, touched, handleChange, handleBlur }) => (
                <Form>
                  <Grid container spacing={2}>
                    <Grid size={12}>
                      <TextField
                        autoComplete="username"
                        name="username"
                        required
                        fullWidth
                        id="username"
                        label="使用者名稱"
                        autoFocus
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={touched.username && Boolean(errors.username)}
                        helperText={touched.username && errors.username}
                      />
                    </Grid>
                    <Grid size={12}>
                      <TextField
                        required
                        fullWidth
                        id="email"
                        label="電子郵件"
                        name="email"
                        autoComplete="email"
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={touched.email && Boolean(errors.email)}
                        helperText={touched.email && errors.email}
                      />
                    </Grid>
                    <Grid size={12}>
                      <TextField
                        required
                        fullWidth
                        name="password"
                        label="密碼"
                        type="password"
                        id="password"
                        autoComplete="new-password"
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={touched.password && Boolean(errors.password)}
                        helperText={touched.password && errors.password}
                      />
                    </Grid>
                    <Grid size={12}>
                      <TextField
                        required
                        fullWidth
                        name="confirmPassword"
                        label="確認密碼"
                        type="password"
                        id="confirmPassword"
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={touched.confirmPassword && Boolean(errors.confirmPassword)}
                        helperText={touched.confirmPassword && errors.confirmPassword}
                      />
                    </Grid>
                  </Grid>
                  <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    sx={{ mt: 3, mb: 2 }}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? '處理中...' : '註冊'}
                  </Button>
                  <Grid container justifyContent="flex-end">
                    <Grid>
                      <Link component={RouterLink} to="/login" variant="body2">
                        已有帳號？登入
                      </Link>
                    </Grid>
                  </Grid>
                </Form>
              )}
            </Formik>
          </Box>
        </Box>
        <Copyright sx={{ mt: 5 }} />
      </Container>
    </ThemeProvider>
  );
}