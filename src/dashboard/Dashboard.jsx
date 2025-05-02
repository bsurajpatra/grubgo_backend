import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Dashboard.css';
import { API_ENDPOINTS, authAxiosConfig } from '../config';

const Dashboard = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState('');
  const [menuItems, setMenuItems] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedEmail = localStorage.getItem('email');
    const storedRole = localStorage.getItem('role');
    const storedName = localStorage.getItem('name');

    if (!token) {
      navigate('/', { replace: true });
      return;
    }

    setEmail(storedEmail);
    setRole(storedRole);
    setName(storedName || storedEmail);

    // Fetch user's menu items based on role
    const fetchMenuItems = async () => {
      try {
        setLoading(true);
        const response = await axios.get(API_ENDPOINTS.MENU, authAxiosConfig());
        
        if (response.data && response.data.menuItems) {
          setMenuItems(response.data.menuItems);
          if (response.data.name && !storedName) {
            setName(response.data.name);
            localStorage.setItem('name', response.data.name);
          }
        } else {
          // Fallback to role-based menu items if API doesn't return any
          setMenuItems(getDefaultMenuItems(storedRole));
        }
      } catch (err) {
        console.error('Error fetching menu items:', err);
        setError('Using default menu options.');
        
        // Fallback to default menu items based on role
        setMenuItems(getDefaultMenuItems(storedRole));
      } finally {
        setLoading(false);
      }
    };

    fetchMenuItems();
    
    // Prevent going back to login page
    window.history.pushState(null, '', window.location.href);
    const handlePopState = () => {
      window.history.pushState(null, '', window.location.href);
    };
    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [navigate]);

  const handleMenuItemClick = (item) => {
    // Navigate to the corresponding page based on the menu item clicked
    switch (item) {
      case 'Browse Restaurants':
        navigate('/restaurants');
        break;
      case 'Order History':
        navigate('/order-history');
        break;
      case 'Profile':
        navigate('/profile');
        break;
      case 'View Recent Orders':
        navigate('/recent-orders');
        break;
      case 'Update Menu':
        navigate('/update-menu');
        break;
      case 'View New Deliveries':
        navigate('/new-deliveries');
        break;
      case 'Delivery History':
        navigate('/delivery-history');
        break;
      case 'View Restaurants/Partners':
        navigate('/restaurants-partners');
        break;
      case 'Set Local Commission':
        navigate('/set-commission');
        break;
      case 'Manage Users':
        navigate('/manage-users');
        break;
      case 'Manage Restaurants':
        navigate('/manage-restaurants');
        break;
      case 'Manage Delivery Partners':
        navigate('/manage-delivery-partners');
        break;
      case 'Manage Community Presidents':
        navigate('/manage-community-presidents');
        break;
      default:
        navigate('/dashboard');
        break;
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    localStorage.removeItem('userId');
    navigate('/', { replace: true });
  };

  const getRoleDisplay = () => {
    if (!role) return '';
    
    return role.replace('_', ' ').toLowerCase().replace(/\b\w/g, char => char.toUpperCase());
  };

  const getIconForMenuItem = (item) => {
    // Map menu items to appropriate icons
    const iconMap = {
      'Browse Restaurants': '🍔',
      'Order History': '📜',
      'Profile': '👤',
      'View Recent Orders': '🔔',
      'Update Menu': '🍽️',
      'View New Deliveries': '🚚',
      'Delivery History': '📦',
      'View Restaurants/Partners': '🏪',
      'Set Local Commission': '💰',
      'Manage Users': '👥',
      'Manage Restaurants': '🏢',
      'Manage Delivery Partners': '🚚',
      'Manage Community Presidents': '👑'
    };
    
    return iconMap[item] || '📋';
  };

  // Fallback menu items based on role
  const getDefaultMenuItems = (role) => {
    switch (role) {
      case 'CUSTOMER':
        return ['Browse Restaurants', 'Order History', 'Profile'];
      case 'RESTAURANT_OWNER':
        return ['View Recent Orders', 'Update Menu', 'Order History', 'Profile'];
      case 'DELIVERY_PARTNER':
        return ['View New Deliveries', 'Delivery History', 'Profile'];
      case 'COMMUNITY_PRESIDENT':
        return ['View Restaurants/Partners', 'Set Local Commission', 'Profile'];
      case 'SUPER_ADMIN':
        return ['Manage Users', 'Manage Restaurants', 'Manage Delivery Partners', 'Manage Community Presidents', 'Profile'];
      default:
        return ['Profile'];
    }
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Welcome, {name || email || 'Guest'}</h1>
        {role && <p className="role-badge">{getRoleDisplay()}</p>}
        <button onClick={handleLogout} className="logout-button">Logout</button>
      </header>

      {loading ? (
        <div className="loading">Loading dashboard...</div>
      ) : error ? (
        <div className="error-message">{error}</div>
      ) : (
        <div className="card-container">
          {menuItems.map((item, index) => (
            <div 
              key={index} 
              className="menu-card" 
              onClick={() => handleMenuItemClick(item)}
            >
              <div className="card-icon">{getIconForMenuItem(item)}</div>
              <h3>{item}</h3>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard; 