package klu.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import klu.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    
    @Query(value = "SELECT o.id AS order_id, " +
                  "u.name AS customer_name, " +
                  "u.email AS customer_email, " +
                  "r.name AS restaurant_name, " +
                  "o.total_amount, " +
                  "o.status, " +
                  "o.order_date, " +
                  "o.delivery_partner_id, " +
                  "o.delivery_address " +
                  "FROM orders o " +
                  "INNER JOIN users u ON o.customer_id = u.id " +
                  "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                  "ORDER BY o.order_date DESC", nativeQuery = true)
    List<Map<String, Object>> getAllOrderHistory();
    
    @Query(value = "SELECT o.id AS order_id, " +
                  "u.name AS customer_name, " +
                  "u.email AS customer_email, " +
                  "r.name AS restaurant_name, " +
                  "o.total_amount, " +
                  "o.status, " +
                  "o.order_date, " +
                  "o.delivery_partner_id, " +
                  "o.delivery_address " +
                  "FROM orders o " +
                  "INNER JOIN users u ON o.customer_id = u.id " +
                  "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                  "WHERE u.id = :customerId " +
                  "ORDER BY o.order_date DESC", nativeQuery = true)
    List<Map<String, Object>> getOrderHistoryByCustomerId(@Param("customerId") Long customerId);
}
