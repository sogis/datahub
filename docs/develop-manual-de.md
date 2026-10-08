# Entwicklung

Voraussetzungen: Java 21, Docker mit Compose und der Gradle-Wrapper im Repository.

## Lokale Umgebung

```sh
./gradlew devSeed
./gradlew bootRun --args='--spring.profiles.active=dev'
```

`devSeed` startet die isolierte Compose-Umgebung `datahub-dev`, wartet auf PostgreSQL und Mailpit,
führt die versionierten SQL-Migrationen aus und lädt idempotent synthetische Beispieldaten.
PostgreSQL ist auf `localhost:55432`, SMTP auf `localhost:1025`, Mailpit auf
[localhost:8025](http://localhost:8025) erreichbar. Mailpit leitet keine Nachrichten weiter.
Die Anwendung verwendet `build/dev/work` und `build/dev/target`.
Die vorhandene Compose-Datenbank auf Port 54321 wird nicht verwendet.

Der ausschliesslich lokale Demo-Key ist
`dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8`.

```sh
curl -i -H 'X-API-KEY: dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8' \
  -F theme=Demo -F operat=sample -F file=@dev/valid.xtf http://localhost:8080/api/deliveries
```

Weitere Beispiele sind `dev/invalid.xtf` und `dev/other-model.xtf`. Das lokale Modellrepository
liegt unter `dev/models`. Für automatische Tests sind keine externen Modellrepositories erforderlich.

- `./gradlew devUp`: Infrastruktur starten und migrieren.
- `./gradlew devSeed`: zusätzlich Demo-Daten laden; mehrfach aufrufbar.
- `./gradlew devDown`: Infrastruktur stoppen, Daten behalten.
- `./gradlew devReset`: **lokale Compose-Datenbank löschen**. Anschliessend `devSeed` ausführen.
  Arbeitsdateien unter `build/dev` separat entfernen, wenn auch Lieferartefakte zurückgesetzt werden sollen.

IDE-Starts verwenden das Spring-Profil `dev` und das Repository als Arbeitsverzeichnis.
JTE-Templates werden in der Entwicklung neu geladen; die Produktionsartefakte enthalten vorkompilierte Templates.

## Datenbankmigrationen

INTERLIS-Modelle in `dev/SO_AGI_Datahub_20240403.ili` bleiben die fachliche Quelle.
Eine Modelländerung benötigt eine geprüfte, versionierte Migration unter `sql/migrations` sowie
aktualisiertes Cayenne-Mapping. Bestehende Installationen werden nicht neu angelegt.
Flyway wird ausschliesslich über das Werkzeug und in Tests ausgeführt, niemals beim Produktionsstart.

```sh
DBURL=jdbc:postgresql://localhost:55432/edit DBUSR=postgres DBPWD=secret ./gradlew dbMigrate
```

Für eine nachweislich unveränderte bestehende V1-Installation kann vor der ersten Migration einmalig
`./gradlew dbMigrate -PmigrationAction=baseline` ausgeführt werden. Diese Baseline muss zuvor
gegen das bestehende Schema geprüft werden; automatisches Baseline-on-migrate ist absichtlich deaktiviert.
Produktive Zugangsdaten ausschliesslich über geschützte Umgebungsvariablen bereitstellen.

## Tests

`./gradlew test` führt schnelle Tests aus. `./gradlew check` prüft zusätzlich Integration und E2E
gegen isolierte PostgreSQL-Container. Die Entwicklungsdatenbank wird dabei nicht verändert.
