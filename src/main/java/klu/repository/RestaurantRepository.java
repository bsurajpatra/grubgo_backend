package klu.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import klu.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByAddressContaining(String addressKeyword);
    List<Restaurant> findByAddress(String address);
}