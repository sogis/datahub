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
Die Oberfläche liegt unter [localhost:8080/jobs](http://localhost:8080/jobs).
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

## Mailversand ausprobieren

Die Lieferung erzeugt nach Validierung/Übernahme einen gespeicherten Mailauftrag. Die Nachricht
ist unter [Mailpit](http://localhost:8025) sichtbar, einschliesslich Absender, Empfänger und Links.
Auch die Schlüsselausstellung lässt sich mit dem Demo-Key ausprobieren:

```sh
curl -i -X POST -H 'X-API-KEY: dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8' \
  http://localhost:8080/api/keys
```

Der neue Key steht nur in dieser Mail. Nach erfolgreichem Versand ist er aktiv. Mit gestopptem
Mailpit schlägt die Ausstellung fehl und der neue Key bleibt unbrauchbar. Ergebnis- und
Widerrufsmails behalten dagegen ihren gespeicherten Auftrag und können separat wiederholt werden.
SMTP-Probleme ändern kein Validierungsergebnis. Für einen SMTP-Ausfall nur Mailpit stoppen:

```sh
docker compose -f dev/docker-compose.yml stop mailpit
docker compose -f dev/docker-compose.yml up -d --wait mailpit
```

## Modell, SQL und Cayenne aktualisieren

1. `dev/SO_AGI_Datahub_20240403.ili` ändern.
2. Aus dem Repository-Root `jbang sql/create_schema_sql.java` ausführen. Die lokale Modellquelle
   erzeugt SQL-Entwürfe unter `build/model-ddl`; kein Datenbankzugriff und kein Überschreiben des
   historischen V1-Schemas. JBang ist nur für die Modellarbeit erforderlich.
3. SQL vergleichen, Datenübernahme und Constraints prüfen, eine neue Flyway-Migration schreiben.
   Physische Namen vorhandener Tabellen bleiben erhalten. Neue Anwendungstabellen verwenden die
   in `sql/model_metadata.py` dokumentierte Zuordnung; `python3 sql/model_metadata.py` erzeugt
   den passenden Exportmetadaten-Entwurf `build/model-ddl/metadata.sql`.
4. `./gradlew devUp` ausführen, Cayenne-Mapping prüfen/aktualisieren und `./gradlew cgen` ausführen.
   `cdbimport` ist auf die lokale Datenbank ausgerichtet, kann aber Mapping-Details verändern und
   muss vor Übernahme des Diffs geprüft werden. Getrackte Mapping-/Klassenänderungen gemeinsam
   mit Modell und Migration einchecken.
5. `./gradlew check` ausführen. Die Testcontainer migrieren ein frisches Schema und einen mit
   historischen Daten gefüllten V1-Stand; Operatsdubletten müssen die Migration abbrechen.

## Interne Verarbeitung

Der Controller bindet die Lieferung und übergibt sie an `DeliverySubmissionService`. Der Dispatcher
verwendet `ValidationExecutor`; JobRunr erhält nur IDs. `ValidationAttemptRunner` ist der gemeinsame
Ablauf für JobRunr und `worker-single`. `ArtifactStore` trennt dauerhafte Artefakte von lokalen
Arbeitsdateien. Veröffentlichung und Benachrichtigung sind eigene, begrenzt wiederholbare Schritte.
Alle Cayenne-Operationen verwenden kurze, getrennte Kontexte im gemeinsamen Runtime.

Einzelauftragsmodus aus IDE oder Jar starten:

```sh
./gradlew bootJar
java -jar build/libs/datahub-*-exec.jar --spring.profiles.active=dev \
  --app.mode=worker-single --app.attempt-id=UUID
```

Zuvor den Web-Dispatcher deaktivieren (`--app.dispatch-enabled=false` beim Webstart). Die Versuch-ID
steht in `validation_attempt`; die öffentliche Lieferungs-ID bleibt in `deliveries_delivery.jobid`.
Der Einzelprozess startet weder HTTP noch JobRunr und übernimmt keine Veröffentlichung oder Mails.

## Prüfbare Änderungsstände

Der Boot-3.5-Zwischenstand wurde mit Unit-, PostgreSQL- und echten E2E-Tests geprüft, bevor die
Umstellung auf Boot 4/Jackson 3/JobRunr 8 erfolgte. CI prüft Pull Requests mit `build` inklusive
`check`; Images werden nur bei erfolgreichem Push-Check auf main oder einem Versionstag publiziert.
`./gradlew buildImage` erstellt das Produktionsimage lokal als `sogis/datahub:local`.
