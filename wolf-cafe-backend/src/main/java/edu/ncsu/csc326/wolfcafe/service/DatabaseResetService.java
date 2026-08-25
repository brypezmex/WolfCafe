package edu.ncsu.csc326.wolfcafe.service;

/**
 * Clears all application data and restores the default roles and accounts.
 * Used by the nightly scheduled reset so the deployed demo always starts each
 * day from a known state.
 */
public interface DatabaseResetService {

    /**
     * Deletes all orders, recipes, items, ingredients, inventory, tax rates,
     * and user accounts, then recreates the default roles and the default
     * admin, staff, and customer users.
     */
    void resetDatabase ();
}
