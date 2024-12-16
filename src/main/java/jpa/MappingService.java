package jpa;

import jpa.request.RequestFintEventEntity;
import jpa.response.ResponseFintEventEntity;
import no.fintlabs.event.EventStatus;
import org.springframework.stereotype.Service;

@Service
public class MappingService {

    public RequestFintEventEntity MapRequestFintEventToEntity(EventStatus eventStatus) {
        RequestFintEventEntity requestFintEventEntity = RequestFintEventEntity.builder()
                .corrId(eventStatus.getCorrId())
                .orgId(eventStatus.getOrgId())
                .domainName(eventStatus.getRequestEvent().getDomainName())
                .packageName(eventStatus.getRequestEvent().getPackageName())
                .resourceName(eventStatus.getRequestEvent().getResourceName())
                .operationType(eventStatus.getRequestEvent().getOperationType().toString())
                .created(eventStatus.getRequestEvent().getCreated())
                .timeToLive(eventStatus.getRequestEvent().getTimeToLive())
                .value(eventStatus.getRequestEvent().getValue())
                .build();
        return requestFintEventEntity;
    }

    public ResponseFintEventEntity mapResponseFintEventToEntity(EventStatus eventStatus) {
        ResponseFintEventEntity responseFintEvent = ResponseFintEventEntity.builder()
                .corrId(eventStatus.getCorrId())
                .orgId(eventStatus.getOrgId())
                .adapterId(eventStatus.getResponseEvent().getAdapterId())
                .handledAt(eventStatus.getResponseEvent().getHandledAt())
                .failed(eventStatus.getResponseEvent().isFailed())
                .errorMessage(eventStatus.getResponseEvent().getErrorMessage())
                .rejected(eventStatus.getResponseEvent().isRejected())
                .rejectReason(eventStatus.getResponseEvent().getRejectReason())
                .conflicted(eventStatus.getResponseEvent().isConflicted())
                .conflictReason(eventStatus.getResponseEvent().getConflictReason())
                .syncPageEntryIdentifier(eventStatus.getResponseEvent().getValue().getIdentifier())
                .syncPageEntryResource(eventStatus.getResponseEvent().getValue().getResource().toString())
                .build();
        return responseFintEvent;
    }
}
