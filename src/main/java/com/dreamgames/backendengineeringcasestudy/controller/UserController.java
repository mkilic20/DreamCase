package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public User createUser() {
        User newUser = new User();
        newUser.setLevel(1); // Starting level
        newUser.setCoins(5000); // Starting coins

        // Randomly assign a country
        User.Country[] countries = User.Country.values();
        newUser.setCountry(countries[new Random().nextInt(countries.length)]);

        return userService.saveUser(newUser);
    }

    @PutMapping("/updateLevel/{id}")
    public User updateLevel(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setLevel(user.getLevel() + 1);
            user.setCoins(user.getCoins() + 25);
            return userService.updateUser(user);
        }
        return null;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
