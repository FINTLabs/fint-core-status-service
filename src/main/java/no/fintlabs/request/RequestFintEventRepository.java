package no.fintlabs.request;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.event.RequestFintEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class RequestFintEventRepository {

    private RequestFintEventJpaRepository requestFintEventJpaRepository;

    @Scheduled(cron = "0 0 13 * * ?")
    private void removeEventsOlderThanTwoWeeks(){
        long twoWeeks = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli();
        List<RequestFintEventEntity> requests = requestFintEventJpaRepository.findOlderThan(twoWeeks);
        requests.forEach(request -> {
            try {
                requestFintEventJpaRepository.delete(request);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

}
