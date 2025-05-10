import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../config';
import './ViewOrders.css';
import { toast } from 'react-toastify';

const ViewOrders = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    // Check if user is a restaurant owner
    const userRole = localStorage.getItem('role');
    if (userRole !== 'RESTAURANT_OWNER') {
      navigate('/dashboard');
      return;
    }

    fetchRecentOrders();
  }, [navigate]);

  const fetchRecentOrders = async () => {
    try {
      setLoading(true);
      const email = localStorage.getItem('email');
      
      console.log('Fetching orders with email:', email);
      
      if (!email) {
        setError('No email found in localStorage. Please log in again.');
        setLoading(false);
        return;
      }
      
      // First check database connection
      try {
        const dbTestResponse = await axios.get(`${API_ENDPOINTS.RESTAURANTS}/test-db`);
        console.log('Database test response:', dbTestResponse.data);
        
        if (!dbTestResponse.data.success) {
          setError(`Database connection error: ${dbTestResponse.data.error}`);
          setLoading(false);
          return;
        }
      } catch (dbErr) {
        console.error('Database test error:', dbErr);
        // Continue with the regular request even if the test fails
      }
      
      // Try the direct email-to-restaurant approach first (most accurate)
      try {
        console.log('Trying direct email-to-restaurant approach...');
        const emailBasedResponse = await axios.get(API_ENDPOINTS.RESTAURANT_ORDERS_BY_EMAIL, {
          params: { email }
        });
        
        console.log('Direct email-to-restaurant response:', emailBasedResponse.data);
        
        if (emailBasedResponse.data && emailBasedResponse.data.orders) {
          setOrders(emailBasedResponse.data.orders);
          setError(null);
          setLoading(false);
          return;
        }
      } catch (emailErr) {
        console.error('Direct email-to-restaurant approach failed:', emailErr);
        // Continue with the next approach if this fails
      }
      
      // Try the name-based approach as fallback
      try {
        console.log('Trying name-based approach as fallback...');
        const nameBasedResponse = await axios.get(API_ENDPOINTS.RESTAURANT_ORDERS_BY_NAME, {
          params: { email }
        });
        
        console.log('Name-based response received:', nameBasedResponse.data);
        
        if (nameBasedResponse.data && nameBasedResponse.data.orders) {
          setOrders(nameBasedResponse.data.orders);
          setError(null);
          setLoading(false);
          return;
        }
      } catch (nameErr) {
        console.error('Name-based approach failed:', nameErr);
        // Continue with the original approach if the name-based approach fails
      }
      
      // Then call the debug endpoint to check if restaurant lookup works
      try {
        const debugResponse = await axios.get(`${API_ENDPOINTS.RESTAURANTS}/debug`, {
          params: { email }
        });
        console.log('Debug response:', debugResponse.data);
        
        if (!debugResponse.data.success) {
          setError(`Debug error: ${debugResponse.data.error}`);
          setLoading(false);
          return;
        }
        
        if (debugResponse.data.matchingRestaurants === 0) {
          setError(`No restaurant found for owner email: ${email}. Please check that this email is associated with a restaurant.`);
          setLoading(false);
          return;
        }
      } catch (debugErr) {
        console.error('Debug endpoint error:', debugErr);
        // Continue with the regular request even if debug fails
      }
      
      // Fall back to the original approach (email → owner ID → restaurant → orders)
      const response = await axios.get(API_ENDPOINTS.RESTAURANT_ORDERS, {
        params: { email }
      });

      console.log('Original approach response received:', response.data);

      if (response.data && response.data.orders) {
        setOrders(response.data.orders);
      } else {
        setOrders([]);
      }
      setError(null);
    } catch (err) {
      console.error('Error fetching recent orders:', err);
      
      // More detailed error logging
      if (err.response) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        console.error('Response data:', err.response.data);
        console.error('Response status:', err.response.status);
        console.error('Response headers:', err.response.headers);
        setError(`Error ${err.response.status}: ${JSON.stringify(err.response.data)}`);
      } else if (err.request) {
        // The request was made but no response was received
        console.error('Request made but no response received');
        setError('No response received from server. Please check your connection.');
      } else {
        // Something happened in setting up the request that triggered an Error
        console.error('Error setting up request:', err.message);
        setError(`Error: ${err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const email = localStorage.getItem('email');
      
      if (!email) {
        toast.error('No email found. Please log in again.');
        return;
      }
      
      console.log(`Updating order ${orderId} status to ${newStatus} with email: ${email}`);
      
      await axios.patch(
        API_ENDPOINTS.ORDER_STATUS(orderId), 
        { status: newStatus },
        { params: { email } }
      );
      
      // Update the order status locally
      setOrders(orders.map(order => 
        order.order_id === orderId ? { ...order, status: newStatus } : order
      ));
      
      toast.success(`Order #${orderId} status updated to ${newStatus}`);
    } catch (err) {
      console.error('Error updating order status:', err);
      
      if (err.response) {
        toast.error(`Failed to update status: ${JSON.stringify(err.response.data)}`);
      } else {
        toast.error('Failed to update order status. Please try again.');
      }
    }
  };

  const handleBackToDashboard = () => {
    navigate('/dashboard');
  };

  const getFilteredOrders = () => {
    if (statusFilter === 'all') {
      return orders;
    }
    return orders.filter(order => order.status.toLowerCase() === statusFilter.toLowerCase());
  };

  const getStatusBadgeClass = (status) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'status-badge pending';
      case 'preparing':
        return 'status-badge preparing';
      case 'ready':
        return 'status-badge ready';
      case 'delivered':
        return 'status-badge delivered';
      case 'cancelled':
        return 'status-badge cancelled';
      default:
        return 'status-badge';
    }
  };

  if (loading) {
    return (
      <div className="recent-orders">
        <h2>Recent Orders</h2>
        <div className="loading">Loading recent orders...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="recent-orders">
        <h2>Recent Orders</h2>
        <div className="error-message">{error}</div>
        <button onClick={handleBackToDashboard} className="back-button">Back to Dashboard</button>
      </div>
    );
  }

  return (
    <div className="recent-orders">
      <header className="orders-header">
        <h2>Recent Orders</h2>
        <button onClick={handleBackToDashboard} className="back-button">Back to Dashboard</button>
      </header>

      <div className="filters">
        <div className="filter-group">
          <label htmlFor="status-filter">Filter by Status:</label>
          <select 
            id="status-filter" 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="all">All Orders</option>
            <option value="pending">Pending</option>
            <option value="preparing">Preparing</option>
            <option value="ready">Ready for Pickup</option>
            <option value="delivered">Delivered</option>
            <option value="cancelled">Cancelled</option>
          </select>
        </div>
        <button onClick={fetchRecentOrders} className="refresh-button">
          Refresh Orders
        </button>
      </div>

      {getFilteredOrders().length === 0 ? (
        <div className="no-orders">
          <p>No {statusFilter !== 'all' ? statusFilter : ''} orders found.</p>
        </div>
      ) : (
        <div className="orders-list">
          {getFilteredOrders().map(order => (
            <div key={order.order_id} className="order-card">
              <div className="order-header">
                <span className="order-id">Order #{order.order_id}</span>
                <span className={getStatusBadgeClass(order.status)}>
                  {order.status}
                </span>
              </div>
              
              <div className="order-time">
                <p><strong>Ordered:</strong> {new Date(order.created_at).toLocaleString()}</p>
              </div>
              
              <div className="customer-info">
                <p><strong>Customer:</strong> {order.customer_name || 'Anonymous'}</p>
                <p><strong>Delivery Address:</strong> {order.delivery_address}</p>
              </div>
              
              <div className="order-items">
                <h4>Items:</h4>
                <ul>
                  {order.items && Array.isArray(order.items) ? (
                    order.items.map((item, index) => (
                      <li key={index}>
                        <span className="item-quantity">{item.quantity}x</span>
                        <span className="item-name">{item.name || item.item_name}</span>
                        <span className="item-price">${parseFloat(item.item_price).toFixed(2)}</span>
                      </li>
                    ))
                  ) : (
                    <li>No items available</li>
                  )}
                </ul>
                <div className="order-total">
                  <p><strong>Total:</strong> ${parseFloat(order.total_amount).toFixed(2)}</p>
                </div>
              </div>
              
              {order.special_instructions && (
                <div className="special-instructions">
                  <p><strong>Special Instructions:</strong></p>
                  <p>{order.special_instructions}</p>
                </div>
              )}
              
              <div className="order-actions">
                <h4>Update Status:</h4>
                <div className="action-buttons">
                  {order.status.toLowerCase() === 'pending' && (
                    <>
                      <button 
                        onClick={() => handleStatusChange(order.order_id, 'PREPARING')}
                        className="action-button preparing"
                      >
                        Start Preparing
                      </button>
                      <button 
                        onClick={() => handleStatusChange(order.order_id, 'CANCELLED')}
                        className="action-button cancel"
                      >
                        Cancel Order
                      </button>
                    </>
                  )}
                  
                  {order.status.toLowerCase() === 'preparing' && (
                    <button 
                      onClick={() => handleStatusChange(order.order_id, 'READY')}
                      className="action-button ready"
                    >
                      Mark as Ready
                    </button>
                  )}
                  
                  {order.status.toLowerCase() === 'ready' && (
                    <button 
                      onClick={() => handleStatusChange(order.order_id, 'DELIVERED')}
                      className="action-button delivered"
                    >
                      Mark as Delivered
                    </button>
                  )}
                  
                  {(order.status.toLowerCase() !== 'delivered' && 
                    order.status.toLowerCase() !== 'cancelled') && (
                    <button 
                      onClick={() => handleStatusChange(order.order_id, 'CANCELLED')}
                      className="action-button cancel"
                    >
                      Cancel Order
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ViewOrders; 