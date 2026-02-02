package com.packtrace.model;

import java.util.UUID;

public class Closet {
    private Long id;
    private UUID accountId;
    private String name;
    private String description;

    public Closet() {}

    public Closet(UUID accountId, String name, String description) {
        this.accountId = accountId;
        this.name = name;
        this.description = description;
    }

    public Closet(Long id, UUID accountId, String name, String description) {
        this.id = id;
        this.accountId = accountId;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
