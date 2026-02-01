package com.packtrace.model;

import java.util.UUID;

public class Account {
    private UUID id;
    private String auth0Id;
    private String username;
    private String bio;

    public Account() {}

    public Account(String auth0Id, String username) {
        this.auth0Id = auth0Id;
        this.username = username;
    }

    public Account(UUID id, String auth0Id, String username, String bio) {
        this.id = id;
        this.auth0Id = auth0Id;
        this.username = username;
        this.bio = bio;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAuth0Id() {
        return auth0Id;
    }

    public void setAuth0Id(String auth0Id) {
        this.auth0Id = auth0Id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
