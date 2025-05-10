package klu.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import klu.model.MenuItem;
import klu.model.Order;
import klu.model.OrderStatus;
import klu.model.Restaurant;
import klu.model.User;
import klu.repository.MenuItemRepository;
import klu.repository.OrderRepository;
import klu.repository.RestaurantRepository;
import klu.repository.UserRepository;
import klu.service.OrderService;
import klu.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);
    
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(@RequestParam(required = false) String address) {
        List<Restaurant> restaurants;
        if (address != null && !address.isEmpty()) {
            restaurants = restaurantService.findByAddress(address);
        } else {
            restaurants = restaurantService.findAll();
        }
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> getMenuForRestaurant(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);
        if (menuItems.isEmpty()) {
            return ResponseEntity.notFound().build(); // Return 404 if no menu items found
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<?> getRestaurantOrders(@RequestParam(required = true) String email) {
        logger.info("Received request for restaurant orders with email: {}", email);
        
        try {
            if (email == null || email.isEmpty()) {
                logger.error("Email parameter is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Email parameter is required"));
            }
            
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            logger.info("Found user: {} (ID: {})", user.getName(), user.getId());
            
            // Try to find restaurant using multiple methods
            Optional<Restaurant> restaurantOpt = restaurantRepository.findByOwnerEmailNative(email);
            
            // If native query fails, try JPQL query
            if (restaurantOpt.isEmpty()) {
                logger.info("Native query failed, trying JPQL query");
                restaurantOpt = restaurantRepository.findByOwnerEmail(email);
            }
            
            // If both queries fail, try direct owner ID lookup
            if (restaurantOpt.isEmpty()) {
                logger.info("Email-based queries failed, trying direct owner ID lookup");
                restaurantOpt = restaurantRepository.findByOwnerId(user.getId());
            }
            
            // If all methods fail, return error
            if (restaurantOpt.isEmpty()) {
                logger.error("No restaurant found for owner with ID: {}", user.getId());
                return ResponseEntity.badRequest().body(Map.of("error", "No restaurant found for this owner"));
            }
            
            Restaurant restaurant = restaurantOpt.get();
            logger.info("Found restaurant: {} (ID: {})", restaurant.getName(), restaurant.getId());
            
            // Get orders for this restaurant with detailed information
            List<Map<String, Object>> orders = orderRepository.getOrdersByRestaurantId(restaurant.getId());
            logger.info("Found {} orders for restaurant", orders.size());
            
            // For each order, fetch the order items
            for (Map<String, Object> order : orders) {
                Long orderId = ((Number) order.get("order_id")).longValue();
                List<Map<String, Object>> items = orderService.getOrderItemDetails(orderId);
                order.put("items", items);
            }
            
            return ResponseEntity.ok(Map.of("orders", orders));
        } catch (Exception e) {
            logger.error("Error processing restaurant orders request", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }
    
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusUpdate,
            @RequestParam String email) {
        
        logger.info("Received request to update order {} status with email: {}", orderId, email);
        
        try {
            if (email == null || email.isEmpty()) {
                logger.error("Email parameter is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Email parameter is required"));
            }
            
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            // Try to find restaurant using multiple methods
            Optional<Restaurant> restaurantOpt = restaurantRepository.findByOwnerEmailNative(email);
            
            // If native query fails, try JPQL query
            if (restaurantOpt.isEmpty()) {
                logger.info("Native query failed, trying JPQL query");
                restaurantOpt = restaurantRepository.findByOwnerEmail(email);
            }
            
            // If both queries fail, try direct owner ID lookup
            if (restaurantOpt.isEmpty()) {
                logger.info("Email-based queries failed, trying direct owner ID lookup");
                restaurantOpt = restaurantRepository.findByOwnerId(user.getId());
            }
            
            // If all methods fail, return error
            if (restaurantOpt.isEmpty()) {
                logger.error("No restaurant found for owner with ID: {}", user.getId());
                return ResponseEntity.badRequest().body(Map.of("error", "No restaurant found for this owner"));
            }
            
            Restaurant restaurant = restaurantOpt.get();
            
            // Get the order
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                logger.error("Order not found with ID: {}", orderId);
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Verify this order belongs to the restaurant
            if (!order.getRestaurantId().equals(restaurant.getId())) {
                logger.error("Order {} does not belong to restaurant {}", orderId, restaurant.getId());
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to update this order"));
            }
            
            // Update the status
            String newStatusStr = statusUpdate.get("status");
            if (newStatusStr == null || newStatusStr.isEmpty()) {
                logger.error("Status is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(newStatusStr);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid status value: {}", newStatusStr);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value"));
            }
            
            Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
            logger.info("Successfully updated order {} status to {}", orderId, newStatus);
            
            return ResponseEntity.ok(Map.of(
                "message", "Order status updated successfully",
                "order", Map.of(
                    "id", updatedOrder.getId(),
                    "status", updatedOrder.getStatus().name()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Error updating order status", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update order status", "message", e.getMessage()));
        }
    }

    @GetMapping("/test-lookup")
    public ResponseEntity<?> testLookup(@RequestParam String email) {
        logger.info("Testing lookup with email: {}", email);
        
        try {
            // Test user lookup
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "User not found for email: " + email
                ));
            }
            
            logger.info("Found user: {} (ID: {}, Role: {})", user.getName(), user.getId(), user.getRole());
            
            // Test restaurant lookup with JPQL query
            Optional<Restaurant> restaurantOpt = restaurantRepository.findByOwnerEmail(email);
            boolean jpqlFound = restaurantOpt.isPresent();
            
            // Test restaurant lookup with native query
            Optional<Restaurant> restaurantNativeOpt = restaurantRepository.findByOwnerEmailNative(email);
            boolean nativeFound = restaurantNativeOpt.isPresent();
            
            // Test direct lookup by owner_id
            List<Restaurant> restaurantsByOwnerId = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(user.getId()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ),
                "jpqlQueryFound", jpqlFound,
                "nativeQueryFound", nativeFound,
                "directLookupFound", !restaurantsByOwnerId.isEmpty(),
                "restaurantsCount", restaurantsByOwnerId.size()
            ));
            
        } catch (Exception e) {
            logger.error("Error in test lookup", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugRestaurantLookup(@RequestParam String email) {
        logger.info("Debug restaurant lookup with email: {}", email);
        
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "User not found for email: " + email
                ));
            }
            
            logger.info("Found user: {} (ID: {}, Role: {})", user.getName(), user.getId(), user.getRole());
            
            // Get all restaurants
            List<Restaurant> allRestaurants = restaurantRepository.findAll();
            logger.info("Total restaurants in database: {}", allRestaurants.size());
            
            // Find restaurants where owner_id matches user.id
            List<Restaurant> matchingRestaurants = allRestaurants.stream()
                .filter(r -> r.getOwnerId() != null && r.getOwnerId().equals(user.getId()))
                .collect(Collectors.toList());
            
            logger.info("Found {} restaurants with owner_id = {}", matchingRestaurants.size(), user.getId());
            
            // Try both query methods
            Optional<Restaurant> jpqlResult = restaurantRepository.findByOwnerEmail(email);
            Optional<Restaurant> nativeResult = restaurantRepository.findByOwnerEmailNative(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ),
                "totalRestaurants", allRestaurants.size(),
                "matchingRestaurants", matchingRestaurants.size(),
                "jpqlQueryFound", jpqlResult.isPresent(),
                "nativeQueryFound", nativeResult.isPresent(),
                "matchingRestaurantDetails", matchingRestaurants.stream()
                    .map(r -> Map.of(
                        "id", r.getId(),
                        "name", r.getName(),
                        "ownerId", r.getOwnerId()
                    ))
                    .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            logger.error("Error in debug endpoint", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/test-db")
    public ResponseEntity<?> testDbConnection() {
        logger.info("Testing database connection");
        
        try {
            // Check if we can retrieve users
            long userCount = userRepository.count();
            logger.info("Found {} users in database", userCount);
            
            // Check if we can retrieve restaurants
            long restaurantCount = restaurantRepository.count();
            logger.info("Found {} restaurants in database", restaurantCount);
            
            // Check if we can retrieve orders
            long orderCount = orderRepository.count();
            logger.info("Found {} orders in database", orderCount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Database connection successful",
                "counts", Map.of(
                    "users", userCount,
                    "restaurants", restaurantCount,
                    "orders", orderCount
                )
            ));
        } catch (Exception e) {
            logger.error("Error testing database connection", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/create-for-user")
    public ResponseEntity<?> createRestaurantForUser(@RequestParam String email, 
                                                    @RequestParam String name,
                                                    @RequestParam(required = false) String address,
                                                    @RequestParam(required = false) String phone,
                                                    @RequestParam(required = false) String cuisineType,
                                                    @RequestParam(required = false) String openingHours,
                                                    @RequestParam(required = false) Double rating) {
        logger.info("Creating restaurant for user with email: {}", email);
        
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            // Check if user already has a restaurant
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByOwnerId(user.getId());
            if (existingRestaurant.isPresent()) {
                logger.info("User already has a restaurant: {}", existingRestaurant.get().getName());
                return ResponseEntity.ok(Map.of(
                    "message", "User already has a restaurant",
                    "restaurant", existingRestaurant.get()
                ));
            }
            
            // Create new restaurant
            Restaurant restaurant = new Restaurant();
            restaurant.setName(name);
            restaurant.setAddress(address != null ? address : name + " Address");
            restaurant.setPhone(phone != null ? phone : "555-0000");
            restaurant.setCuisineType(cuisineType != null ? cuisineType : "Various");
            restaurant.setOpeningHours(openingHours != null ? openingHours : "09:00-22:00");
            restaurant.setOwnerId(user.getId());
            restaurant.setRating(rating != null ? rating : 4.5);
            
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            logger.info("Created restaurant: {} (ID: {})", savedRestaurant.getName(), savedRestaurant.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Restaurant created successfully",
                "restaurant", savedRestaurant
            ));
        } catch (Exception e) {
            logger.error("Error creating restaurant", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/orders-by-name")
    public ResponseEntity<?> getRestaurantOrdersByName(@RequestParam(required = true) String email) {
        logger.info("Received request for restaurant orders with email: {}", email);
        
        try {
            if (email == null || email.isEmpty()) {
                logger.error("Email parameter is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Email parameter is required"));
            }
            
            // Step 1: Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            logger.info("Found user: {} (ID: {})", user.getName(), user.getId());
            
            // Step 2: Get restaurant name from user's name (assuming restaurant name is related to user name)
            String restaurantName = user.getName();
            logger.info("Using restaurant name: {}", restaurantName);
            
            // Step 3: Find restaurant by name using repository method
            List<Restaurant> restaurants = restaurantRepository.findByNameIgnoreCase(restaurantName);
                
            if (restaurants.isEmpty()) {
                // Try with partial name match if exact match fails
                restaurants = restaurantRepository.findByNameContainingIgnoreCase(restaurantName);
                
                if (restaurants.isEmpty()) {
                    logger.error("No restaurant found with name: {}", restaurantName);
                    return ResponseEntity.badRequest().body(Map.of("error", "No restaurant found with this name"));
                }
            }
            
            // Use the first matching restaurant
            Restaurant restaurant = restaurants.get(0);
            logger.info("Found restaurant: {} (ID: {})", restaurant.getName(), restaurant.getId());
            
            // Step 4: Get orders for this restaurant with detailed information
            List<Map<String, Object>> orders = orderRepository.getOrdersByRestaurantId(restaurant.getId());
            logger.info("Found {} orders for restaurant", orders.size());
            
            // For each order, fetch the order items
            for (Map<String, Object> order : orders) {
                Long orderId = ((Number) order.get("order_id")).longValue();
                List<Map<String, Object>> items = orderService.getOrderItemDetails(orderId);
                order.put("items", items);
            }
            
            return ResponseEntity.ok(Map.of("orders", orders));
        } catch (Exception e) {
            logger.error("Error processing restaurant orders request", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }

    @GetMapping("/orders-by-email")
    public ResponseEntity<?> getRestaurantOrdersByEmailDirect(@RequestParam(required = true) String email) {
        logger.info("Received request for restaurant orders with email: {}", email);
        
        try {
            if (email == null || email.isEmpty()) {
                logger.error("Email parameter is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Email parameter is required"));
            }
            
            // Step 1: Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            logger.info("Found user: {} (ID: {})", user.getName(), user.getId());
            
            // Step 2: Find restaurant directly by owner ID
            Optional<Restaurant> restaurantOpt = restaurantRepository.findByOwnerId(user.getId());
            if (restaurantOpt.isEmpty()) {
                logger.error("No restaurant found with owner ID: {}", user.getId());
                
                // For debugging, check if there's a restaurant with a matching name
                List<Restaurant> nameMatchRestaurants = restaurantRepository.findByNameIgnoreCase(user.getName());
                if (!nameMatchRestaurants.isEmpty()) {
                    logger.info("Found restaurant by name match, but owner ID doesn't match");
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Found restaurant with matching name but different owner ID",
                        "suggestion", "Update restaurant owner_id to " + user.getId()
                    ));
                }
                
                return ResponseEntity.badRequest().body(Map.of("error", "No restaurant found for this owner"));
            }
            
            Restaurant restaurant = restaurantOpt.get();
            logger.info("Found restaurant: {} (ID: {}) with owner_id: {}", 
                restaurant.getName(), restaurant.getId(), restaurant.getOwnerId());
            
            // Step 3: Get orders for this restaurant with detailed information
            List<Map<String, Object>> orders = orderRepository.getOrdersByRestaurantId(restaurant.getId());
            logger.info("Found {} orders for restaurant", orders.size());
            
            // For each order, fetch the order items
            for (Map<String, Object> order : orders) {
                Long orderId = ((Number) order.get("order_id")).longValue();
                List<Map<String, Object>> items = orderService.getOrderItemDetails(orderId);
                order.put("items", items);
            }
            
            return ResponseEntity.ok(Map.of(
                "orders", orders,
                "restaurant", Map.of(
                    "id", restaurant.getId(),
                    "name", restaurant.getName(),
                    "owner_id", restaurant.getOwnerId()
                )
            ));
        } catch (Exception e) {
            logger.error("Error processing restaurant orders request", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }

    @PostMapping("/create-if-not-exists")
    public ResponseEntity<?> createRestaurantIfNotExists(@RequestParam String email) {
        logger.info("Creating restaurant for user with email if it doesn't exist: {}", email);
        
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            // Check if user already has a restaurant
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByOwnerId(user.getId());
            if (existingRestaurant.isPresent()) {
                logger.info("User already has a restaurant: {}", existingRestaurant.get().getName());
                return ResponseEntity.ok(Map.of(
                    "message", "User already has a restaurant",
                    "restaurant", existingRestaurant.get()
                ));
            }
            
            // Create new restaurant using user's name
            Restaurant restaurant = new Restaurant();
            restaurant.setName(user.getName()); // Use user's name as restaurant name
            restaurant.setAddress(user.getAddress() != null ? user.getAddress() : "Default Address");
            restaurant.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "555-0000");
            restaurant.setCuisineType("Various");
            restaurant.setOpeningHours("09:00-22:00");
            restaurant.setOwnerId(user.getId());
            restaurant.setRating(4.5);
            
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            logger.info("Created restaurant: {} (ID: {})", savedRestaurant.getName(), savedRestaurant.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Restaurant created successfully",
                "restaurant", savedRestaurant
            ));
        } catch (Exception e) {
            logger.error("Error creating restaurant", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}