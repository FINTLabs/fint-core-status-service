import { Link } from "react-router";
import { ChevronRightIcon } from "@navikt/aksel-icons";
import { Detail, HStack } from "@navikt/ds-react";

interface BreadcrumbItem {
  label: string;
  href: string;
}

interface BreadcrumbsProps {
  items: BreadcrumbItem[];
}

export function Breadcrumbs({ items }: BreadcrumbsProps) {
  return (
    <Detail>
      <HStack
        // className="flex items-center space-x-2 text-sm text-ax-neutral-700 mb-6"
        aria-label="Breadcrumb"
        data-cy="breadcrumbs"
        gap="space-4"
        marginBlock="space-16"
      >
        <Link
          to="/adaptere"
          className="flex items-center hover:text-ax-accent-700 transition-colors"
          aria-label="Tilbake til adaptere"
        >
          Adaptere
        </Link>

        {items.map((item, index) => (
          <HStack key={index} gap="space-0" align="center">
            <ChevronRightIcon
              fontSize="0.875rem"
              className="text-ax-neutral-500"
            />
            {index === items.length - 1 ? (
              <span
                // className="text-ax-neutral-1000 font-medium"
                aria-current="page"
              >
                {item.label}
              </span>
            ) : (
              <Link
                to={item.href}
                // className="hover:text-ax-accent-700 transition-colors"
              >
                {item.label}
              </Link>
            )}
          </HStack>
        ))}
      </HStack>
    </Detail>
  );
}
