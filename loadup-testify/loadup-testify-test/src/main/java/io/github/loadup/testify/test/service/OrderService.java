package io.github.loadup.testify.test.service;

import io.github.loadup.testify.test.model.Order;
import org.springframework.stereotype.Service;

/**
 * User service for demo - demonstrates integration testing with Testify.
 */
@Service
public class OrderService {

    /**
     * Create a new user.
     */
    public Order createOrder(String orderId, String orderName) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderName(orderName);
        return order;
    }
}
