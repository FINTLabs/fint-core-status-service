import {
  Box,
  CopyButton,
  Detail,
  HStack,
  Label,
  VStack,
} from "@navikt/ds-react";
import type { IRequestEvent } from "~/types/Event";
import { formatJson, OperationBadge } from "./eventUtils";
import { formatTimestampDetailed } from "~/utils/time";

interface RequestEventDataProps {
  requestData: IRequestEvent | null;
}

export function RequestEventData({ requestData }: RequestEventDataProps) {
  return (
    <>
      <Box className="mt-4">
        <h2 className="mb-3">Request Data</h2>
        {requestData ? (
          <HStack gap="space-16" justify="space-around">
            <VStack gap="space-8">
              <Label size="small">Correlation ID:</Label>
              <Detail>{requestData.corrId}</Detail>
              <Label size="small">Organization:</Label>
              <Detail>{requestData.orgId}</Detail>
              <Label size="small">Domain:</Label>
              <Detail>{requestData.domainName}</Detail>
              <Label size="small">Package:</Label>
              <Detail>{requestData.packageName}</Detail>
            </VStack>
            <VStack gap="space-8">
              <Label size="small">Operation:</Label>
              <Detail weight="semibold">
                <OperationBadge operation={requestData?.operationType} />
              </Detail>
              <Label size="small">Created:</Label>
              <Detail>{formatTimestampDetailed(requestData.created)}</Detail>
              <Label size="small">Time to Live:</Label>
              <Detail>{formatTimestampDetailed(requestData.timeToLive)}</Detail>
            </VStack>
          </HStack>
        ) : (
          <p className="text-ax-neutral-600">No request data available</p>
        )}
      </Box>
      <Box className="mt-4">
        {requestData ? (
          <>
            <HStack
              gap="space-16"
              justify="space-between"
              align="center"
              className="mb-2"
            >
              <span className="font-medium text-ax-neutral-700">Raw JSON:</span>
              <CopyButton
                copyText={formatJson(requestData)}
                size="small"
                text="Kopier JSON"
                activeText="Kopiert!"
              />
            </HStack>
            <pre className="novari-json-box">{formatJson(requestData)}</pre>
          </>
        ) : (
          <p className="text-ax-neutral-600">No request data available</p>
        )}
      </Box>
    </>
  );
}
