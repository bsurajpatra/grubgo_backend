package klu.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import klu.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    
    @Query(value = "SELECT oi.id, oi.order_id, oi.menu_item_id, oi.item_name, oi.quantity, oi.item_price, " +
                  "mi.name, mi.description " +
                  "FROM order_items oi " +
                  "INNER JOIN menu_items mi ON oi.menu_item_id = mi.item_id " +
                  "WHERE oi.order_id = :orderId", nativeQuery = true)
    List<Map<String, Object>> getOrderItemsWithDetails(@Param("orderId") Long orderId);
} 