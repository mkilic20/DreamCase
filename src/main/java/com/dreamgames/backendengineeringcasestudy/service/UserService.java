package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.TournamentUser;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentUserRepository tournamentUserRepository;

    /**
     * Creates a new user with a given username.
     * If the username is not provided, it assigns a default username.
     * 
     * @param username the username of the new user (optional)
     * @return the newly created user
     */
    public User createUser(String username) {
        User newUser = new User();
        newUser.setLevel(1); // Starting level
        newUser.setCoins(5000); // Starting coins
        // Randomly assign a country
        Country[] countries = Country.values();
        newUser.setCountry(countries[new Random().nextInt(countries.length)]);
        // Set a new username if username is not provided
        if (username != null && !username.isEmpty()) {
            newUser.setUsername(username);
        }
        // Save the user to database
        User savedUser = userRepository.save(newUser);
        if (savedUser.getUsername() == null || savedUser.getUsername().isEmpty()) {
            savedUser.setUsername("username" + savedUser.getId());
            userRepository.save(savedUser);
        }
        return savedUser;
    }

    /**
     * Saves the user entity.
     * 
     * @param user the user entity to save
     * @return the saved user entity
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves all users.
     * 
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param id the ID of the user
     * @return the user entity if found, null otherwise
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Updates the user entity.
     * 
     * @param user the user entity to update
     * @return the updated user entity
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param id the ID of the user to delete
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Updates the level and coins of a user by their ID.
     * If the user is in a tournament and the competition has started, their score
     * is also updated.
     * 
     * @param userId the ID of the user to update
     * @return the updated user entity if found, null otherwise
     */
    public User updateUserLevel(Long userId) {
        User user = getUserById(userId);
        if (user != null) {
            user.setLevel(user.getLevel() + 1); // Increase the level by 1
            user.setCoins(user.getCoins() + 25); // Add 25 coins
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
