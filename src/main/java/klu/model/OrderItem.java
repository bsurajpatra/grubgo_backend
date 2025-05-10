package klu.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "item_price", nullable = false)
    private BigDecimal itemPrice;
    
    // Constructors
    public OrderItem() {
    }
    
    public OrderItem(Long orderId, Long menuItemId, String itemName, Integer quantity, BigDecimal itemPrice) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getMenuItemId() {
        return menuItemId;
    }
    
    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getItemPrice() {
        return itemPrice;
    }
    
    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }
} 