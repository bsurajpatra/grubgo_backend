package klu.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import klu.model.Order;
import klu.model.OrderItem;
import klu.model.Restaurant;
import klu.repository.MenuItemRepository;
import klu.repository.OrderItemRepository;
import klu.repository.OrderRepository;
import klu.repository.RestaurantRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    public List<Map<String, Object>> getDetailedOrderHistoryByCustomerId(Long customerId) {
        return orderRepository.getOrderHistoryByCustomerId(customerId);
    }

    public List<Map<String, Object>> getAllOrderHistory() {
        return orderRepository.getAllOrderHistory();
    }
    
    @Transactional
    public Order createOrder(Long customerId, Long restaurantId, List<Map<String, Object>> items, 
                            Double totalAmount, String deliveryAddress) {
        // Create and save the order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(restaurantId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");
        order.setOrderDate(new Date());
        order.setDeliveryAddress(deliveryAddress);
        
        Order savedOrder = orderRepository.save(order);
        
        // Create and save order items
        for (Map<String, Object> item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setMenuItemId(Long.valueOf(item.get("menu_item_id").toString()));
            orderItem.setQuantity(Integer.valueOf(item.get("quantity").toString()));
            orderItem.setItemPrice(new BigDecimal(item.get("item_price").toString()));
            
            // Set item name if available
            if (item.containsKey("item_name")) {
                orderItem.setItemName((String) item.get("item_name"));
            }
            
            orderItemRepository.save(orderItem);
        }
        
        return savedOrder;
    }
    
    public Map<String, Object> getOrderConfirmation(Long orderId, Long customerId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isEmpty() || !orderOpt.get().getCustomerId().equals(customerId)) {
            return null;
        }
        
        Order order = orderOpt.get();
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(order.getRestaurantId());
        String restaurantName = restaurantOpt.isPresent() ? restaurantOpt.get().getName() : "Unknown Restaurant";
        
        List<Map<String, Object>> orderItems = orderItemRepository.getOrderItemsWithDetails(orderId);
        
        Map<String, Object> confirmation = new HashMap<>();
        confirmation.put("order_id", order.getId());
        confirmation.put("restaurant_name", restaurantName);
        confirmation.put("total_amount", order.getTotalAmount());
        confirmation.put("status", order.getStatus());
        confirmation.put("order_date", order.getOrderDate());
        confirmation.put("delivery_address", order.getDeliveryAddress());
        confirmation.put("items", orderItems);
        confirmation.put("estimated_delivery_time", "30-45 minutes"); // This could be calculated based on various factors
        
        return confirmation;
    }
}
