package no.fintlabs;

import no.fintlabs.adapter.models.event.RequestFintEvent;
import no.fintlabs.adapter.models.event.ResponseFintEvent;
import no.fintlabs.adapter.models.sync.SyncPageEntry;
import no.fintlabs.adapter.operation.OperationType;
import no.fintlabs.request.RequestFintEventEntity;
import no.fintlabs.response.ResponseFintEventEntity;
import org.springframework.stereotype.Service;

@Service
public class MappingService {

    // TODO: Refactor to Kotlin & || make this static

    public RequestFintEventEntity mapRequestFintEventToEntity(RequestFintEvent requestEvent, String topic) {
        return new RequestFintEventEntity(
                requestEvent.getCorrId(),
                requestEvent.getOrgId(),
                topic,
                requestEvent.getDomainName(),
                requestEvent.getPackageName(),
                requestEvent.getResourceName(),
                String.valueOf(requestEvent.getOperationType()),
                requestEvent.getCreated(),
                requestEvent.getTimeToLive(),
                ""
        );
    }

    public ResponseFintEventEntity mapResponseFintEventToEntity(ResponseFintEvent responseEvent, String topic) {
        return new ResponseFintEventEntity(
                responseEvent.getCorrId(),
                responseEvent.getOrgId(),
                responseEvent.getAdapterId(),
                topic,
                responseEvent.getHandledAt(),
                responseEvent.isFailed(),
                responseEvent.getErrorMessage(),
                responseEvent.getRejectReason(),
                responseEvent.isConflicted(),
                responseEvent.isRejected(),
                responseEvent.getConflictReason(),
                responseEvent.getValue() == null ? "[NULL]" : responseEvent.getValue().getIdentifier(),
                ""
        );
    }

    public RequestFintEvent mapEntityToRequestFintEvent(RequestFintEventEntity entity) {
        return RequestFintEvent.builder()
                .corrId(entity.getCorrId())
                .orgId(entity.getOrgId())
                .domainName(entity.getDomainName())
                .packageName(entity.getPackageName())
                .resourceName(entity.getResourceName())
                .operationType(setOperationType(entity.getOperationType()))
                .created(entity.getCreated())
                .timeToLive(entity.getTimeToLive())
                .value(entity.getValue())
                .build();
    }

    public ResponseFintEvent mapEntityToResponseFintEvent(ResponseFintEventEntity entity) {
        return ResponseFintEvent.builder()
                .corrId(entity.getCorrId())
                .orgId(entity.getOrgId())
                .adapterId(entity.getAdapterId())
                .handledAt(entity.getHandledAt())
                .failed(entity.isFailed())
                .errorMessage(entity.getErrorMessage())
                .rejected(entity.isRejected())
                .rejectReason(entity.getRejectReason())
                .conflicted(entity.isConflicted())
                .conflictReason(entity.getConflictReason())
                .value(SyncPageEntry.of(entity.getSyncPageEntryIdentifier(), entity.getSyncPageEntryResource()))
                .build();
    }

    private OperationType setOperationType(String operationTypeValue) {
        if (operationTypeValue == "CREATE") {
            return OperationType.CREATE;
        } else if (operationTypeValue == "UPDATE") {
            return OperationType.UPDATE;
        } else if (operationTypeValue == "VALIDATE") {
            return OperationType.VALIDATE;
        }
        return OperationType.DELETE;
    }
}
