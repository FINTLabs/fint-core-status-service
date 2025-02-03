package no.fintlabs.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class RequestFintEventRepository {

    private RequestFintEventJpaRepository requestFintEventJpaRepository;

    public RequestFintEventRepository(RequestFintEventJpaRepository requestFintEventJpaRepository) {
        this.requestFintEventJpaRepository = requestFintEventJpaRepository;
    }

    @Scheduled(cron = "${event.database.cleanupTime}")
    private void removeEventsOlderThanTwoWeeks() {
        long twoWeeks = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli();
        try {
            requestFintEventJpaRepository.deleteRowsOlderThan(twoWeeks);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
