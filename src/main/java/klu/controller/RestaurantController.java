package klu.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import klu.model.Restaurant;
import klu.service.RestaurantService;
import org.springframework.web.bind.annotation.PathVariable;
import klu.model.MenuItem;
import klu.repository.MenuItemRepository;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemRepository menuItemRepository;

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
}