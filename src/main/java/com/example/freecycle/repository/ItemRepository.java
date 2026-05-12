package com.example.freecycle.repository;

import com.example.freecycle.entity.Item;
import com.example.freecycle.entity.ItemState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByState(ItemState state);
    List<Item> findByDonorId(Long donorId);
}
