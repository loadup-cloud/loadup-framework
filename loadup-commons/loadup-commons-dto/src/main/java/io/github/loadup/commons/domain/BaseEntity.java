package io.github.loadup.commons.domain;

/** Base type for domain entities with a stable identity. */
public abstract class BaseEntity {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
