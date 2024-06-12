package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.TournamentUser;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TournamentUserRepository tournamentUserRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setId(1L);
        user.setCoins(5000);
        user.setLevel(1);
        user.setCountry(Country.values()[new Random().nextInt(Country.values().length)]);
        user.setUsername("TestUser");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser("TestUser");

        assertNotNull(createdUser);
        assertEquals(1, createdUser.getLevel());
        assertEquals(5000, createdUser.getCoins());
        assertNotNull(createdUser.getCountry());
        assertEquals("TestUser", createdUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUserLevel() {
        User user = new User();
        user.setId(1L);
        user.setLevel(100);
        user.setCoins(100000);

        TournamentUser tournamentUser = new TournamentUser();
        tournamentUser.setUser(user);
        TournamentGroup tournamentGroup = new TournamentGroup();
        tournamentGroup.setCompetitionStarted(true);

        Tournament tournament = new Tournament();
        tournament.setActive(true);
        tournamentGroup.setTournament(tournament);

        tournamentUser.setTournamentGroup(tournamentGroup);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentUserRepository.findByUser(user)).thenReturn(Arrays.asList(tournamentUser));
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userService.updateUserLevel(1L);

        assertNotNull(updatedUser);
        assertEquals(101, updatedUser.getLevel());
        assertEquals(100025, updatedUser.getCoins());
        verify(userRepository, times(1)).findById(1L);
        verify(tournamentUserRepository, times(1)).findByUser(user);
        verify(userRepository, times(1)).save(user);
        verify(tournamentUserRepository, times(1)).save(tournamentUser);
    }
}
