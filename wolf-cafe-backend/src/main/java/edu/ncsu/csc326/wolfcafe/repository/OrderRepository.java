package edu.ncsu.csc326.wolfcafe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;

/**
 * Repository interface for Orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns all orders placed by the given customer username.
     *
     * @param customerUsername the customer's username
     * @return list of orders for that customer
     */
    List<Order> findByCustomerUsername(String customerUsername);

    /**
     * Returns all orders with the given status.
     *
     * @param status order status to filter by
     * @return list of orders with that status
     */
    List<Order> findByStatus(OrderStatus status);
}
