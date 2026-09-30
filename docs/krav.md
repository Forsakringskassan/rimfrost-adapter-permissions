# Krav — Permissions Adapter

## Bakgrund

Adaptern kapslar in anrop till permissions-tjänsten och exponerar dess `hasSidPermission`-endpoint
som ett typat Java-API för konsumenter inom Rimfrost.

---

## Funktionella krav

### PERM-FR-01 — Kontrollera SID-behörighet

- **PERM-FR-01.1** Adaptern ska returnera `true` om permissions-tjänsten svarar att användaren har SID-behörighet.
- **PERM-FR-01.2** Adaptern ska returnera `false` om permissions-tjänsten svarar att användaren saknar SID-behörighet.
- **PERM-FR-01.3** Om permissions-tjänsten svarar med HTTP 404 ska adaptern propagera ett `NotFoundException`.
