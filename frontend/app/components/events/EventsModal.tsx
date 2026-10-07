import { Box, Button, Heading, Label, Modal, Tabs } from "@navikt/ds-react";
import type { IRequestEvent, IResponseEvent } from "~/types/Event";
import { RequestEventData } from "./RequestEventData";
import { ResponseEventData } from "./ResponseEventData";

interface HendelserModalProps {
  isOpen: boolean;
  onClose: () => void;
  requestData: IRequestEvent | null;
  responseData: IResponseEvent | null;
}

export function EventsModal({
  isOpen,
  onClose,
  requestData,
  responseData,
}: HendelserModalProps) {
  return (
    <Modal
      open={isOpen}
      onClose={onClose}
      aria-labelledby="hendelser-modal-title"
      aria-describedby="hendelser-modal-description"
      placement="top"
      className="min-w-150"
    >
      <Modal.Header>
        <Heading id="hendelser-modal-title" size="medium">
          Hendelse Detaljer:
        </Heading>
        <Label>{requestData?.corrId}</Label>
      </Modal.Header>
      <Modal.Body>
        <Box id="hendelser-modal-description">
          <Tabs defaultValue="request" size="small">
            <Tabs.List>
              <Tabs.Tab value="request" label="Request" />
              <Tabs.Tab value="response" label="Response" />
            </Tabs.List>

            <Tabs.Panel value="request">
              <RequestEventData requestData={requestData} />
            </Tabs.Panel>

            <Tabs.Panel value="response">
              <ResponseEventData responseData={responseData} />
            </Tabs.Panel>
          </Tabs>
        </Box>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="tertiary" onClick={onClose}>
          Lukk
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
