package com.dreamgames.backendengineeringcasestudy.dto;

public class GroupLeaderboardEntry {
    private Long userId;
    private String username;
    private String country;
    private int score;
    private int rank; // Add rank field

    public GroupLeaderboardEntry(Long userId, String username, String country, int score, int rank) {
        this.userId = userId;
        this.username = username;
        this.country = country;
        this.score = score;
        this.rank = rank;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
