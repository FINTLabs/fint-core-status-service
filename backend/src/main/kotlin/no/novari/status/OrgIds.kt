package no.novari.status

fun normalizeOrgId(orgId: String): String =
    orgId
        .trim()
        .lowercase()
        .replace('-', '.')
        .replace('_', '.')
