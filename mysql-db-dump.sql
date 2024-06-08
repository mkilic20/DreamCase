-- User Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    coins INT NOT NULL DEFAULT 5000,
    country ENUM('TURKEY', 'UNITED_STATES', 'UNITED_KINGDOM', 'FRANCE', 'GERMANY') NOT NULL
);


-- Tournament Table
CREATE TABLE IF NOT EXISTS tournaments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL
);

-- Tournament Group Table
CREATE TABLE IF NOT EXISTS tournament_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id)
);

-- Tournament Participant Table
CREATE TABLE IF NOT EXISTS tournament_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT DEFAULT 0,
    FOREIGN KEY (tournament_group_id) REFERENCES tournament_groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
