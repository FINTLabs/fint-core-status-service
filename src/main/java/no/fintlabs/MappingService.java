package no.fintlabs;

import no.fintlabs.adapter.models.event.RequestFintEvent;
import no.fintlabs.adapter.models.event.ResponseFintEvent;
import no.fintlabs.request.RequestFintEventEntity;
import no.fintlabs.response.ResponseFintEventEntity;
import org.springframework.stereotype.Service;

@Service
public class MappingService {

    public RequestFintEventEntity mapRequestFintEventToEntity(RequestFintEvent requestEvent) {
        return RequestFintEventEntity.builder()
                .corrId(requestEvent.getCorrId())
                .orgId(requestEvent.getOrgId())
                .domainName(requestEvent.getDomainName())
                .packageName(requestEvent.getPackageName())
                .resourceName(requestEvent.getResourceName())
                .operationType(requestEvent.getOperationType().toString())
                .created(requestEvent.getCreated())
                .timeToLive(requestEvent.getTimeToLive())
                .value(requestEvent.getValue())
                .build();
    }

    public ResponseFintEventEntity mapResponseFintEventToEntity(ResponseFintEvent responseEvent) {
        return ResponseFintEventEntity.builder()
                .corrId(responseEvent.getCorrId())
                .orgId(responseEvent.getOrgId())
                .adapterId(responseEvent.getAdapterId())
                .handledAt(responseEvent.getHandledAt())
                .failed(responseEvent.isFailed())
                .errorMessage(responseEvent.getErrorMessage())
                .rejected(responseEvent.isRejected())
                .rejectReason(responseEvent.getRejectReason())
                .conflicted(responseEvent.isConflicted())
                .conflictReason(responseEvent.getConflictReason())
                .syncPageEntryIdentifier(responseEvent.getValue().getIdentifier())
                .syncPageEntryResource(responseEvent.getValue().getResource().toString())
                .build();
    }
}
