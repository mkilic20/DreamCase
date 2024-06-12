package com.dreamgames.backendengineeringcasestudy.scheduler;

import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TournamentScheduler {

    @Autowired
    private TournamentService tournamentService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void createDailyTournament() {
        tournamentService.createTournament();
    }

    @Scheduled(cron = "0 0 23 * * ?", zone = "UTC")
    public void endDailyTournaments() {
        tournamentService.endTournaments();
    }
}
