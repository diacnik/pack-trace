package com.packtrace.dto;

import java.util.UUID;

public class AccountResponse {
    private UUID id;
    private String username;
    private String bio;

    public AccountResponse() {}

    public AccountResponse(UUID id, String username, String bio) {
        this.id = id;
        this.username = username;
        this.bio = bio;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

