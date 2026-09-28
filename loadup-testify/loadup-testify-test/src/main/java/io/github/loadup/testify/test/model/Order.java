package io.github.loadup.testify.test.model;

/**
 * User entity for demo.
 */
public class Order {
    private String orderId;
    private String orderName;

    public String getOrderId() {
        return orderId;
    }

    public Order setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getOrderName() {
        return orderName;
    }

    public Order setOrderName(String orderName) {
        this.orderName = orderName;
        return this;
    }
}
