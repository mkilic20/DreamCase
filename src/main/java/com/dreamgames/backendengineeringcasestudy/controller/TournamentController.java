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

/**
 * REST controller for managing tournaments.
 */
@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    private static final Logger logger = LoggerFactory.getLogger(TournamentController.class);

    @Autowired
    private TournamentService tournamentService;

    /**
     * Endpoint for a user to enter a tournament.
     *
     * @param userId the ID of the user entering the tournament
     * @return the leaderboard of the tournament group the user entered
     */
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

    /**
     * Endpoint for a user to claim their reward from the last tournament.
     *
     * @param userId the ID of the user claiming the reward
     * @return the updated user information
     */
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

    /**
     * Endpoint to get the rank of a user within their recent tournament group.
     *
     * @param userId the ID of the user
     * @return the user's rank within their tournament group and user information
     */
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

    /**
     * Endpoint to get the leaderboard of a specific tournament group.
     *
     * @param groupId the ID of the tournament group
     * @return the leaderboard of the tournament group
     */
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

    /**
     * Endpoint to get the country leaderboard for a specific tournament.
     *
     * @param tournamentId the ID of the tournament
     * @return the country leaderboard of the tournament
     */
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

    /**
     * For Testing: Endpoint to end the current tournament.
     *
     * @return No response
     */
    @PostMapping("/end")
    public ResponseEntity<?> endCurrentTournament() {
        try {
            tournamentService.endTournaments();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while ending the current tournament", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * For Testing: Endpoint to start a new tournament.
     *
     * @return No response
     */
    @PostMapping("/start")
    public ResponseEntity<?> startNewTournament() {
        try {
            Tournament newTournament = tournamentService.createTournament();
            return new ResponseEntity<>(newTournament, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while starting a new tournament", e);
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
