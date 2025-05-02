package klu.controller;

import klu.model.MenuItem;
import klu.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurant-owner")
public class RestaurantOwnerController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping("/menu/{restaurantId}")
    public ResponseEntity<List<MenuItem>> getMenuItems(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemRepository.findByRestaurantId(restaurantId));
    }
    
    @PostMapping("/menu")
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(menuItemRepository.save(menuItem));
    }
    
    @PutMapping("/menu/{itemId}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long itemId, @RequestBody MenuItem menuItem) {
        menuItem.setItemId(itemId);
        return ResponseEntity.ok(menuItemRepository.save(menuItem));
    }
    
    @DeleteMapping("/menu/{itemId}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long itemId) {
        menuItemRepository.deleteById(itemId);
        return ResponseEntity.ok().build();
    }
} 