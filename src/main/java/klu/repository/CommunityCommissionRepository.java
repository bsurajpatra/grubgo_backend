package klu.repository;

import klu.model.CommunityCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityCommissionRepository extends JpaRepository<CommunityCommission, Long> {
    List<CommunityCommission> findByCommunityId(Long communityId);
    CommunityCommission findByCommunityIdAndRestaurantId(Long communityId, Long restaurantId);
} 