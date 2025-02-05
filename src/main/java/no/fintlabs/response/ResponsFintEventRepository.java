package no.fintlabs.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class ResponsFintEventRepository {

    private ResponseFintEventJpaRepository responseFintEventJpaRepository;

    public ResponsFintEventRepository(ResponseFintEventJpaRepository responseFintEventJpaRepository) {
        this.responseFintEventJpaRepository = responseFintEventJpaRepository;
    }

    @Scheduled(cron = "${event.database.cleanupTime}")
    private void removeEventsOlderThanTwoWeeks() {
        long days = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli();
        try {
            responseFintEventJpaRepository.deleteRowsOlderThan(days);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
