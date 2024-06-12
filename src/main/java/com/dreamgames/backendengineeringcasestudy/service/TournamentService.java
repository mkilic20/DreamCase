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

    public Tournament createTournament() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endTime = now.withHour(23).withMinute(0).withSecond(0);

        Tournament tournament = new Tournament();
        tournament.setStartTime(now);
        tournament.setEndTime(endTime);
        tournament.setActive(true);

        return tournamentRepository.save(tournament);
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    public Tournament updateTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public void endTournaments() {
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();
        for (Tournament tournament : activeTournaments) {
            tournament.setActive(false);
            tournamentRepository.save(tournament);
            distributeRewards(tournament);
        }
    }

    private void distributeRewards(Tournament tournament) {
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament);
        for (TournamentGroup group : groups) {
            if (group.getCompetitionStarted()) {
                List<TournamentUser> participants = group.getParticipants().stream()
                        .sorted(Comparator.comparingInt(TournamentUser::getScore).reversed())
                        .collect(Collectors.toList());

                if (!participants.isEmpty()) {
                    participants.get(0).setReward(10000); // First place
                    participants.get(0).setRewardClaimed(false);
                    if (participants.size() > 1) {
                        participants.get(1).setReward(5000); // Second place
                        participants.get(1).setRewardClaimed(false);
                    }
                }

                tournamentUserRepository.saveAll(participants);
            }
        }
    }

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

        user.setCoins(user.getCoins() - 1000);
        userRepository.save(user);

        TournamentGroup tournamentGroup = findOrCreateTournamentGroup(currentTournament, user);

        logger.info("User {} added to group {} which now has {} participants.",
                user.getId(), tournamentGroup.getId(), tournamentGroup.getParticipants().size());

        return getLeaderboard(tournamentGroup);
    }

    private TournamentGroup findOrCreateTournamentGroup(Tournament tournament, User user) {
        List<TournamentGroup> groups = tournamentGroupRepository.findByTournament(tournament)
                .stream()
                .sorted(Comparator.comparing(TournamentGroup::getId))
                .collect(Collectors.toList());

        for (TournamentGroup group : groups) {
            long participantCount = tournamentUserRepository.countByTournamentGroup(group);
            boolean hasSameCountry = tournamentUserRepository.existsByTournamentGroupAndUser_Country(group,
                    user.getCountry());

            if (!hasSameCountry && participantCount < 5) {
                TournamentUser tournamentUser = new TournamentUser(user, group);
                tournamentUserRepository.save(tournamentUser);
                group.getParticipants().add(tournamentUser);

                if (participantCount == 4) {
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

        if (savedGroup.getParticipants().size() == 5) {
            savedGroup.setCompetitionStarted(true);
        }

        tournamentGroupRepository.save(savedGroup);

        return savedGroup;
    }

    private boolean hasUnclaimedReward(User user) {
        return tournamentUserRepository.findByUser(user)
                .stream()
                .anyMatch(tu -> tu.getReward() > 0 && !tu.isRewardClaimed());
    }

    public List<GroupLeaderboardEntry> getLeaderboard(TournamentGroup tournamentGroup) {
        List<GroupLeaderboardEntry> leaderboard = tournamentGroup.getParticipants().stream()
                .sorted((u1, u2) -> u2.getScore() - u1.getScore())
                .map(participant -> new GroupLeaderboardEntry(
                        participant.getUser().getId(),
                        participant.getUser().getUsername(),
                        participant.getUser().getCountry().toString(),
                        participant.getScore(),
                        0 // rank will be set later
                ))
                .collect(Collectors.toList());

        // Set ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        return leaderboard;
    }

    public User claimReward(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        List<TournamentUser> tournamentUsers = tournamentUserRepository.findByUser(user)
                .stream()
                .filter(tu -> tu.getReward() > 0 && !tu.isRewardClaimed())
                .collect(Collectors.toList());

        if (tournamentUsers.isEmpty()) {
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

    private Tournament getCurrentActiveTournament() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();

        if (activeTournaments.isEmpty()) {
            if (now.getHour() >= 0 && now.getHour() < 23) {
                return createTournament();
            } else {
                throw new IllegalArgumentException("No active tournament found");
            }
        }
        return activeTournaments.get(0);
    }

    private Tournament getLastTournamentByUser(User user) {
        logger.info("Finding last tournament for user {}", user.getId());
        List<TournamentUser> tournamentUsers = tournamentUserRepository.findByUser(user)
                .stream()
                .filter(tu -> !tu.isRewardClaimed())
                .collect(Collectors.toList());

        if (!tournamentUsers.isEmpty()) {
            TournamentGroup group = tournamentUsers.get(0).getTournamentGroup();
            if (!group.getTournament().isActive()) {
                logger.info("Found inactive tournament for user {}: {}", user.getId(), group.getTournament().getId());
                return group.getTournament();
            }
        }
        logger.info("No inactive tournament found for user {}", user.getId());
        return null;
    }

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

        int rank = leaderboard.stream()
                .map(GroupLeaderboardEntry::getUserId)
                .collect(Collectors.toList())
                .indexOf(userId) + 1;

        GroupLeaderboardEntry userEntry = leaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (userEntry != null) {
            userEntry.setRank(rank);
        }

        return userEntry;
    }

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

    public TournamentGroup getTournamentGroupById(Long groupId) {
        return tournamentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    // For Test Purposes: End Tournament Directly
    public void endTournamentDirectly() {
        List<Tournament> activeTournaments = tournamentRepository.findByIsActiveTrue();
        for (Tournament tournament : activeTournaments) {
            tournament.setActive(false);
            tournamentRepository.save(tournament);
            distributeRewards(tournament);
        }
    }
}
