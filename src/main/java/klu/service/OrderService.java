package klu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import klu.model.Order;
import klu.repository.OrderRepository;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    public List<Map<String, Object>> getDetailedOrderHistoryByCustomerId(Long customerId) {
        return orderRepository.getOrderHistoryByCustomerId(customerId);
    }

    public List<Map<String, Object>> getAllOrderHistory() {
        return orderRepository.getAllOrderHistory();
    }
}
