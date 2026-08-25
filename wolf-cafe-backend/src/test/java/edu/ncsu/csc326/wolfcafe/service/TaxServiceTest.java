package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
 
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
 
/**
 * Unit tests for TaxServiceImpl.
 */
@SpringBootTest
public class TaxServiceTest {
 
    @Autowired
    private TaxService taxService;
 
    @Autowired
    private TaxRateRepository taxRateRepository;
 
    /** Default rate from application.properties */
    @Value("${app.tax-rate:0.02}")
    private double defaultTaxRate;
 
    /**
     * Clear all saved tax rates before each test so tests are independent.
     */
    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
    }
    
 
    /**
     * When no rate has been saved by an admin, the service returns
     * the default from application.properties.
     */
    @Test
    public void testGetCurrentTaxRateReturnsDefaultWhenTableIsEmpty() {
        TaxRateDto rate = taxService.getCurrentTaxRate();
        assertEquals(defaultTaxRate, rate.getRate(), 0.0001,
                "Should return the default rate when no rate has been saved.");
    }
 
    /**
     * After an admin saves a rate, getCurrentTaxRate returns that rate.
     */
    @Test
    @Transactional
    public void testGetCurrentTaxRateReturnsLatestSavedRate() {
        taxService.updateTaxRate(new TaxRateDto(0.05));
 
        TaxRateDto rate = taxService.getCurrentTaxRate();
        assertEquals(0.05, rate.getRate(), 0.0001);
    }
 
    /**
     * If the admin saves multiple rates, the most recent one is returned.
     */
    @Test
    @Transactional
    public void testGetCurrentTaxRateReturnsLatestAmongMultiple() {
        taxService.updateTaxRate(new TaxRateDto(0.03));
        taxService.updateTaxRate(new TaxRateDto(0.045));
 
        TaxRateDto rate = taxService.getCurrentTaxRate();
        assertEquals(0.045, rate.getRate(), 0.0001);
    }
 
 
    /**
     * Admin successfully updates the tax rate to a valid value.
     */
    @Test
    @Transactional
    public void testUpdateTaxRateSuccess() {
        TaxRateDto result = taxService.updateTaxRate(new TaxRateDto(0.045));
 
        assertEquals(0.045, result.getRate(), 0.0001);
        assertEquals(0.045, taxService.getCurrentTaxRate().getRate(), 0.0001);
    }
 
    /**
     * A tax rate of exactly 0.0 is valid (no tax).
     */
    @Test
    @Transactional
    public void testUpdateTaxRateZeroIsValid() {
        TaxRateDto result = taxService.updateTaxRate(new TaxRateDto(0.0));
        assertEquals(0.0, result.getRate(), 0.0001);
    }
 
 
    /**
     * Admin enters a negative rate — must be rejected.
     */
    @Test
    public void testUpdateTaxRateNegativeThrows() {
        assertThrows(WolfCafeAPIException.class,
                () -> taxService.updateTaxRate(new TaxRateDto(-1.5)));
    }
 
    /**
     * After a rejected update, the rate is unchanged.
     */
    @Test
    @Transactional
    public void testUpdateTaxRateNegativeDoesNotChangeCurrentRate() {
        taxService.updateTaxRate(new TaxRateDto(0.03));
 
        assertThrows(WolfCafeAPIException.class,
                () -> taxService.updateTaxRate(new TaxRateDto(-0.01)));
 
        // Rate must still be the previously valid value
        assertEquals(0.03, taxService.getCurrentTaxRate().getRate(), 0.0001);
    }
}