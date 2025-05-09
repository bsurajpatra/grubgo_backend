package klu.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import klu.model.Restaurant;
import klu.repository.RestaurantRepository;

@Service
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public List<Restaurant> findByAddress(String address) {
        return restaurantRepository.findByAddress(address);
    }

    public List<Restaurant> findByAddressContaining(String addressKeyword) {
        return restaurantRepository.findByAddressContaining(addressKeyword);
    }
}