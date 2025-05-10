package klu.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import klu.model.Order;
import klu.model.OrderStatus;
import klu.model.Restaurant;
import klu.model.User;
import klu.repository.RestaurantRepository;
import klu.repository.UserRepository;
import klu.util.EmailManager;

@Service
public class OrderEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEmailService.class);
    
    @Autowired
    private EmailManager emailManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    /**
     * Sends an order confirmation email to the customer
     * 
     * @param order The order that was placed
     * @param orderItems The items in the order
     * @param restaurantName The name of the restaurant
     */
    public void sendOrderConfirmationEmail(Order order, List<Map<String, Object>> orderItems, String restaurantName) {
        // Find the customer
        Optional<User> customerOpt = userRepository.findById(order.getCustomerId());
        if (customerOpt.isEmpty()) {
            logger.error("Cannot send order confirmation email: Customer not found for ID {}", order.getCustomerId());
            return;
        }
        
        User customer = customerOpt.get();
        String subject = "Your GrubGo Order #" + order.getId() + " Confirmation";
        
        // Format the order date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        String formattedDate = dateFormat.format(order.getOrderDate());
        
        // Build the HTML email content
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        htmlBuilder.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append("h1 { color: #4a90e2; }");
        htmlBuilder.append(".order-details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        htmlBuilder.append(".order-items { margin: 20px 0; }");
        htmlBuilder.append("table { width: 100%; border-collapse: collapse; }");
        htmlBuilder.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        htmlBuilder.append(".total { font-weight: bold; text-align: right; margin-top: 15px; }");
        htmlBuilder.append(".footer { margin-top: 30px; font-size: 12px; color: #777; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class='container'>");
        
        // Header
        htmlBuilder.append("<h1>Order Confirmation</h1>");
        htmlBuilder.append("<p>Dear ").append(customer.getName()).append(",</p>");
        htmlBuilder.append("<p>Thank you for your order! We're pleased to confirm that your order has been received and is being processed.</p>");
        
        // Order details
        htmlBuilder.append("<div class='order-details'>");
        htmlBuilder.append("<h2>Order Details</h2>");
        htmlBuilder.append("<p><strong>Order Number:</strong> #").append(order.getId()).append("</p>");
        htmlBuilder.append("<p><strong>Order Date:</strong> ").append(formattedDate).append("</p>");
        htmlBuilder.append("<p><strong>Restaurant:</strong> ").append(restaurantName).append("</p>");
        htmlBuilder.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        htmlBuilder.append("<p><strong>Status:</strong> ").append(order.getStatus().getDisplayName()).append("</p>");
        htmlBuilder.append("</div>");
        
        // Order items
        htmlBuilder.append("<div class='order-items'>");
        htmlBuilder.append("<h2>Order Items</h2>");
        htmlBuilder.append("<table>");
        htmlBuilder.append("<tr><th>Item</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");
        
        for (Map<String, Object> item : orderItems) {
            String itemName = item.containsKey("item_name") ? (String) item.get("item_name") : "Unknown Item";
            int quantity = item.containsKey("quantity") ? Integer.parseInt(item.get("quantity").toString()) : 0;
            double price = item.containsKey("item_price") ? Double.parseDouble(item.get("item_price").toString()) : 0.0;
            double itemTotal = price * quantity;
            
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(itemName).append("</td>");
            htmlBuilder.append("<td>").append(quantity).append("</td>");
            htmlBuilder.append("<td>$").append(String.format("%.2f", price)).append("</td>");
            htmlBuilder.append("<td>$").append(String.format("%.2f", itemTotal)).append("</td>");
            htmlBuilder.append("</tr>");
        }
        
        htmlBuilder.append("</table>");
        htmlBuilder.append("<p class='total'>Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("</p>");
        htmlBuilder.append("</div>");
        
        // Estimated delivery time
        htmlBuilder.append("<p><strong>Estimated Delivery Time:</strong> 30-45 minutes</p>");
        
        // Footer
        htmlBuilder.append("<div class='footer'>");
        htmlBuilder.append("<p>Thank you for choosing GrubGo!</p>");
        htmlBuilder.append("<p>If you have any questions about your order, please contact our customer support.</p>");
        htmlBuilder.append("<p>Best regards,<br>The GrubGo Team</p>");
        htmlBuilder.append("</div>");
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");
        
        try {
            logger.info("Sending order confirmation email to: {}", customer.getEmail());
            emailManager.sendHtmlEmail(customer.getEmail(), subject, htmlBuilder.toString());
            logger.info("Order confirmation email sent successfully to: {}", customer.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to: {}", customer.getEmail(), e);
        }
    }

    /**
     * Sends an order status update email to the customer
     * 
     * @param order The order with the updated status
     */
    public void sendOrderStatusUpdateEmail(Order order) {
        // Find the customer
        Optional<User> customerOpt = userRepository.findById(order.getCustomerId());
        if (customerOpt.isEmpty()) {
            logger.error("Cannot send status update email: Customer not found for ID {}", order.getCustomerId());
            return;
        }
        
        // Find the restaurant
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(order.getRestaurantId());
        String restaurantName = restaurantOpt.isPresent() ? restaurantOpt.get().getName() : "Unknown Restaurant";
        
        User customer = customerOpt.get();
        String subject = "Your GrubGo Order #" + order.getId() + " Status Update";
        
        // Format the order date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        String formattedDate = dateFormat.format(order.getOrderDate());
        
        // Build the HTML email content
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        htmlBuilder.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append("h1 { color: #4a90e2; }");
        htmlBuilder.append(".status-update { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        htmlBuilder.append(".status { font-size: 18px; font-weight: bold; color: #4a90e2; }");
        htmlBuilder.append(".footer { margin-top: 30px; font-size: 12px; color: #777; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class='container'>");
        
        // Header
        htmlBuilder.append("<h1>Order Status Update</h1>");
        htmlBuilder.append("<p>Dear ").append(customer.getName()).append(",</p>");
        
        // Status message based on the order status
        switch (order.getStatus()) {
            case PREPARING:
                htmlBuilder.append("<p>Great news! The restaurant has started preparing your order.</p>");
                break;
            case READY:
                htmlBuilder.append("<p>Your order is now ready for pickup by our delivery partner.</p>");
                break;
            case OUT_FOR_DELIVERY:
                htmlBuilder.append("<p>Your order is on its way! Our delivery partner has picked up your food and is heading to your location.</p>");
                break;
            case DELIVERED:
                htmlBuilder.append("<p>Your order has been delivered. We hope you enjoy your meal!</p>");
                break;
            case CANCELLED:
                htmlBuilder.append("<p>We're sorry to inform you that your order has been cancelled.</p>");
                break;
            default:
                htmlBuilder.append("<p>The status of your order has been updated.</p>");
        }
        
        // Order details
        htmlBuilder.append("<div class='status-update'>");
        htmlBuilder.append("<h2>Order Details</h2>");
        htmlBuilder.append("<p><strong>Order Number:</strong> #").append(order.getId()).append("</p>");
        htmlBuilder.append("<p><strong>Order Date:</strong> ").append(formattedDate).append("</p>");
        htmlBuilder.append("<p><strong>Restaurant:</strong> ").append(restaurantName).append("</p>");
        htmlBuilder.append("<p><strong>Current Status:</strong> <span class='status'>").append(order.getStatus().getDisplayName()).append("</span></p>");
        htmlBuilder.append("</div>");
        
        // Additional information based on status
        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            htmlBuilder.append("<p>You can expect your delivery in approximately 15-30 minutes.</p>");
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            htmlBuilder.append("<p>If you have any questions about the cancellation, please contact our customer support.</p>");
        }
        
        // Footer
        htmlBuilder.append("<div class='footer'>");
        htmlBuilder.append("<p>Thank you for choosing GrubGo!</p>");
        htmlBuilder.append("<p>If you have any questions about your order, please contact our customer support.</p>");
        htmlBuilder.append("<p>Best regards,<br>The GrubGo Team</p>");
        htmlBuilder.append("</div>");
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");
        
        try {
            logger.info("Sending order status update email to: {}", customer.getEmail());
            emailManager.sendHtmlEmail(customer.getEmail(), subject, htmlBuilder.toString());
            logger.info("Order status update email sent successfully to: {}", customer.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send order status update email to: {}", customer.getEmail(), e);
        }
    }
} 