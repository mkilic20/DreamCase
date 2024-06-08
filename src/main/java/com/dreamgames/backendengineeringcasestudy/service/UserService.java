package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.UserDao;
import com.dreamgames.backendengineeringcasestudy.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public int saveUser(User user) {
        return userDao.save(user);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User getUserById(long id) {
        return userDao.findById(id);
    }

    public int updateUser(User user) {
        return userDao.update(user);
    }

    public int deleteUser(long id) {
        return userDao.delete(id);
    }
}
