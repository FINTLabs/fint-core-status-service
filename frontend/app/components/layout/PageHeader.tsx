import { Box, Detail, Heading, HStack, Spacer, VStack } from "@navikt/ds-react";

interface PageHeaderProps {
  title: string;
  description: string;
  env?: string;
  icon?: React.ElementType;
  actions?: React.ReactNode;
}

export function PageHeader({
  title,
  description,
  env,
  icon: IconComponent,
  actions,
}: PageHeaderProps) {
  return (
    <Box padding={"space-2"} marginBlock="space-16">
      <HStack
        marginBlock="space-8"
        gap="space-16"
        align="center"
        // justify={"start"}
      >
        {IconComponent && (
          <IconComponent title="Header Icon" fontSize="2.5rem" />
        )}
        <VStack gap="space-8">
          <Heading size="large">
            {title}
            {env && (
              <span
                className={"novari-header-title"}
                style={{
                  fontSize: "2rem",
                }}
              >
                : {env}
              </span>
            )}
          </Heading>
          <Detail>{description}</Detail>
        </VStack>
        <Spacer />
        {actions}
      </HStack>
    </Box>
  );
}
