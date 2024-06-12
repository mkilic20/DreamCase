package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.TournamentUser;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentUserRepository tournamentUserRepository;

    public User createUser(String username) {
        User newUser = new User();
        newUser.setLevel(100); // Starting level
        newUser.setCoins(100000); // Starting coins

        // Randomly assign a country
        User.Country[] countries = User.Country.values();
        newUser.setCountry(countries[new Random().nextInt(countries.length)]);

        if (username != null && !username.isEmpty()) {
            newUser.setUsername(username);
        }

        User savedUser = userRepository.save(newUser);
        if (savedUser.getUsername() == null || savedUser.getUsername().isEmpty()) {
            savedUser.setUsername("username" + savedUser.getId());
            userRepository.save(savedUser);
        }
        return savedUser;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUserLevel(Long userId) {
        User user = getUserById(userId);
        if (user != null) {
            user.setLevel(user.getLevel() + 1);
            user.setCoins(user.getCoins() + 25);

            // Check if the user is in a tournament and the competition has started
            List<TournamentUser> tournamentUsers = tournamentUserRepository.findByUser(user);
            for (TournamentUser tournamentUser : tournamentUsers) {
                TournamentGroup tournamentGroup = tournamentUser.getTournamentGroup();
                if (tournamentGroup.getCompetitionStarted() && tournamentGroup.getTournament().isActive()) {
                    tournamentUser.setScore(tournamentUser.getScore() + 1);
                    tournamentUserRepository.save(tournamentUser);
                }
            }

            return userRepository.save(user);
        }
        return null;
    }

}
