package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.dto.CountryScore;
import com.dreamgames.backendengineeringcasestudy.dto.GroupLeaderboardEntry;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    private static final Logger logger = LoggerFactory.getLogger(TournamentController.class);

    @Autowired
    private TournamentService tournamentService;

    @PostMapping("/enter/{userId}")
    public ResponseEntity<?> enterTournament(@PathVariable Long userId) {
        try {
            List<GroupLeaderboardEntry> leaderboard = tournamentService.enterTournament(userId);
            return new ResponseEntity<>(leaderboard, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while entering tournament", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/claimReward/{userId}")
    public ResponseEntity<?> claimReward(@PathVariable Long userId) {
        try {
            User updatedUser = tournamentService.claimReward(userId);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while claiming reward", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<?> getGroupRank(@PathVariable Long userId) {
        try {
            GroupLeaderboardEntry rank = tournamentService.getGroupRank(userId);
            return new ResponseEntity<>(rank, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while getting group rank", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/groupLeaderboard/{groupId}")
    public ResponseEntity<?> getGroupLeaderboard(@PathVariable Long groupId) {
        try {
            TournamentGroup group = tournamentService.getTournamentGroupById(groupId);
            List<GroupLeaderboardEntry> leaderboard = tournamentService.getLeaderboard(group);
            return new ResponseEntity<>(leaderboard, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while getting group leaderboard", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/countryLeaderboard/{tournamentId}")
    public ResponseEntity<?> getCountryLeaderboard(@PathVariable Long tournamentId) {
        try {
            List<CountryScore> countryLeaderboard = tournamentService.getCountryLeaderboard(tournamentId);
            return new ResponseEntity<>(countryLeaderboard, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while getting country leaderboard", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/end")
    public ResponseEntity<?> endCurrentTournament() {
        try {
            tournamentService.endTournaments(); // Reuse existing method
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while ending the current tournament", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startNewTournament() {
        try {
            Tournament newTournament = tournamentService.createTournament(); // Reuse existing method
            return new ResponseEntity<>(newTournament, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while starting a new tournament", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
