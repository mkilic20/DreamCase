package com.dreamgames.backendengineeringcasestudy.scheduler;

import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TournamentScheduler {

    @Autowired
    private TournamentService tournamentService;

    // Automatically starts a new tournament at 00:00 UTC
    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void createDailyTournament() {
        tournamentService.createTournament();
    }

    // Automatically ends the current tournament at 20:00 UTC
    @Scheduled(cron = "0 0 20 * * ?", zone = "UTC")
    public void endDailyTournaments() {
        tournamentService.endTournaments();
    }
}
