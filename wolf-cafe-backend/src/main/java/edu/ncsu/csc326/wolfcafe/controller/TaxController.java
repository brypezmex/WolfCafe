package edu.ncsu.csc326.wolfcafe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.service.TaxService;
import lombok.AllArgsConstructor;
 
/**
 * REST controller for tax rate management.
 *
 * Base path: /api/tax
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/tax")
@AllArgsConstructor
public class TaxController {
 
    /** Link to TaxService */
    private TaxService taxService;
 
    /**
     * Returns the current active tax rate.
     * All authenticated roles can read the rate so that the
     * order placement page can display the correct tax to the customer.
     *
     * GET /api/tax
     * Role required: any authenticated user
     *
     * @return TaxRateDto with the current rate
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @GetMapping
    public ResponseEntity<TaxRateDto> getCurrentTaxRate() {
        TaxRateDto rate = taxService.getCurrentTaxRate();
        return ResponseEntity.ok(rate);
    }
 
    /**
     * Updates the tax rate.
     * Returns 400 if the rate is negative.
     * Spring Security returns 403 for non-admin callers.
     *
     * PUT /api/tax
     * Role required: ADMIN
     *
     * @param taxRateDto DTO containing the new rate value
     * @return updated TaxRateDto
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<TaxRateDto> updateTaxRate(@RequestBody TaxRateDto taxRateDto) {
        TaxRateDto updated = taxService.updateTaxRate(taxRateDto);
        return ResponseEntity.ok(updated);
    }
}