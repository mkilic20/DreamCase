package com.dreamgames.backendengineeringcasestudy.model;

public class User {

    private Long id;
    private String username;
    private int level;
    private int coins;
    private Country country;

    public enum Country {
        TURKEY, UNITED_STATES, UNITED_KINGDOM, FRANCE, GERMANY
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
