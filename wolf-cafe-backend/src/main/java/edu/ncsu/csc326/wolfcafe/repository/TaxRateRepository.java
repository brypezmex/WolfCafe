package edu.ncsu.csc326.wolfcafe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
 
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
 
/**
 * Repository interface for TaxRate.
 * Uses the most recently inserted row as the current active rate.
 */
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
 
    /**
     * Returns the most recently saved tax rate record.
     * Spring Data generates the query from the method name:
     * find the first row ordered by id descending.
     *
     * @return Optional containing the latest TaxRate, empty if table is empty
     */
    Optional<TaxRate> findFirstByOrderByIdDesc();
}