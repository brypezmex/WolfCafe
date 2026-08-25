package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Data transfer object for the current tax rate.
 * The rate is expressed as a decimal (e.g. 0.02 = 2%).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateDto {
 
    /**
     * The tax rate as a decimal (e.g. 0.02 for 2%).
     * Must be >= 0; validated in TaxServiceImpl.
     */
    private double rate;
}
