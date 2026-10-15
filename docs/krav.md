# Krav — Permissions Adapter

## Bakgrund

Adaptern kapslar in anrop till permissions-tjänsten och exponerar dess `hasSidPermission`-endpoint
som ett typat Java-API för konsumenter inom Rimfrost.

---

## Funktionella krav

### PERM-FR-01 — Kontrollera SID-behörighet

- **PERM-FR-01.1** Adaptern ska returnera `true` om permissions-tjänsten svarar att användaren har SID-behörighet.
- **PERM-FR-01.2** Adaptern ska returnera `false` om permissions-tjänsten svarar att användaren saknar SID-behörighet.
- **PERM-FR-01.3** Om permissions-tjänsten svarar med HTTP 404 ska adaptern kasta ett `PermissionsException` med `ErrorType.NOT_FOUND`.
- **PERM-FR-01.4** Om permissions-tjänsten svarar med HTTP 400 ska adaptern kasta ett `PermissionsException` med `ErrorType.BAD_REQUEST`.
- **PERM-FR-01.5** Om permissions-tjänsten svarar med HTTP 503 ska adaptern kasta ett `PermissionsException` med `ErrorType.SERVICE_UNAVAILABLE`.
- **PERM-FR-01.6** Vid övriga HTTP-fel eller kommunikationsfel ska adaptern kasta ett `PermissionsException` med `ErrorType.UNEXPECTED_ERROR`.
