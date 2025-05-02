import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Footer from '../footer/Footer';
import { handleApiError, API_ENDPOINTS, axiosConfig } from '../config'; 
import './SignIn.css';

function SignIn() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    if (token) {
      navigate('/dashboard', { replace: true });
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData({ ...formData, [id]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
  
    try {
      const response = await axios.post(API_ENDPOINTS.LOGIN, formData, axiosConfig);
      const data = response.data;
  
      if (data && data.token) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('email', data.email || formData.email);
        localStorage.setItem('role', data.role);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('name', data.name);
  
        navigate('/dashboard', { replace: true });
      } else {
        setError('Invalid credentials or missing token in response');
      }
    } catch (error) {
      console.error('Login error:', error);
      setError(error.response?.data || 'Authentication failed. Please check your credentials.');
    }
  };

  return (
    <>
      <div className="signin-container">
        <div className="logo-container">
          <img src="/logo.png" alt="Logo" className="signin-logo" />
        </div>
        <div className="login-fields">
          <h1 style={{ color: '#4CAF50', textAlign: 'center', marginBottom: '3rem' }}>The Key to Happiness - SignIn</h1>
          {error && <div className="error-message">{error}</div>}
          <form className="signin__form" onSubmit={handleSubmit}>
            <div className="form-group">
              <input
                type="email"
                id="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <input
                type="password"
                id="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>
            <button type="submit" className="signin-button">Log In</button>
          </form>
          <div className="signin-footer">
            <Link to="/signup" className="footer-link">Sign Up</Link>
            <Link to="/forgot-password" className="footer-link">Forgot Password?</Link>
            <Link to="/" className="footer-link">Back to Home</Link>
          </div>
        </div>
      </div>
      <Footer />
    </>
  );
}

export default SignIn; 