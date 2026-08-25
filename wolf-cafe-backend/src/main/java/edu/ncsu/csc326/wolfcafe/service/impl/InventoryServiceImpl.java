package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;

/**
 * Implementation of the InventoryService interface.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryRepository inventoryRepository;

    /** Screens user-supplied text for profanity */
    @Autowired
    private ProfanityFilterService profanityFilterService;

    /**
     * Creates the inventory.
     *
     * @param inventoryDto inventory to create
     * @return updated inventory after creation
     */
    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        validateLanguage(inventoryDto);
        Inventory inventory = mapToInventory(inventoryDto);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToInventoryDto(savedInventory);
    }
 
    /**
     * Returns the single inventory. Creates one if none exists.
     *
     * @return the single inventory
     */
    @Override
    public InventoryDto getInventory() {
        return mapToInventoryDto(findOrCreateInventory());
    }
 
    /**
     * Updates the contents of the inventory.
     *
     * @param inventoryDto values to update
     * @return updated inventory
     */
    @Override
    public InventoryDto updateInventory(InventoryDto inventoryDto) {
        validateLanguage(inventoryDto);

        Inventory inventory = findOrCreateInventory();
        inventory.setIngredients(inventoryDto.getIngredients() == null
                ? new ArrayList<Ingredient>()
                : inventoryDto.getIngredients());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToInventoryDto(savedInventory);
    }
 
    /**
     * Returns the single inventory row, creating an empty one if the table is
     * empty.
     *
     * The row is located by querying for it rather than by assuming a fixed id:
     * ids are database-generated, so a row that is deleted and recreated (by a
     * database reset, say) comes back with a different id.
     *
     * @return the single inventory entity
     */
    private Inventory findOrCreateInventory() {
        List<Inventory> inventories = inventoryRepository.findAll();
        if (!inventories.isEmpty()) {
            return inventories.get(0);
        }
        return inventoryRepository.save(new Inventory());
    }
 
    /**
     * Screens every ingredient name in the inventory for profanity.
     *
     * @param inventoryDto the inventory whose ingredient names should be screened
     */
    private void validateLanguage(InventoryDto inventoryDto) {
        if (inventoryDto.getIngredients() == null) {
            return;
        }
        for (Ingredient ingredient : inventoryDto.getIngredients()) {
            profanityFilterService.validate("Ingredient name", ingredient.getIngredientName());
        }
    }
 
    /**
     * Maps an Inventory entity to an InventoryDto.
     *
     * @param inventory the entity to map
     * @return the mapped InventoryDto
     */
    private InventoryDto mapToInventoryDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto(inventory.getId());
        for (Ingredient ingredient : inventory.getIngredients()) {
            dto.addIngredient(ingredient);
        }
        return dto;
    }
 
    /**
     * Maps an InventoryDto to an Inventory entity.
     *
     * @param inventoryDto the DTO to map
     * @return the mapped Inventory entity
     */
    private Inventory mapToInventory(InventoryDto inventoryDto) {
        Inventory inventory = new Inventory(inventoryDto.getId());
        for (Ingredient ingredient : inventoryDto.getIngredients()) {
            inventory.addIngredient(ingredient);
        }
        return inventory;
    }

}
