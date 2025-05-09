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

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;

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
}