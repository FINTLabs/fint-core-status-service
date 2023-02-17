package no.fintlabs.adapter.datasync.deletesync;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.entities.AdapterDeleteSyncEntity;
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
public class AdapterDeleteSyncService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterDeleteSyncRepository adapterDeleteSyncRepository;

    public AdapterDeleteSyncService(EventConsumerFactoryService consumerFactory, AdapterDeleteSyncRepository adapterDeleteSyncRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterDeleteSyncRepository = adapterDeleteSyncRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createFactory(
                SyncPageMetadata.class,
                onAdapterDeleteSync(),
                new CommonLoggingErrorHandler(),
                false
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.anyOf("adapter-delete-sync"))
                        .build()
        );
    }

    private Consumer<ConsumerRecord<String, SyncPageMetadata>> onAdapterDeleteSync() {
        return (ConsumerRecord<String, SyncPageMetadata> record) -> {
            SyncPageMetadata syncPageMetadata = record.value();
            log.trace("Sync page metadata: {}", syncPageMetadata.toString());

            AdapterDeleteSyncEntity adapterDeleteSyncEntity = AdapterDeleteSyncEntity.toEntity(syncPageMetadata);

            adapterDeleteSyncRepository.save(adapterDeleteSyncEntity);
        };
    }
}
