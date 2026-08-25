package edu.ncsu.csc326.wolfcafe.repository;

import edu.ncsu.csc326.wolfcafe.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}
