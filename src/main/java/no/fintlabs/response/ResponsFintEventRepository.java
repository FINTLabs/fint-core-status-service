package no.fintlabs.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class ResponsFintEventRepository {

    private ResponseFintEventJpaRepository responseFintEventJpaRepository;

    @Scheduled(cron = "0 08 13 * * ?")
    private void removeEventsOlderThanTwoWeeks() {
        long twoWeeks = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli();
        List<ResponseFintEventEntity> responsesOlderThanTwoWeeks = responseFintEventJpaRepository.findOlderThan(twoWeeks);
        responsesOlderThanTwoWeeks.forEach(responseFintEventEntity -> {
            try {
                responseFintEventJpaRepository.delete(responseFintEventEntity);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

}
