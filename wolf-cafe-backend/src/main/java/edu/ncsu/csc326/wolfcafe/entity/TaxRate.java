package edu.ncsu.csc326.wolfcafe.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Persists the current sales tax rate.
 * Each time an admin updates the rate a new row is saved,
 * so the table acts as a history log. TaxRateRepository
 * retrieves the most recently saved row as the active rate.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tax_rate")
public class TaxRate {
 
    /** Tax rate record id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    /**
     * The tax rate as a decimal (e.g. 0.02 = 2%).
     * Must be >= 0.
     */
    @Column(nullable = false)
    private double rate;
 
    /** Timestamp when this rate was saved */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
