package com.example.freecycle.repository;

import com.example.freecycle.entity.InterestStatus;
import com.example.freecycle.entity.ItemInterest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemInterestRepository extends JpaRepository<ItemInterest, Long> {
    List<ItemInterest> findByItemId(Long itemId);
    List<ItemInterest> findByUserId(Long userId);
    Optional<ItemInterest> findByItemIdAndUserId(Long itemId, Long userId);
    Optional<ItemInterest> findByItemIdAndStatus(Long itemId, InterestStatus status);
    void deleteByItemId(Long itemId);
}
