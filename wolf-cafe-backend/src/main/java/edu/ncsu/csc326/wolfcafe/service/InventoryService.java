package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;

/**
 * Interface defining the inventory behaviors.
 */
public interface InventoryService {
	
	/**
	 * Creates the inventory.
	 * @param inventoryDto inventory to create
	 * @return updated inventory after creation
	 */
	InventoryDto createInventory(InventoryDto inventoryDto);
	
	/**
	 * Returns the single inventory.
	 * @return the single inventory
	 */
	InventoryDto getInventory();
	
	/**
	 * Updates the contents of the inventory.
	 * @param inventoryDto values to update
	 * @return updated inventory
	 */
	InventoryDto updateInventory(InventoryDto inventoryDto);

}
