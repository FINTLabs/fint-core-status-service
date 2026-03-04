import no.fintlabs.MappingService;
import no.fintlabs.adapter.models.event.RequestFintEvent;
import no.fintlabs.adapter.models.event.ResponseFintEvent;
import no.fintlabs.adapter.models.sync.SyncPageEntry;
import no.fintlabs.adapter.operation.OperationType;
import no.fintlabs.request.RequestFintEventEntity;
import no.fintlabs.response.ResponseFintEventEntity;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class FintEventMapperTest {

    private final MappingService mapper = new MappingService();

    @Test
    void mapRequestFintEventToEntity_maps_all_fields_and_sets_resource_empty_string() {
        var created = 1770208885536L;
        var request = RequestFintEvent.builder()
                .corrId("corr-1")
                .orgId("org-1")
                .domainName("domain")
                .packageName("pkg")
                .resourceName("resource")
                .operationType(OperationType.CREATE)
                .created(created)
                .timeToLive(123L)
                .value("some-value")
                .build();

        var entity = mapper.mapRequestFintEventToEntity(request, "topic-1");

        assertThat(entity.getCorrId()).isEqualTo("corr-1");
        assertThat(entity.getOrgId()).isEqualTo("org-1");
        assertThat(entity.getTopic()).isEqualTo("topic-1");
        assertThat(entity.getDomainName()).isEqualTo("domain");
        assertThat(entity.getPackageName()).isEqualTo("pkg");
        assertThat(entity.getResourceName()).isEqualTo("resource");
        assertThat(entity.getOperationType()).isEqualTo("CREATE");
        assertThat(entity.getCreated()).isEqualTo(created);
        assertThat(entity.getTimeToLive()).isEqualTo(123L);

        assertThat(entity.getValue()).isEqualTo("");
    }

    @Test
    void mapResponseFintEventToEntity_maps_all_fields_when_value_present() {
        var handledAt = 1770208885536L;
        var value = SyncPageEntry.of("id-123", "ignored-resource");
        var response = ResponseFintEvent.builder()
                .corrId("corr-2")
                .orgId("org-2")
                .adapterId("adapter-1")
                .handledAt(handledAt)
                .failed(true)
                .errorMessage("boom")
                .rejectReason("reject-reason")
                .conflicted(true)
                .rejected(false)
                .conflictReason("conflict-reason")
                .value(value)
                .build();

        var entity = mapper.mapResponseFintEventToEntity(response, "topic-2");

        assertThat(entity.getCorrId()).isEqualTo("corr-2");
        assertThat(entity.getOrgId()).isEqualTo("org-2");
        assertThat(entity.getAdapterId()).isEqualTo("adapter-1");
        assertThat(entity.getTopic()).isEqualTo("topic-2");
        assertThat(entity.getHandledAt()).isEqualTo(handledAt);
        assertThat(entity.isFailed()).isTrue();
        assertThat(entity.getErrorMessage()).isEqualTo("boom");
        assertThat(entity.getRejectReason()).isEqualTo("reject-reason");
        assertThat(entity.isConflicted()).isTrue();
        assertThat(entity.isRejected()).isFalse();
        assertThat(entity.getConflictReason()).isEqualTo("conflict-reason");

        assertThat(entity.getSyncPageEntryIdentifier()).isEqualTo("id-123");

        assertThat(entity.getSyncPageEntryResource()).isEqualTo("");
    }

    @Test
    void mapResponseFintEventToEntity_sets_identifier_to_NULL_when_value_is_null() {
        var handledAt = 1770208885536L;
        var response = ResponseFintEvent.builder()
                .corrId("corr-3")
                .orgId("org-3")
                .adapterId("adapter-2")
                .handledAt(handledAt)
                .failed(false)
                .errorMessage(null)
                .rejectReason(null)
                .conflicted(false)
                .rejected(false)
                .conflictReason(null)
                .value(null)
                .build();

        var entity = mapper.mapResponseFintEventToEntity(response, "topic-3");

        assertThat(entity.getSyncPageEntryIdentifier()).isEqualTo("[NULL]");
        assertThat(entity.getSyncPageEntryResource()).isEqualTo("");
    }

    @Test
    void mapEntityToRequestFintEvent_maps_all_fields_and_converts_operationType_CREATE() {
        var created = 1770208885536L;
        var entity = new RequestFintEventEntity(
                "corr-4",
                "org-4",
                "topic-ignored-here",
                "domain",
                "pkg",
                "resource",
                "CREATE",
                created,
                555L,
                "value-1"
        );

        var dto = mapper.mapEntityToRequestFintEvent(entity);

        assertThat(dto.getCorrId()).isEqualTo("corr-4");
        assertThat(dto.getOrgId()).isEqualTo("org-4");
        assertThat(dto.getDomainName()).isEqualTo("domain");
        assertThat(dto.getPackageName()).isEqualTo("pkg");
        assertThat(dto.getResourceName()).isEqualTo("resource");
        assertThat(dto.getOperationType()).isEqualTo(OperationType.CREATE);
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getTimeToLive()).isEqualTo(555L);
        assertThat(dto.getValue()).isEqualTo("value-1");
    }

    @Test
    void mapEntityToRequestFintEvent_converts_operationType_UPDATE_VALIDATE_UNKNOWN() {
        var baseCreated = 1770208885536L;

        var updateEntity = new RequestFintEventEntity(
                "c1", "o1", "t", "d", "p", "r", "UPDATE", baseCreated, 1L, "v"
        );
        var validateEntity = new RequestFintEventEntity(
                "c2", "o2", "t", "d", "p", "r", "VALIDATE", baseCreated, 1L, "v"
        );
        var unknownEntity = new RequestFintEventEntity(
                "c3", "o3", "t", "d", "p", "r", "SOMETHING_ELSE", baseCreated, 1L, "v"
        );

        assertThat(mapper.mapEntityToRequestFintEvent(updateEntity).getOperationType())
                .isEqualTo(OperationType.UPDATE);
        assertThat(mapper.mapEntityToRequestFintEvent(validateEntity).getOperationType())
                .isEqualTo(OperationType.VALIDATE);
        assertThat(mapper.mapEntityToRequestFintEvent(unknownEntity).getOperationType())
                .isEqualTo(OperationType.UNKNOWN);
    }

    @Test
    void mapEntityToResponseFintEvent_maps_all_fields_and_builds_syncPageEntry() {
        var handledAt = 1770208885536L;
        var entity = new ResponseFintEventEntity(
                "corr-5",
                "org-5",
                "adapter-5",
                "topic-ignored-here",
                handledAt,
                true,
                "err",
                "reject",
                true,
                true,
                "conflict",
                "id-999",
                "res-999"
        );

        var dto = mapper.mapEntityToResponseFintEvent(entity);

        assertThat(dto.getCorrId()).isEqualTo("corr-5");
        assertThat(dto.getOrgId()).isEqualTo("org-5");
        assertThat(dto.getAdapterId()).isEqualTo("adapter-5");
        assertThat(dto.getHandledAt()).isEqualTo(handledAt);
        assertThat(dto.isFailed()).isTrue();
        assertThat(dto.getErrorMessage()).isEqualTo("err");
        assertThat(dto.isRejected()).isTrue();
        assertThat(dto.getRejectReason()).isEqualTo("reject");
        assertThat(dto.isConflicted()).isTrue();
        assertThat(dto.getConflictReason()).isEqualTo("conflict");

        assertThat(dto.getValue()).isNotNull();
        assertThat(dto.getValue().getIdentifier()).isEqualTo("id-999");
        assertThat(dto.getValue().getResource()).isEqualTo("res-999");
    }
}