package no.fintlabs.adapter.datasync.deltasync;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.entities.AdapterDeltaSyncEntity;
import no.fintlabs.adapter.models.SyncPageMetadata;
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern;
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
public class AdapterDeltaSyncService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterDeltaSyncRepository adapterDeltaSyncRepository;

    public AdapterDeltaSyncService(EventConsumerFactoryService consumerFactory, AdapterDeltaSyncRepository adapterDeltaSyncRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterDeltaSyncRepository = adapterDeltaSyncRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createFactory(
                SyncPageMetadata.class,
                onAdapterDeltaSync(),
                new CommonLoggingErrorHandler(),
                false
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.anyOf("adapter-delta-sync"))
                        .build()
        );
    }

    private Consumer<ConsumerRecord<String, SyncPageMetadata>> onAdapterDeltaSync() {
        return (ConsumerRecord<String, SyncPageMetadata> record) -> {
            SyncPageMetadata syncPageMetadata = record.value();
            log.trace("DeltaSync metadata {}", syncPageMetadata.toString());

            AdapterDeltaSyncEntity adapterDeltaSyncEntity = AdapterDeltaSyncEntity.toEntity(syncPageMetadata);

            adapterDeltaSyncRepository.save(adapterDeltaSyncEntity);
        };
    }
}
