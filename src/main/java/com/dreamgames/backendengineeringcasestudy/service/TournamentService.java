package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.CountryScore;
import com.dreamgames.backendengineeringcasestudy.dto.GroupLeaderboardEntry;
import com.dreamgames.backendengineeringcasestudy.model.*;
import com.dreamgames.backendengineeringcasestudy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing tournaments.
 */
@Service
public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentGroupRepository tournamentGroupRepository;

    @Autowired
    private TournamentUserRepository tournamentUserRepository;

    /**
     * Creates a new tournament with the current start time and a fixed end time
     * (20:00 UTC).
     * 
     * @return the newly created tournament
     */
    public Tournament createTournament() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endTime = now.withHour(20).withMinute(0).withSecond(0);

        Tournament tournament = new Tournament();
        tournament.setStartTime(now);
        tournament.setEndTime(endTime);
        tournament.setActive(true);

        return tournamentRepository.save(tournament);
    }

    /**
     * Retrieves all tournaments.
     * 
     * @return a list of all tournaments
     */
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    /**
     * Retrieves a tournament by its ID.
     * 
     * @param id the ID of the tournament
     * @return the tournament entity if found, null otherwise
     */
    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    /**
     * Updates the given tournament entity.
     * 
     * @param tournament the tournament entity to update
     * @return the updated tournament entity
     */
    public Tournament updateTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    /**
     * Ends all active tournaments and distributes rewards to the participants.
     */
    public void endTournaments() {
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();
        for (Tournament tournament : activeTournaments) {
            tournament.setActive(false);
            tournamentRepository.save(tournament);
            distributeRewards(tournament);
        }
    }

    /**
     * Distributes rewards to the participants of the given tournament.
     * 
     * @param tournament the tournament entity
     */
    protected void distributeRewards(Tournament tournament) {
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament);
        for (TournamentGroup group : groups) {
            if (group.getCompetitionStarted()) { // Only distribute rewards if the competition has started
                List<TournamentUser> participants = group.getParticipants().stream()
                        .sorted(Comparator.comparingInt(TournamentUser::getScore).reversed())
                        .collect(Collectors.toList());

                if (!participants.isEmpty()) {
                    int currentRank = 1;
                    int rewardForRank = 10000;
                    for (int i = 0; i < participants.size(); i++) {
                        TournamentUser participant = participants.get(i);

                        // Determine the appropriate reward based on the current rank
                        if (currentRank == 1) {
                            rewardForRank = 10000;
                        } else if (currentRank == 2) {
                            rewardForRank = 5000;
                        } else {
                            break; // Only top 2 ranks get rewards
                        }

                        participant.setReward(rewardForRank);
                        participant.setRewardClaimed(false);

                        if (i < participants.size() - 1) {
                            if (participants.get(i).getScore() != participants.get(i + 1).getScore()) {
                                currentRank++;
                            }
                        }

                        tournamentUserRepository.save(participant);
                    }
                }
            }
        }
    }

    /**
     * Allows a user to enter the current active tournament.
     * 
     * @param userId the ID of the user
     * @return the leaderboard of the user's tournament group
     * @throws IllegalArgumentException if the user does not meet the requirements
     */
    public List<GroupLeaderboardEntry> enterTournament(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        if (user.getLevel() < 20 || user.getCoins() < 1000) {
            throw new IllegalArgumentException("User does not meet the requirements to enter the tournament");
        }

        if (hasUnclaimedReward(user)) {
            throw new IllegalArgumentException("User has unclaimed rewards and cannot enter a new tournament");
        }

        Tournament currentTournament = getCurrentActiveTournament();

        // Check if the user is already in the current tournament
        if (tournamentGroupRepository.existsByTournamentAndParticipants_User(currentTournament, user)) {
            throw new IllegalArgumentException("User already entered the current tournament");
        }

        user.setCoins(user.getCoins() - 1000); // Deduct 1000 coins
        userRepository.save(user);

        TournamentGroup tournamentGroup = findOrCreateTournamentGroup(currentTournament, user);

        logger.info("User {} added to group {} which now has {} participants.",
                user.getId(), tournamentGroup.getId(), tournamentGroup.getParticipants().size());

        return getLeaderboard(tournamentGroup);
    }

    /**
     * Finds or creates a tournament group for the user.
     * 
     * @param tournament the tournament entity
     * @param user       the user entity
     * @return the tournament group entity
     */
    private TournamentGroup findOrCreateTournamentGroup(Tournament tournament, User user) {
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament)
                .stream()
                .sorted(Comparator.comparing(TournamentGroup::getId))
                .collect(Collectors.toList());

        for (TournamentGroup group : groups) {
            long participantCount = tournamentUserRepository.countByTournamentGroup(group);
            boolean hasSameCountry = tournamentUserRepository.existsByTournamentGroupAndUser_Country(group,
                    user.getCountry());

            if (!hasSameCountry && participantCount < 5) { // Add user to the group
                TournamentUser tournamentUser = new TournamentUser(user, group);
                tournamentUserRepository.save(tournamentUser);
                group.getParticipants().add(tournamentUser);

                if (participantCount == 4) { // Group is full
                    group.setCompetitionStarted(true);
                }

                tournamentGroupRepository.save(group);
                return group;
            }
        }

        TournamentGroup newGroup = new TournamentGroup();
        newGroup.setTournament(tournament);

        TournamentGroup savedGroup = tournamentGroupRepository.save(newGroup);

        TournamentUser tournamentUser = new TournamentUser(user, savedGroup);
        tournamentUserRepository.save(tournamentUser);
        savedGroup.getParticipants().add(tournamentUser);

        if (savedGroup.getParticipants().size() == 5) { // Group is full
            savedGroup.setCompetitionStarted(true);
        }

        tournamentGroupRepository.save(savedGroup);

        return savedGroup;
    }

    /**
     * Checks if the user has any unclaimed rewards.
     * 
     * @param user the user entity
     * @return true if the user has unclaimed rewards, false otherwise
     */
    private boolean hasUnclaimedReward(User user) {
        return tournamentUserRepository.findByUser(user)
                .stream()
                .anyMatch(tu -> tu.getReward() > 0 && !tu.isRewardClaimed());
    }

    /**
     * Retrieves the leaderboard for a given tournament group.
     * 
     * @param tournamentGroup the tournament group entity
     * @return a list of leaderboard entries
     */
    public List<GroupLeaderboardEntry> getLeaderboard(TournamentGroup tournamentGroup) {
        List<GroupLeaderboardEntry> leaderboard = tournamentGroup.getParticipants().stream()
                .sorted((u1, u2) -> u2.getScore() - u1.getScore())
                .map(participant -> new GroupLeaderboardEntry(
                        participant.getUser().getId(),
                        participant.getUser().getUsername(),
                        participant.getUser().getCountry() == null ? "Unknown"
                                : participant.getUser().getCountry().toString(),
                        participant.getScore(),
                        0 // rank will be set later
                ))
                .collect(Collectors.toList());

        // Set ranks and handle draws
        int rank = 1;
        for (int i = 0; i < leaderboard.size(); i++) {
            if (i > 0 && leaderboard.get(i).getScore() == leaderboard.get(i - 1).getScore()) {
                leaderboard.get(i).setRank(leaderboard.get(i - 1).getRank());
            } else {
                leaderboard.get(i).setRank(rank);
            }
            rank++;
        }

        return leaderboard;
    }

    /**
     * Allows a user to claim their reward from the tournament.
     * 
     * @param userId the ID of the user
     * @return the updated user entity
     * @throws IllegalArgumentException if no rewards are found or reward is already
     *                                  claimed
     */
    public User claimReward(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) { // Check if user is found
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        List<TournamentUser> tournamentUsers = tournamentUserRepository.findByUser(user)
                .stream()
                .filter(tu -> tu.getReward() > 0 && !tu.isRewardClaimed())
                .collect(Collectors.toList());

        if (tournamentUsers.isEmpty()) { // Check if user has rewards to claim
            throw new IllegalArgumentException("No rewards to claim or reward already claimed");
        }

        for (TournamentUser tournamentUser : tournamentUsers) {
            user.setCoins(user.getCoins() + tournamentUser.getReward());
            tournamentUser.setRewardClaimed(true);
            tournamentUserRepository.save(tournamentUser);
        }

        userRepository.save(user);

        return user;
    }

    /**
     * Finds the tournament group of a user for a given tournament.
     * 
     * @param user       the user entity
     * @param tournament the tournament entity
     * @return the tournament group entity, or null if not found
     */
    private TournamentGroup findUserTournamentGroup(User user, Tournament tournament) {
        logger.info("Finding tournament group for user {}", user.getId());
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament);
        for (TournamentGroup group : groups) {
            logger.info("Checking group {}", group.getId());
            for (TournamentUser participant : group.getParticipants()) {
                if (participant.getUser().equals(user)) {
                    logger.info("User {} found in group {}", user.getId(), group.getId());
                    return group;
                }
            }
        }
        logger.info("User {} not found in any group for tournament {}", user.getId(), tournament.getId());
        return null;
    }

    /**
     * Retrieves the current active tournament, creating one if no active tournament
     * exists.
     * 
     * @return the current active tournament
     * @throws IllegalArgumentException if no active tournament is found and it's
     *                                  not the appropriate time to create one
     */
    private Tournament getCurrentActiveTournament() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();
        // If system is started and there is no tournament create new one (Only for
        // beginning of the system and for exceptions)
        if (activeTournaments.isEmpty()) {
            if (now.getHour() >= 0 && now.getHour() < 20) {
                return createTournament();
            } else {
                throw new IllegalArgumentException("No active tournament found");
            }
        }
        return activeTournaments.get(0);
    }

    /**
     * Finds the current active tournament that a user is participating in.
     * 
     * @param user the user entity
     * @return the current active tournament, or null if not found
     */
    private Tournament getCurrentTournamentByUser(User user) {
        logger.info("Finding current active tournament for user {}", user.getId());
        List<TournamentUser> tournamentUsers = tournamentUserRepository.findByUser(user)
                .stream()
                .filter(tu -> tu.getTournamentGroup().getTournament().isActive())
                .collect(Collectors.toList());

        if (!tournamentUsers.isEmpty()) {
            TournamentGroup group = tournamentUsers.get(0).getTournamentGroup();
            if (group.getTournament().isActive()) {
                logger.info("Found active tournament for user {}: {}", user.getId(), group.getTournament().getId());
                return group.getTournament();
            }
        }
        logger.info("No active tournament found for user {}", user.getId());
        return null;
    }

    /**
     * Retrieves the rank of a user within their tournament group.
     * 
     * @param userId the ID of the user
     * @return the user's rank within their tournament group
     * @throws IllegalArgumentException if the user is not found or not part of any
     *                                  tournament group
     */
    public GroupLeaderboardEntry getGroupRank(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Tournament currentTournament = getCurrentTournamentByUser(user);

        if (currentTournament == null) {
            throw new IllegalArgumentException("No active tournament found for user");
        }

        TournamentGroup group = findUserTournamentGroup(user, currentTournament);
        if (group == null) {
            throw new IllegalArgumentException("User is not part of any tournament group");
        }

        List<GroupLeaderboardEntry> leaderboard = getLeaderboard(group);

        GroupLeaderboardEntry userEntry = leaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (userEntry == null) {
            throw new IllegalArgumentException("User is not found in the tournament group leaderboard");
        }

        return userEntry;
    }

    /**
     * Retrieves the country leaderboard for a specific tournament.
     * 
     * @param tournamentId the ID of the tournament
     * @return a list of country scores
     * @throws IllegalArgumentException if the tournament is not found
     */
    public List<CountryScore> getCountryLeaderboard(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament);
        Map<String, Integer> countryScores = new HashMap<>();

        for (TournamentGroup group : groups) {
            for (TournamentUser participant : group.getParticipants()) {
                String country = participant.getUser().getCountry().name();
                countryScores.put(country, countryScores.getOrDefault(country, 0) + participant.getScore());
            }
        }

        return countryScores.entrySet().stream()
                .map(entry -> new CountryScore(entry.getKey(), entry.getValue()))
                .sorted((cs1, cs2) -> cs2.getTotalScore() - cs1.getTotalScore())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a tournament group by its ID.
     * 
     * @param groupId the ID of the group
     * @return the tournament group entity
     * @throws IllegalArgumentException if the group is not found
     */
    public TournamentGroup getTournamentGroupById(Long groupId) {
        return tournamentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    /**
     * Ends all active tournaments immediately and distributes rewards.
     * This method is intended for test purposes.
     */
    public void endTournamentDirectly() {
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();
        for (Tournament tournament : activeTournaments) {
            tournament.setActive(false);
            tournamentRepository.save(tournament);
            distributeRewards(tournament);
        }
    }
}
