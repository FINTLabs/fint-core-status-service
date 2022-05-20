package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.SyncPageMetadata;
import no.fintlabs.entities.AdapterFullSyncEntity;
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
public class AdapterFullSyncService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterFullSyncRepository adapterFullSyncRepository;

    public AdapterFullSyncService(EventConsumerFactoryService consumerFactory, AdapterFullSyncRepository adapterFullSyncRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterFullSyncRepository = adapterFullSyncRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createFactory(
                SyncPageMetadata.class,
                onAdapterRegister(),
                new CommonLoggingErrorHandler(),
                false
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.anyOf("adapter-full-sync"))
                        .build()
        );
    }

    private Consumer<ConsumerRecord<String, SyncPageMetadata>> onAdapterRegister() {

        return (ConsumerRecord<String, SyncPageMetadata> record) -> {
            SyncPageMetadata syncPageMetadata = record.value();
            log.trace("Sync page metadata: {}", syncPageMetadata.toString());

            AdapterFullSyncEntity adapterFullSyncEntity = AdapterFullSyncEntity.toEntity(syncPageMetadata);

            adapterFullSyncRepository.save(adapterFullSyncEntity);
        };
    }
}
