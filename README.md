# Dream Games Backend Engineering Case Study
### Mehmet Eren Kılıç
## Project Overview
This project is a backend system for a mobile game. The backend is built using Spring Boot with Java, and it provides a REST API to maintain the users' progress and manage a daily World Cup tournament. The system is designed to be fast and secure, enhancing the user experience.

## Features
### User Progress
1. **User Creation**: A new user begins with 5,000 coins from level 1 and is randomly assigned to one of five countries (Turkey, the United States, the United Kingdom, France, Germany).
   - Username is optional. If you want to add a username, add `{"username" : [YOUR_DESIRED_USERNAME]}` to the create user endpoint. Otherwise, the username will be `username` + [ID of the user].
2. **Level Up**: After completing each level, the user advances to the next level and receives 25 coins.
3. **Persistent Storage**: User progress (level, coins, country) is stored in a MySQL database.

### World Cup Tournament
1. **Daily Tournaments**: Tournaments run daily from 00:00 to 20:00 (UTC). A new tournament starts automatically the next day.
   - If the system is established between 00:00 and 20:00 and there is no active tournament, a new tournament will be established as soon as the system is installed.
2. **Participation Requirements**: Users must be at least level 20 and pay 1,000 coins to participate.
3. **Group Formation**: Each tournament group includes 5 users, one from each country. The competition begins when a group is complete.
4. **Score and Rewards**: Each level passed increases the user's score by 1. Users can claim rewards based on their rankings after the tournament ends.
5. **Restrictions**: Users cannot enter a new tournament if they haven't claimed their last tournament's rewards.

### Leaderboards
1. **Group Leaderboard**: Displays user ID, username, country, and tournament score, sorted by the highest to lowest scores.
2. **Country Leaderboard**: Shows the total scores contributed by users from each country, sorted by the highest to lowest total scores.

## API Endpoints
### User Endpoints
- **Create User**: `POST /users/create` (can add username in the body)
- **Update Level**: `PUT /users/updateLevel/{id}`
- **Get All Users**: `GET /users` (for testing)
- **Get User by ID**: `GET /users/{id}` (for testing)
- **Delete User**: `DELETE /users/{id}` (for testing)

### Tournament Endpoints
- **Enter Tournament**: `POST /tournaments/enter/{userId}`
- **Claim Reward**: `POST /tournaments/claimReward/{userId}`
- **Get Group Rank**: `GET /tournaments/rank/{userId}`
- **Get Group Leaderboard**: `GET /tournaments/leaderboard/group/{groupId}`
- **Get Country Leaderboard**: `GET /tournaments/leaderboard/country/{tournamentId}`
- **End Tournament**: `POST /tournaments/end` (for testing)
- **Start Tournament**: `POST /tournaments/start` (for testing)

## Design Choices
### User Progress
- **Random Country Assignment**: Ensures even distribution of users across countries.
- **MySQL Database**: Chosen for reliable data storage and ease of integration with Spring Boot.
- **Level Up Logic**: Simple coin reward system to encourage user progression.

### Tournament
- **Daily Tournaments**: Creates a regular competitive event for users.
- **Group Formation**: Ensures diversity in groups, with one user from each country.
- **Score System**: Simple and effective way to rank users based on level progression.

### Leaderboards
- **Real-Time Updates**: Ensures users see up-to-date rankings.
- **Draw Handling**: Users with the same score share the same rank.

## Database Design
### Entities
- **User**: Represents a user in the system, including fields like ID, username, level, coins, and country.
- **TournamentUser**: Represents a user's participation in a tournament.
- **TournamentGroup**: Represents a group within a tournament.
- **Tournament**: Represents a tournament event.

### Relationships
- **User and TournamentUser**: One-to-many relationship, as a user can participate in multiple tournaments.
- **TournamentGroup and TournamentUser**: One-to-many relationship, as a group can have multiple users.
- **Tournament and TournamentGroup**: One-to-many relationship, as a tournament can have multiple groups.

## Setup Instructions
### Prerequisites
- Java 17
- Docker
- Docker Compose

### Steps
1. **Clone the Repository**
   ```bash
   git clone https://github.com/mkilic20/DreamCase.git
   docker-compose up --build
