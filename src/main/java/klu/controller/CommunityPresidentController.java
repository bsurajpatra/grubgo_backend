package klu.controller;

import klu.model.CommunityCommission;
import klu.repository.CommunityCommissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/community-president")
public class CommunityPresidentController {

    @Autowired
    private CommunityCommissionRepository commissionRepository;

    @GetMapping("/commissions/{communityId}")
    public ResponseEntity<List<CommunityCommission>> getCommissions(@PathVariable Long communityId) {
        return ResponseEntity.ok(commissionRepository.findByCommunityId(communityId));
    }
    
    @PostMapping("/commissions")
    public ResponseEntity<CommunityCommission> setCommission(@RequestBody CommunityCommission commission) {
        return ResponseEntity.ok(commissionRepository.save(commission));
    }
    
    @PutMapping("/commissions/{commissionId}")
    public ResponseEntity<CommunityCommission> updateCommission(@PathVariable Long commissionId, @RequestBody CommunityCommission commission) {
        commission.setCommissionId(commissionId);
        return ResponseEntity.ok(commissionRepository.save(commission));
    }
} 