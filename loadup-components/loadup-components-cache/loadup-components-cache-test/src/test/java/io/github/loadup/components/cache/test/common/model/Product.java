package io.github.loadup.components.cache.test.common.model;

/*-
 * #%L
 * loadup-components-cache-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Test Product entity
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;

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

    public Product(String id, String name, String description, BigDecimal price, Integer stock, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public Product() {}

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Integer getStock() {
        return this.stock;
    }

    public String getCategory() {
        return this.category;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, description, price, stock, category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product other = (Product) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(name, other.name)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(price, other.price)) return false;
        if (!java.util.Objects.equals(stock, other.stock)) return false;
        if (!java.util.Objects.equals(category, other.category)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Product(" + "id=" + id + ", " + "name=" + name + ", " + "description=" + description + ", " + "price="
                + price + ", " + "stock=" + stock + ", " + "category=" + category + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private String category;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Product build() {
            return new Product(this.id, this.name, this.description, this.price, this.stock, this.category);
        }
    }
}
