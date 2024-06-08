package com.dreamgames.backendengineeringcasestudy.dao;

import com.dreamgames.backendengineeringcasestudy.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_USER = "INSERT INTO users (username, level, coins, country) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, level = ?, coins = ?, country = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

    public int save(User user) {
        return jdbcTemplate.update(INSERT_USER, user.getUsername(), user.getLevel(), user.getCoins(),
                user.getCountry() != null ? user.getCountry().toString() : "TURKEY");
    }

    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_ALL_USERS, new UserRowMapper());
    }

    public User findById(long id) {
        return jdbcTemplate.queryForObject(SELECT_USER_BY_ID, new UserRowMapper(), id);
    }

    public int update(User user) {
        return jdbcTemplate.update(UPDATE_USER, user.getUsername(), user.getLevel(), user.getCoins(),
                user.getCountry() != null ? user.getCountry().toString() : null, user.getId());
    }

    public int delete(long id) {
        return jdbcTemplate.update(DELETE_USER, id);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setLevel(rs.getInt("level"));
            user.setCoins(rs.getInt("coins"));
            user.setCountry(User.Country.valueOf(rs.getString("country")));
            return user;
        }
    }
}
