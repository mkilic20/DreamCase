package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tournament_users")
public class TournamentUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tournament_group_id")
    private TournamentGroup tournamentGroup;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int reward;

    @Column(nullable = false)
    private boolean rewardClaimed;

    public TournamentUser() {
    }

    public TournamentUser(User user, TournamentGroup tournamentGroup) {
        this.user = user;
        this.tournamentGroup = tournamentGroup;
        this.score = 0;
        this.reward = 0;
        this.rewardClaimed = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TournamentGroup getTournamentGroup() {
        return tournamentGroup;
    }

    public void setTournamentGroup(TournamentGroup tournamentGroup) {
        this.tournamentGroup = tournamentGroup;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }
}
