package klu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import klu.model.CommunityCommission;
 
public interface CommunityCommissionRepository extends JpaRepository<CommunityCommission, Long> {
    List<CommunityCommission> findByCommunityId(Long communityId);
    CommunityCommission findByCommunityIdAndRestaurantId(Long communityId, Long restaurantId);
} 