package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.TournamentUser;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentUserRepository extends JpaRepository<TournamentUser, Long> {
    long countByTournamentGroup(TournamentGroup group);

    List<TournamentUser> findByUser(User user);

    List<TournamentUser> findByTournamentGroup(TournamentGroup tournamentGroup);

    boolean existsByTournamentGroupAndUser_Country(TournamentGroup group, Country country);

    boolean existsByTournamentGroup_TournamentAndUser(Tournament tournament, User user);
}
