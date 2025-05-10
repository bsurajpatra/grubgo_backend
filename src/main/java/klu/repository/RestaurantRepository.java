package klu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import klu.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByAddressContaining(String addressKeyword);
    List<Restaurant> findByAddress(String address);
    
    @Query("SELECT r FROM Restaurant r JOIN klu.model.User u ON r.ownerId = u.id WHERE u.email = :email")
    Optional<Restaurant> findByOwnerEmail(@Param("email") String email);
    
    @Query(value = "SELECT r.* FROM restaurants r INNER JOIN users u ON r.owner_id = u.id WHERE u.email = :email", nativeQuery = true)
    Optional<Restaurant> findByOwnerEmailNative(@Param("email") String email);
    
    // Direct finder method by owner ID
    Optional<Restaurant> findByOwnerId(Long ownerId);
}