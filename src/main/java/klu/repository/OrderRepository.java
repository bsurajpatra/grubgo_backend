package klu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import klu.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    
    @Query(value = "SELECT o.id AS order_id, " +
                  "u.name AS customer_name, " +
                  "u.email AS customer_email, " +
                  "o.item_name, " +
                  "o.total_amount, " +
                  "o.status, " +
                  "o.order_date, " +
                  "o.delivery_partner_id, " +
                  "o.delivery_address " +
                  "FROM orders o " +
                  "INNER JOIN users u ON o.customer_id = u.id " +
                  "ORDER BY o.order_date DESC", nativeQuery = true)
    List<Map<String, Object>> getAllOrderHistory();
    
    @Query(value = "SELECT o.id AS order_id, " +
                  "u.name AS customer_name, " +
                  "u.email AS customer_email, " +
                  "o.item_name, " +
                  "o.total_amount, " +
                  "o.status, " +
                  "o.order_date, " +
                  "o.delivery_partner_id, " +
                  "o.delivery_address " +
                  "FROM orders o " +
                  "INNER JOIN user u ON o.customer_id = u.id " +
                  "WHERE u.id = :customerId " +
                  "ORDER BY o.order_date DESC", nativeQuery = true)
    List<Map<String, Object>> getOrderHistoryByCustomerId(@Param("customerId") Long customerId);
}
