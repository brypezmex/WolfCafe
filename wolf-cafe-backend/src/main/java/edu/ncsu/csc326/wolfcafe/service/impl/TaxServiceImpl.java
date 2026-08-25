package edu.ncsu.csc326.wolfcafe.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
 
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxService;
import lombok.RequiredArgsConstructor;
 
/**
 * Implementation of TaxService.
 * Reads the current rate from the tax_rate table; falls back to
 * the value in application.properties if the table is empty.
 * Each admin update inserts a new row so history is preserved.
 */
@Service
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {
 
    /** Tax rate repository */
    private final TaxRateRepository taxRateRepository;
 
    /**
     * Default tax rate from application.properties (app.tax-rate=0.02).
     * Used as the fallback when no rate has been saved to the database yet.
     */
    @Value("${app.tax-rate:0.02}")
    private double defaultTaxRate;
 
    /**
     * Returns the current active tax rate.
     * Reads the most recently saved row from the database.
     * If the table is empty, returns the default from application.properties
     * so the system works without any admin action.
     *
     * @return TaxRateDto with the current decimal rate
     */
    @Override
    public TaxRateDto getCurrentTaxRate() {
        return taxRateRepository.findFirstByOrderByIdDesc()
                .map(taxRate -> new TaxRateDto(taxRate.getRate()))
                .orElse(new TaxRateDto(defaultTaxRate));
    }
 
    /**
     * Saves a new tax rate.
     * Validates that the rate is not negative before saving.
     * Inserts a new row rather than updating in place to preserve history.
     *
     * @param taxRateDto DTO with the new rate value
     * @return TaxRateDto with the persisted rate
     */
    @Override
    public TaxRateDto updateTaxRate(TaxRateDto taxRateDto) {
        // reject negative tax rates
        if (taxRateDto.getRate() < 0) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
                    "Tax rate cannot be negative. Provided value: " + taxRateDto.getRate());
        }
 
        TaxRate taxRate = new TaxRate();
        taxRate.setRate(taxRateDto.getRate());
        taxRate.setUpdatedAt(LocalDateTime.now());
 
        TaxRate saved = taxRateRepository.save(taxRate);
        return new TaxRateDto(saved.getRate());
    }
}