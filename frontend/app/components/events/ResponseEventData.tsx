import {
  Box,
  CopyButton,
  HStack,
  Detail,
  VStack,
  Label,
} from "@navikt/ds-react";
import type { IResponseEvent } from "~/types/Event";
import { formatJson, StatusBadge } from "./eventUtils";
import { formatTimestampDetailed } from "~/utils/time";

interface ResponseEventDataProps {
  responseData: IResponseEvent | null;
}

export function ResponseEventData({ responseData }: ResponseEventDataProps) {
  return (
    <>
      <Box className="mt-4">
        <h2 className="mb-3">Response Data</h2>
        {responseData ? (
          <HStack gap="space-16" justify="space-around">
            <VStack gap="space-8">
              <Label size="small">Correlation ID:</Label>
              <Detail className="bg-ax-neutral-200 p-2">
                {responseData.corrId}
              </Detail>
              <Label size="small">Adapter ID:</Label>
              <Detail>{responseData.adapterId}</Detail>
              <Label size="small">Organization:</Label>
              <Detail>{responseData.orgId}</Detail>
            </VStack>
            <VStack gap="space-8">
              <Label size="small">Status:</Label>
              <Detail weight="semibold">
                <StatusBadge
                  failed={responseData.failed}
                  rejected={responseData.rejected}
                  conflicted={responseData.conflicted}
                />
              </Detail>
              <Label size="small">Handled At:</Label>
              <Detail>{formatTimestampDetailed(responseData.handledAt)}</Detail>
            </VStack>
            <VStack gap="space-8">
              {responseData.errorMessage && (
                <Box className="col-span-2">
                  <span className="font-medium text-ax-neutral-700">
                    Error Message:
                  </span>
                  <Box className="mt-1 p-2 bg-ax-danger-100 border border-ax-danger-300 rounded text-ax-danger-900">
                    {responseData.errorMessage}
                  </Box>
                </Box>
              )}
              {responseData.rejectReason && (
                <Box className="col-span-2">
                  <span className="font-medium text-ax-neutral-700">
                    Reject Reason:
                  </span>
                  <Box className="mt-1 p-2 bg-yellow-50 border border-yellow-200 rounded text-yellow-800">
                    {responseData.rejectReason}
                  </Box>
                </Box>
              )}
              {responseData.conflictReason && (
                <Box className="col-span-2">
                  <span className="font-medium text-ax-neutral-700">
                    Conflict Reason:
                  </span>
                  <Box className="mt-1 p-2 bg-ax-warning-100 border border-ax-warning-300 rounded text-ax-warning-900">
                    {responseData.conflictReason}
                  </Box>
                </Box>
              )}
            </VStack>
          </HStack>
        ) : (
          <p className="text-ax-neutral-600">No response data available</p>
        )}
      </Box>
      <Box className="mt-4">
        {responseData ? (
          <>
            <HStack
              gap="space-16"
              justify="space-between"
              align="center"
              className="mb-2"
            >
              <span className="font-medium text-ax-neutral-700">Raw JSON:</span>
              <CopyButton
                copyText={formatJson(responseData)}
                size="small"
                text="Kopier JSON"
                activeText="Kopiert!"
              />
            </HStack>
            <pre className="novari-json-box">{formatJson(responseData)}</pre>
          </>
        ) : (
          <p className="text-ax-neutral-600">No response data available</p>
        )}
      </Box>
    </>
  );
}
