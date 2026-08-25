package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;

/**
 * Service interface for tax rate management. Provides read access to the
 * current rate for all authenticated users and update access for admins only.
 */
public interface TaxService {

    /**
     * Returns the current active tax rate. If no rate has been set by an admin,
     * returns the default NC food tax rate of 2% (0.02).
     *
     * @return TaxRateDto containing the current rate as a decimal
     */
    TaxRateDto getCurrentTaxRate ();

    /**
     * Updates the tax rate to the value provided in the DTO.
     * Throws WolfCafeAPIException if the rate is negative.
     *
     * @param taxRateDto
     *            DTO containing the new rate
     * @return TaxRateDto containing the saved rate
     */
    TaxRateDto updateTaxRate ( TaxRateDto taxRateDto );
}
