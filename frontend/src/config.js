// Fallback to localhost if environment variable is not set
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const axiosConfig = {
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
};

export const authAxiosConfig = () => ({
  headers: {
    ...axiosConfig.headers,
    ...getAuthHeader()
  }
});

export const API_ENDPOINTS = {
  // Authentication - updated to match backend endpoints
  LOGIN: `${API_BASE_URL}/user/login`, 
  REGISTER: `${API_BASE_URL}/user/register`, 
  MENU: `${API_BASE_URL}/dashboard/menu`, 
  
  // Restaurant endpoints
  RESTAURANTS: `${API_BASE_URL}/restaurants`,
  RESTAURANT_MENU: (id) => `${API_BASE_URL}/restaurants/${id}/menu`,
  
  // Order endpoints
  ORDERS: `${API_BASE_URL}/customer/order-history`,
  RECENT_ORDERS: `${API_BASE_URL}/orders/recent`,
  
  // Delivery endpoints
  DELIVERIES: `${API_BASE_URL}/deliveries`,
  NEW_DELIVERIES: `${API_BASE_URL}/deliveries/new`,
  DELIVERY_HISTORY: `${API_BASE_URL}/deliveries/history`,
  
  // Community endpoints
  COMMUNITY_COMMISSION: `${API_BASE_URL}/community/commission`,
  
  // Admin endpoints
  ADMIN_USERS: `${API_BASE_URL}/super-admin/users`,
  ADMIN_RESTAURANTS: `${API_BASE_URL}/super-admin/restaurants`,
  ADMIN_DELIVERY_PARTNERS: `${API_BASE_URL}/super-admin/delivery-partners`,
  ADMIN_COMMUNITY_PRESIDENTS: `${API_BASE_URL}/super-admin/community-presidents`,
  
  // User profile - added missing endpoints
  USER_PROFILE: `${API_BASE_URL}/user/profile`,
  UPDATE_PROFILE: `${API_BASE_URL}/user/profile`,
  CHANGE_PASSWORD: `${API_BASE_URL}/user/change-password`,
  DELETE_ACCOUNT: `${API_BASE_URL}/user/account`,
  FORGOT_PASSWORD: `${API_BASE_URL}/password/forgot`
};

export const handleApiError = (error) => {
  if (error.response) {
    if (error.response.status === 401 || error.response.status === 403) {
      localStorage.removeItem('token');
      return 'Session expired. Please login again.';
    }
    return error.response.data || 'An error occurred';
  } else if (error.request) {
    return 'No response from server. Please try again.';
  } else {
    return error.message || 'An unexpected error occurred';
  }
}; 