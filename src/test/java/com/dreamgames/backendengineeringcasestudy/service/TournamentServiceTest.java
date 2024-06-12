package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.CountryScore;
import com.dreamgames.backendengineeringcasestudy.dto.GroupLeaderboardEntry;
import com.dreamgames.backendengineeringcasestudy.model.*;
import com.dreamgames.backendengineeringcasestudy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TournamentServiceTest {

    @InjectMocks
    private TournamentService tournamentService;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TournamentGroupRepository tournamentGroupRepository;

    @Mock
    private TournamentUserRepository tournamentUserRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTournament() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endTime = now.withHour(20).withMinute(0).withSecond(0);

        Tournament tournament = new Tournament();
        tournament.setStartTime(now);
        tournament.setEndTime(endTime);
        tournament.setActive(true);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        Tournament createdTournament = tournamentService.createTournament();

        assertNotNull(createdTournament);
        assertEquals(now, createdTournament.getStartTime());
        assertEquals(endTime, createdTournament.getEndTime());
        assertTrue(createdTournament.isActive());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    public void testGetAllTournaments() {
        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);

        when(tournamentRepository.findAll()).thenReturn(Arrays.asList(tournament1, tournament2));

        List<Tournament> tournaments = tournamentService.getAllTournaments();

        assertNotNull(tournaments);
        assertEquals(2, tournaments.size());
        verify(tournamentRepository, times(1)).findAll();
    }

    @Test
    public void testGetTournamentById() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        Tournament foundTournament = tournamentService.getTournamentById(1L);

        assertNotNull(foundTournament);
        assertEquals(1L, foundTournament.getId());
        verify(tournamentRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateTournament() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);

        when(tournamentRepository.save(tournament)).thenReturn(tournament);

        Tournament updatedTournament = tournamentService.updateTournament(tournament);

        assertNotNull(updatedTournament);
        assertEquals(1L, updatedTournament.getId());
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    public void testEndTournaments() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setActive(true);

        when(tournamentRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(tournament));

        doAnswer(invocation -> {
            Tournament arg = invocation.getArgument(0);
            arg.setActive(false);
            return null;
        }).when(tournamentRepository).save(any(Tournament.class));

        tournamentService.endTournaments();

        assertFalse(tournament.isActive());
        verify(tournamentRepository, times(1)).findByIsActiveTrue();
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    public void testDistributeRewards() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);

        TournamentGroup group = new TournamentGroup();
        group.setId(1L);
        group.setTournament(tournament);
        group.setCompetitionStarted(true);

        TournamentUser user1 = new TournamentUser();
        User actualUser1 = new User();
        actualUser1.setId(1L);
        user1.setUser(actualUser1);
        user1.setScore(10);

        TournamentUser user2 = new TournamentUser();
        User actualUser2 = new User();
        actualUser2.setId(2L);
        user2.setUser(actualUser2);
        user2.setScore(8);

        group.setParticipants(Arrays.asList(user1, user2));

        when(tournamentGroupRepository.findByTournament(tournament)).thenReturn(Collections.singletonList(group));

        // Call the method under test
        tournamentService.distributeRewards(tournament);

        // Check the rewards
        assertEquals(10000, user1.getReward());
        assertEquals(5000, user2.getReward());
        verify(tournamentUserRepository, times(1)).save(user1);
        verify(tournamentUserRepository, times(1)).save(user2);
    }

    @Test
    public void testEnterTournament() {
        User user = new User();
        user.setId(1L);
        user.setLevel(21);
        user.setCoins(2000);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setActive(true);

        TournamentGroup group = new TournamentGroup();
        group.setId(1L);
        group.setTournament(tournament);

        TournamentUser tournamentUser = new TournamentUser(user, group);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(tournament));
        when(tournamentGroupRepository.existsByTournamentAndParticipants_User(tournament, user)).thenReturn(false);
        when(tournamentGroupRepository.findByTournament(tournament)).thenReturn(Collections.singletonList(group));
        when(tournamentUserRepository.save(any(TournamentUser.class))).thenReturn(tournamentUser);
        when(userRepository.save(user)).thenReturn(user);

        List<GroupLeaderboardEntry> leaderboard = tournamentService.enterTournament(1L);

        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());
        assertEquals(user.getId(), leaderboard.get(0).getUserId());
        verify(userRepository, times(1)).findById(1L);
        verify(tournamentRepository, times(1)).findByIsActiveTrue();
        verify(tournamentGroupRepository, times(1)).existsByTournamentAndParticipants_User(tournament, user);
        verify(tournamentGroupRepository, times(1)).findByTournament(tournament);
        verify(tournamentUserRepository, times(1)).save(any(TournamentUser.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testClaimReward() {
        User user = new User();
        user.setId(1L);
        user.setCoins(5000);

        TournamentUser tournamentUser = new TournamentUser();
        tournamentUser.setUser(user);
        tournamentUser.setReward(10000);
        tournamentUser.setRewardClaimed(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentUserRepository.findByUser(user)).thenReturn(Collections.singletonList(tournamentUser));
        when(userRepository.save(user)).thenReturn(user);
        when(tournamentUserRepository.save(tournamentUser)).thenReturn(tournamentUser);

        User rewardedUser = tournamentService.claimReward(1L);

        assertNotNull(rewardedUser);
        assertEquals(15000, rewardedUser.getCoins());
        assertTrue(tournamentUser.isRewardClaimed());
        verify(userRepository, times(1)).findById(1L);
        verify(tournamentUserRepository, times(1)).findByUser(user);
        verify(userRepository, times(1)).save(user);
        verify(tournamentUserRepository, times(1)).save(tournamentUser);
    }

    @Test
    public void testGetCountryLeaderboard() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);

        TournamentGroup group = new TournamentGroup();
        group.setId(1L);
        group.setTournament(tournament);

        TournamentUser user1 = new TournamentUser();
        user1.setUser(new User());
        user1.getUser().setCountry(User.Country.TURKEY);
        user1.setScore(10);

        TournamentUser user2 = new TournamentUser();
        user2.setUser(new User());
        user2.getUser().setCountry(User.Country.UNITED_STATES);
        user2.setScore(8);

        group.setParticipants(Arrays.asList(user1, user2));

        when(tournamentGroupRepository.findByTournament(tournament)).thenReturn(Collections.singletonList(group));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        List<CountryScore> leaderboard = tournamentService.getCountryLeaderboard(1L);

        assertNotNull(leaderboard);
        assertEquals(2, leaderboard.size());
        assertEquals("TURKEY", leaderboard.get(0).getCountry());
        assertEquals(10, leaderboard.get(0).getTotalScore());
        assertEquals("UNITED_STATES", leaderboard.get(1).getCountry());
        assertEquals(8, leaderboard.get(1).getTotalScore());
        verify(tournamentRepository, times(1)).findById(1L);
        verify(tournamentGroupRepository, times(1)).findByTournament(tournament);
    }

    @Test
    public void testGetGroupRank() {
        User user = new User();
        user.setId(1L);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setActive(true);

        TournamentGroup group = new TournamentGroup();
        group.setId(1L);
        group.setTournament(tournament);

        TournamentUser tournamentUser = new TournamentUser(user, group);
        tournamentUser.setScore(10);

        group.setParticipants(Collections.singletonList(tournamentUser));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentUserRepository.findByUser(user)).thenReturn(Collections.singletonList(tournamentUser));
        when(tournamentGroupRepository.findByTournament(tournament)).thenReturn(Collections.singletonList(group));
        when(tournamentRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(tournament));

        GroupLeaderboardEntry rank = tournamentService.getGroupRank(1L);

        assertNotNull(rank);
        assertEquals(1L, rank.getUserId());
        assertEquals(10, rank.getScore());
    }
}
