package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament_groups")
public class TournamentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @OneToMany(mappedBy = "tournamentGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentUser> participants = new ArrayList<>();

    private boolean competitionStarted;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public List<TournamentUser> getParticipants() {
        return participants;
    }

    public void setParticipants(List<TournamentUser> participants) {
        this.participants = participants;
    }

    public boolean getCompetitionStarted() {
        return competitionStarted;
    }

    public void setCompetitionStarted(boolean competitionStarted) {
        this.competitionStarted = competitionStarted;
    }
}
