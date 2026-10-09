# datahub

Datendrehscheibe für die Anlieferung von INTERLIS-Transferdateien.

## Beschreibung

Datahub authentifiziert Lieferanten über organisationsgebundene API-Keys, autorisiert das konkrete
Thema/Operat und prüft die Transferdatei gegen erwartete Modelle und konfigurierte INTERLIS-Regeln.
Lieferungen und Prüfversuche werden dauerhaft in PostgreSQL gespeichert; JobRunr führt die Prüfung aus.

Status, Prüfprotokolle und die Übersicht sind öffentlich zugänglich. Gültige Ergebnisse werden ins
Zielfilesystem übernommen. Bei `CREATE_DIRECTORIES=true` werden fehlende Zielverzeichnisse angelegt;
die Ablagen müssen beschreibbar sein. Die normalen Dateien `Thema/Operat.xtf` und
`Thema/Operat.xtf.log` bleiben verfügbar. Ergebnis-Mails werden bei aktiviertem Mailversand separat
verschickt; Übernahme- und Mailfehler sind unabhängig vom Prüfergebnis sichtbar.

Organisationen können eigene Keys erstellen und widerrufen. Administratoren dürfen für andere
Organisationen Keys ausstellen und jedes konfigurierte Operat beliefern. Die Ausstellung benötigt
funktionierenden Mailversand. Berechtigungen und Themenkonfiguration werden administrativ in der
Datenbank gepflegt. INTERLIS bleibt die Grundlage der Konfigurations- und Liefermodelle.

## Anleitungen

- [Benutzerhandbuch](docs/user-manual-de.md): API, Statuswerte, Keys und Dateien
- [Entwicklerhandbuch](docs/develop-manual-de.md): lokale Umgebung, Mailpit, Tests und Modellarbeit
- [Betriebshandbuch](docs/admin-manual-de.md): Konfiguration, Migration, Auslieferung und Überwachung

## Entwicklung und Prüfung

Java 21, Gradle 8.14.3, Spring Boot 4.1.1, Cayenne 4.2.3, JTE 3.2.4, HTMX 2.0.10 und JobRunr 8.8.2.
PostgreSQL ist erforderlich, auch für Claims und instanzübergreifende Veröffentlichungssperren.
SMTP ist für den Mailversand erforderlich. Kubernetes ist in dieser Umsetzung nicht enthalten.

Für den lokalen Einstieg aus dem Repository-Root (die Zugangsdaten gelten nur für die Demo):

```sh
DBURL=jdbc:postgresql://localhost:55432/edit DBUSR=postgres DBPWD=secret \
  CONFIG_DB_SCHEMA=agi_datahub_config_v1 LOG_DB_SCHEMA=agi_datahub_log_v1 \
  JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1 ./gradlew devSeed
DBUSR=postgres DBPWD=secret CONFIG_DB_SCHEMA=agi_datahub_config_v1 \
  LOG_DB_SCHEMA=agi_datahub_log_v1 JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1. \
  ./gradlew bootRun --args='--spring.profiles.active=dev'
```

Docker Compose startet PostgreSQL und Mailpit auf localhost. Lieferungen und Schlüsselausstellung
lassen sich ohne externe E-Mails ausprobieren. Vollständiger Ablauf und Datenlebenszyklus:
[Entwicklerhandbuch](docs/develop-manual-de.md).

```sh
./gradlew test    # schnelle Tests
./gradlew check   # zusätzlich Integration/E2E; benötigt Docker
./gradlew build   # enthält check und baut die Anwendung
```

Integration und E2E verwenden isolierte Testcontainers für PostgreSQL, reale Validierung, Mailpit
und das Produktionsimage. Schemaänderungen werden kontrolliert über `dbMigrate` ausgeführt,
nicht beim Anwendungsstart. Siehe [Betriebshandbuch](docs/admin-manual-de.md).
