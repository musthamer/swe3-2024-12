# Impftermin-Registrierungsapp (SWE3)

## Auf Hopper (Hochschul-Server)

Jeder Student hat einen eigenen Account auf **hopper** und ein eigenes **Docker/Tomcat**-Setup
(`docker-<username>-java` / `docker-<username>-manager`).

### Einmalig nach Clone

```bash
ssh hopper
cd <projektverzeichnis>/impfterminregistrierungsappSWE3
bin/configure.sh    # liest ~/.my.cnf, erzeugt local/config.txt
```

### Build & Deploy (alles automatisch)

```bash
bin/build.sh
```

`build.sh` erledigt automatisch:
1. `configure.sh` – falls noch keine `local/config.txt` existiert
2. `download-libs.sh` – falls JARs fehlen
3. `init-db.sh` – Datenbank anlegen (einmalig, Marker: `local/.db-initialized`)
4. clean → prepare → compile → assemble → deploy → check

Die App läuft danach unter:

**https://informatik.hs-bremerhaven.de/docker-dein-username-java/**

Admin-Login (nach DB-Init): `admin@impfservice.de` / `admin123`

### Datenbank neu initialisieren

```bash
rm local/.db-initialized
bin/init-db.sh
```

## Lokales Docker (optional, nicht Hopper)

```bash
bin/configure-work.sh
bin/build.sh
```

## Skripte

| Skript | Zweck |
|--------|-------|
| `bin/configure.sh` | Hopper-Config (DB, Redis, Tomcat-Manager) |
| `bin/init-db.sh` | Schema + Stammdaten in MariaDB |
| `bin/build.sh` | Vollständiger Build- und Deploy-Zyklus |
| `bin/clean.sh` | `build/` und `target/` leeren |
| `bin/clean-all.sh` | Alles inkl. `lib/`, `local/` |
