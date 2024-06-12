package com.dreamgames.backendengineeringcasestudy.dto;

public class CountryScore {
    private String country;
    private int totalScore;

    public CountryScore(String country, int totalScore) {
        this.country = country;
        this.totalScore = totalScore;
    }

    // Getters and setters
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
