package edu.ncsu.csc326.wolfcafe.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemDto;
import edu.ncsu.csc326.wolfcafe.dto.PlaceOrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderItem;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import edu.ncsu.csc326.wolfcafe.service.TaxService;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of OrderService for customer orders, staff fulfillment, and
 * customer pickup.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** Order repository. */
    private final OrderRepository  orderRepository;

    /** Recipe repository - used to look up price, name, and ingredients. */
    private final RecipeRepository recipeRepository;

    /**
     * Inventory service used to check and deduct inventory on order placement.
     */
    private final InventoryService inventoryService;

    /** Tax service used to read the current tax rate. */
    private final TaxService       taxService;

    /**
     * Places a new order for the authenticated customer.
     *
     * This method: 1. validates the order, 2. resolves recipes, 3. checks
     * inventory, 4. deducts inventory, 5. computes subtotal/tax/tip/total, 6.
     * saves a PENDING order.
     *
     * @param customerUsername
     *            username of the placing customer
     * @param placeOrderDto
     *            items and tip percent
     * @return the saved OrderDto with all totals populated
     */
    @Override
    public OrderDto placeOrder ( String customerUsername, PlaceOrderDto placeOrderDto ) {
        if ( placeOrderDto.getItems() == null || placeOrderDto.getItems().isEmpty() ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Cannot place an empty order." );
        }

        if ( placeOrderDto.getTipPercent() < 0 ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Tip percent cannot be negative." );
        }

        double currentTaxRate = taxService.getCurrentTaxRate().getRate();

        Order order = new Order();
        order.setCustomerUsername( customerUsername );
        order.setPlacedAt( LocalDateTime.now() );
        order.setStatus( OrderStatus.PENDING );
        order.setTaxRate( currentTaxRate );
        order.setTipPercent( placeOrderDto.getTipPercent() );

        double subtotal = 0.0;

        /*
         * This map totals the amount of each ingredient required for the whole
         * order. Example: if the customer orders 2 lattes and each latte needs
         * 2 coffee, requiredIngredients["Coffee"] becomes 4.
         */
        Map<String, Integer> requiredIngredients = new LinkedHashMap<>();

        for ( OrderItemDto itemDto : placeOrderDto.getItems() ) {
            if ( itemDto.getQuantity() <= 0 ) {
                throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Quantity must be at least 1 for each item." );
            }

            Recipe recipe = recipeRepository.findById( itemDto.getRecipeId() ).orElseThrow(
                    () -> new ResourceNotFoundException( "Recipe not found with id " + itemDto.getRecipeId() ) );

            OrderItem orderItem = new OrderItem();
            orderItem.setRecipeId( recipe.getId() );
            orderItem.setRecipeName( recipe.getName() );
            orderItem.setPriceEach( recipe.getPrice() );
            orderItem.setQuantity( itemDto.getQuantity() );

            order.getItems().add( orderItem );
            subtotal += recipe.getPrice() * itemDto.getQuantity();

            addRecipeIngredientsToRequiredTotals( requiredIngredients, recipe, itemDto.getQuantity() );
        }

        /*
         * Only check inventory if the recipes actually have ingredients. This
         * keeps older tests with ingredient-less test recipes working, while
         * enforcing inventory for real recipes that include ingredients.
         */
        if ( !requiredIngredients.isEmpty() ) {
            checkAndDeductInventory( requiredIngredients );
        }

        double taxAmount = round2( subtotal * currentTaxRate );
        double tipAmount = round2( subtotal * ( placeOrderDto.getTipPercent() / 100.0 ) );
        double total = round2( subtotal + taxAmount + tipAmount );

        order.setSubtotal( round2( subtotal ) );
        order.setTaxAmount( taxAmount );
        order.setTipAmount( tipAmount );
        order.setTotal( total );

        Order saved = orderRepository.save( order );
        return mapToDto( saved );
    }

    /**
     * Adds one recipe's ingredients to the required ingredient totals.
     *
     * @param requiredIngredients
     *            map of ingredient name to required amount
     * @param recipe
     *            recipe being ordered
     * @param quantity
     *            number of this recipe being ordered
     */
    private void addRecipeIngredientsToRequiredTotals ( Map<String, Integer> requiredIngredients, Recipe recipe,
            int quantity ) {
        if ( recipe.getIngredients() == null || recipe.getIngredients().isEmpty() ) {
            return;
        }

        for ( Ingredient ingredient : recipe.getIngredients() ) {
            if ( ingredient.getIngredientName() == null || ingredient.getAmount() == null ) {
                continue;
            }

            int requiredAmount = ingredient.getAmount() * quantity;

            if ( requiredAmount <= 0 ) {
                continue;
            }

            requiredIngredients.merge( ingredient.getIngredientName(), requiredAmount, Integer::sum );
        }
    }

    /**
     * Checks inventory and deducts ingredients if there is enough stock.
     *
     * If any ingredient is missing or too low, no inventory is changed and the
     * order is rejected.
     *
     * @param requiredIngredients
     *            map of ingredient name to required amount
     */
    private void checkAndDeductInventory ( Map<String, Integer> requiredIngredients ) {
        InventoryDto inventory = inventoryService.getInventory();

        for ( Map.Entry<String, Integer> entry : requiredIngredients.entrySet() ) {
            String ingredientName = entry.getKey();
            int requiredAmount = entry.getValue();

            Ingredient inventoryIngredient = inventory.getIngredientByName( ingredientName );

            if ( inventoryIngredient == null ) {
                throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                        "Insufficient inventory: missing ingredient " + ingredientName + "." );
            }

            if ( inventoryIngredient.getAmount() == null || inventoryIngredient.getAmount() < requiredAmount ) {
                throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                        "Insufficient inventory for " + ingredientName + "." );
            }
        }

        for ( Map.Entry<String, Integer> entry : requiredIngredients.entrySet() ) {
            String ingredientName = entry.getKey();
            int requiredAmount = entry.getValue();

            Ingredient inventoryIngredient = inventory.getIngredientByName( ingredientName );
            inventoryIngredient.setAmount( inventoryIngredient.getAmount() - requiredAmount );
        }

        inventoryService.updateInventory( inventory );
    }

    /**
     * Advances a PENDING order to FULFILLED status.
     *
     * @param orderId
     *            the id of the order to fulfill
     * @return updated OrderDto with FULFILLED status
     */
    @Override
    public OrderDto fulfillOrder ( Long orderId ) {
        Order order = orderRepository.findById( orderId )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with id " + orderId ) );

        if ( order.getStatus() != OrderStatus.PENDING ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                    "Order " + orderId + " cannot be fulfilled because its current status is " + order.getStatus()
                            + ". Only PENDING orders can be fulfilled." );
        }

        order.setStatus( OrderStatus.FULFILLED );
        return mapToDto( orderRepository.save( order ) );
    }

    /**
     * Advances a FULFILLED order to PICKED_UP status.
     *
     * @param orderId
     *            the id of the order to pick up
     * @param customerUsername
     *            the username of the requesting customer
     * @return updated OrderDto with PICKED_UP status
     */
    @Override
    public OrderDto confirmPickup ( Long orderId, String customerUsername ) {
        Order order = orderRepository.findById( orderId )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with id " + orderId ) );

        if ( !order.getCustomerUsername().equals( customerUsername ) ) {
            throw new WolfCafeAPIException( HttpStatus.FORBIDDEN,
                    "You do not have permission to pick up order " + orderId + "." );
        }

        if ( order.getStatus() != OrderStatus.FULFILLED ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                    "Order " + orderId + " cannot be picked up because its current status is " + order.getStatus()
                            + ". Only FULFILLED orders can be picked up." );
        }

        order.setStatus( OrderStatus.PICKED_UP );
        return mapToDto( orderRepository.save( order ) );
    }

    /**
     * Returns all orders placed by the given customer.
     *
     * @param customerUsername
     *            customer's username
     * @return list of OrderDto
     */
    @Override
    public List<OrderDto> getOrdersForCustomer ( String customerUsername ) {
        return orderRepository.findByCustomerUsername( customerUsername ).stream().map( this::mapToDto )
                .collect( Collectors.toList() );
    }

    /**
     * Returns all orders in the system.
     *
     * @return list of all OrderDto
     */
    @Override
    public List<OrderDto> getAllOrders () {
        return orderRepository.findAll().stream().map( this::mapToDto ).collect( Collectors.toList() );
    }

    /**
     * Returns the order with the given id.
     *
     * @param orderId
     *            the order id
     * @return OrderDto
     */
    @Override
    public OrderDto getOrderById ( Long orderId ) {
        Order order = orderRepository.findById( orderId )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with id " + orderId ) );
        return mapToDto( order );
    }

    /**
     * Maps an Order entity to an OrderDto.
     *
     * @param order
     *            order entity
     * @return OrderDto
     */
    private OrderDto mapToDto ( Order order ) {
        List<OrderItemDto> itemDtos = new ArrayList<>();

        for ( OrderItem item : order.getItems() ) {
            itemDtos.add( new OrderItemDto( item.getRecipeId(), item.getRecipeName(), item.getPriceEach(),
                    item.getQuantity() ) );
        }

        return new OrderDto( order.getId(), order.getCustomerUsername(), itemDtos, order.getSubtotal(),
                order.getTaxRate(), order.getTaxAmount(), order.getTipPercent(), order.getTipAmount(), order.getTotal(),
                order.getStatus(), order.getPlacedAt() );
    }

    /**
     * Rounds a double to 2 decimal places.
     *
     * @param value
     *            value to round
     * @return value rounded to 2 decimal places
     */
    private double round2 ( double value ) {
        return Math.round( value * 100.0 ) / 100.0;
    }
}
