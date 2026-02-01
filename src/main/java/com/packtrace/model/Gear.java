package com.packtrace.model;

import java.util.UUID;

public class Gear {
    private long id;
    private UUID ownerId;
    private String name;
    private String brand;
    private Integer weightGrams;
    private String websiteURL;
    private String category;

    public Gear() {}

    public Gear(long id, UUID ownerId, String name, String brand, int weightGrams, String websiteURL, String category) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.brand = brand;
        this.weightGrams = weightGrams;
        this.websiteURL = websiteURL;
        this.category = category;
    }

    public Gear(UUID ownerId, String name, String brand, int weightGrams, String websiteURL, String category) {
        this.ownerId = ownerId;
        this.name = name;
        this.brand = brand;
        this.weightGrams = weightGrams;
        this.websiteURL = websiteURL;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(int weightGrams) {
        this.weightGrams = weightGrams;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
