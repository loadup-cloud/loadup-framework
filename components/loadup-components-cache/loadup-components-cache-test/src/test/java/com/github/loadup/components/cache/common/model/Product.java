/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.common.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Test Product entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String     id;
    private String     name;
    private String     description;
    private BigDecimal price;
    private Integer    stock;
    private String     category;

    public static Product createTestProduct(String id) {
        return Product.builder()
            .id(id)
            .name("Product" + id)
            .description("Test product " + id)
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("Electronics")
            .build();
    }
}

