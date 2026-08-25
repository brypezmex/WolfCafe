package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.config.SetupDataLoader;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.IngredientRepository;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.DatabaseResetService;
import lombok.AllArgsConstructor;

/**
 * Implemented DatabaseResetService.
 */
@Service
@AllArgsConstructor
public class DatabaseResetServiceImpl implements DatabaseResetService {

    /** Logger for reset auditing */
    private static final Logger  LOGGER = LoggerFactory.getLogger( DatabaseResetServiceImpl.class );

    /** Order repository */
    private OrderRepository      orderRepository;

    /** Recipe repository */
    private RecipeRepository     recipeRepository;

    /** Inventory repository */
    private InventoryRepository  inventoryRepository;

    /** Ingredient repository */
    private IngredientRepository ingredientRepository;

    /** Item repository */
    private ItemRepository       itemRepository;

    /** Tax rate repository */
    private TaxRateRepository    taxRateRepository;

    /** User repository */
    private UserRepository       userRepository;

    /** Recreates the default roles and accounts after the wipe */
    private SetupDataLoader      setupDataLoader;

    /**
     * Deletes all application data and reseeds the defaults. Runs in a single
     * transaction so a failure part-way through leaves the database untouched.
     */
    @Override
    @Transactional
    public void resetDatabase () {
        LOGGER.info( "action=DB_RESET_START" );

        // Deletion order follows the foreign keys: referencing rows go first.
        orderRepository.deleteAll();
        recipeRepository.deleteAll();
        inventoryRepository.deleteAll();

        // Sweeps up ingredient rows orphaned by the deletes above.
        ingredientRepository.deleteAll();
        itemRepository.deleteAll();
        taxRateRepository.deleteAll();

        /*
         * Roles are shared rows, so detach each user from the users_roles join
         * table before deleting the user itself.
         */
        final List<User> users = new ArrayList<>( userRepository.findAll() );
        for ( final User user : users ) {
            user.setRoles( new ArrayList<>() );
        }
        userRepository.saveAll( users );
        userRepository.deleteAll();

        setupDataLoader.seedDefaults();

        LOGGER.info( "action=DB_RESET_COMPLETE clearedUsers={}", users.size() );
    }
}
