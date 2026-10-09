# Entwicklung

Voraussetzungen: Java 21, Docker mit Compose und der Gradle-Wrapper im Repository. Alle Befehle
werden aus dem Repository-Root ausgeführt. Die hier genannten Zugangsdaten und Keys sind
ausschliesslich synthetische lokale Beispieldaten.

## Lokale Umgebung starten

Migrationseinstellungen ausdrücklich lokal setzen: `devUp` und `devSeed` übernehmen sonst auch
bereits gesetzte Datenbank-Umgebungsvariablen. Die Anwendung benötigt beim JobRunr-Schema den
abschliessenden Punkt; das Migrationswerkzeug benötigt den Namen ohne Punkt.

```sh
DBURL=jdbc:postgresql://localhost:55432/edit DBUSR=postgres DBPWD=secret \
  CONFIG_DB_SCHEMA=agi_datahub_config_v1 LOG_DB_SCHEMA=agi_datahub_log_v1 \
  JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1 ./gradlew devSeed

DBUSR=postgres DBPWD=secret CONFIG_DB_SCHEMA=agi_datahub_config_v1 \
  LOG_DB_SCHEMA=agi_datahub_log_v1 JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1. \
  ./gradlew bootRun --args='--spring.profiles.active=dev'
```

`devSeed` startet Compose-Projekt `datahub-dev`, wartet auf die Healthchecks von PostgreSQL und
Mailpit, migriert die Datenbank und lädt idempotent die Beispieldaten. Bereits vorhandene
Lieferungen werden dabei nicht gelöscht. Das Profil `dev` aktiviert Mailversand und JTE-Hot-Reload
und verwendet das lokale Modellrepository ohne zusätzliche externe Standardrepositories.

| Dienst / Einstellung | Lokaler Wert |
| --- | --- |
| Anwendung / Übersicht | `http://localhost:8080` / `/jobs` |
| PostgreSQL | `localhost:55432`, Datenbank `edit`, Benutzer `postgres`, Passwort `secret` |
| JDBC-URL | `jdbc:postgresql://localhost:55432/edit` |
| Mailpit 1.31.4 | SMTP `localhost:1025`, Weboberfläche `http://localhost:8025` |
| SMTP-Anmeldung / TLS | Beide deaktiviert; Mailpit leitet keine Nachrichten weiter |
| Mail-Absender / Demo-Empfänger | `datahub@example.test` / `supplier@example.test` |
| Arbeits- / Zielverzeichnis | `build/dev/work` / `build/dev/target` |
| Modellrepository | `dev/models` |
| Öffentliche Basis-URL | `http://localhost:8080` |

Die Compose-Ports sind auf `127.0.0.1` beschränkt. Die ältere Datenbank auf Port 54321 wird nicht
verwendet. IDE-Starts verwenden dieselben lokalen Umgebungsvariablen, das Profil `dev` und das
Repository als Arbeitsverzeichnis. Bei einem anderen HTTP-Port auch `--app.public-base-url`
anpassen, beispielsweise zusammen `--server.port=18080 --app.public-base-url=http://localhost:18080`.

## Lieferung senden und Ergebnis ansehen

Der lokale Demo-Key lautet
`dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8`.
Er gehört der Organisation `Demo Supplier` mit Lieferrecht für `Demo/sample`.

```sh
curl --fail-with-body -sS -D build/dev/delivery.headers -o /dev/null \
  -H 'X-API-KEY: dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8' \
  -F theme=Demo -F operat=sample -F file=@dev/valid.xtf \
  http://localhost:8080/api/deliveries
delivery_status_url=$(awk 'tolower($1)=="operation-location:" {gsub("\r", "", $2); print $2}' build/dev/delivery.headers)
curl --fail-with-body -sS "$delivery_status_url"
```

Der POST liefert `202` und `Operation-Location`; den Status bis zum Abschluss erneut abrufen oder
die [Übersicht](http://localhost:8080/jobs) öffnen. Eine gültige Lieferung erreicht
`SUCCEEDED`, `DONE`, `DELIVERED` und bei erfolgreichem Versand `SENT`. Die normalen Dateien liegen
unter `build/dev/target/Demo/sample.xtf` und `sample.xtf.log`.

Die Nachricht in [Mailpit](http://localhost:8025) öffnen und Empfänger, Absender, Thema/Operat sowie
Links zur Übersicht und zum Protokoll prüfen. Weitere Beispiele sind `dev/invalid.xtf` und
`dev/other-model.xtf`: Beide ergeben eine technisch abgeschlossene, ungültige Prüfung ohne
Veröffentlichung. Einen zuvor gültigen veröffentlichten Stand ersetzen sie nicht.

## Daten behalten oder zurücksetzen

| Befehl | PostgreSQL | Mailpit-Nachrichten | Arbeits- und Zieldateien unter `build/dev` |
| --- | --- | --- | --- |
| `./gradlew devUp` | Startet Infrastruktur und migriert; bestehende Daten bleiben | Bleiben bei unverändert laufendem Container | Bleiben |
| `./gradlew devSeed` | Zusätzlich idempotente Demo-Konfiguration; Historie bleibt | Wie `devUp` | Bleiben |
| `./gradlew devDown` | Container entfernt, benanntes Datenvolume bleibt | Gehen beim Entfernen des Containers verloren | Bleiben |
| `./gradlew devReset` | **Löscht das lokale Compose-Datenvolume** | Gehen ebenfalls verloren | Bleiben |
| `./gradlew clean` | Unverändert, einschliesslich Lieferhistorie | Unverändert | **Werden mit dem Build-Verzeichnis gelöscht** |

Bei `devUp`/`devSeed` den lokalen Umgebungsprefix aus dem Startbeispiel verwenden. Mailpit hat kein
persistentes Volume; Nachrichten gehen auch bei einer Container-Neuerstellung verloren.
`devReset` wird nur ausdrücklich zum Zurücksetzen verwendet; anschliessend `devSeed` ausführen.
Ein vollständiger lokaler Reset benötigt zusätzlich das Entfernen der alten Arbeitsartefakte.
Vor `clean` die Anwendung stoppen und benötigte Demo-Artefakte sichern: Die Datenbank verweist
anschliessend weiterhin auf ihre Lieferungen, deren Dateien fehlen können.

## Mailversand ausprobieren

Die Schlüsselausstellung mit dem Demo-Key erfolgt für dessen eigene Organisation:

```sh
curl -i -X POST \
  -H 'X-API-KEY: dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8' \
  http://localhost:8080/api/keys
```

Der neue Key steht nur in der Mail. Der Request speichert zuerst seinen Hash als `PENDING`,
versendet das Secret einmalig und aktiviert danach den Key. Fehlgeschlagene Ausstellung erzeugt
keinen aktiven Key und wird nicht automatisch wiederholt. Bei deaktiviertem Mailversand wird sie
vor der Speicherung mit `503` abgelehnt.

Für einen lokalen SMTP-Ausfall nur Mailpit stoppen, den Key-Request erneut ausführen und die
`500`-Antwort prüfen. Dann Mailpit wieder starten:

```sh
docker compose -f dev/docker-compose.yml stop mailpit
# Den oben gezeigten POST /api/keys erneut ausführen.
docker compose -f dev/docker-compose.yml up -d --wait mailpit
```

Ergebnis- und Widerrufsmails verwenden dagegen gespeicherte Benachrichtigungsaufträge ohne
Key-Secrets. Ein Versandfehler führt zu `RETRY`, nach höchstens drei Wiederholungen nach 1, 5 und
15 Minuten zu `FAILED`. SMTP-Verbindungs-, Lese- und Schreibtimeout sind jeweils fünf Sekunden.
Der Versand löst keine neue Validierung aus. Nach SMTP-Annahme ohne erhaltene Bestätigung kann
eine Wiederholung eine doppelte Mail erzeugen.

## Einzelauftragsmodus nachvollziehen

Der Controller übergibt Lieferungen an `DeliverySubmissionService`; `ValidationExecutor` kapselt
JobRunr. `ValidationAttemptRunner` führt denselben Ablauf in JobRunr und `worker-single` aus.
`ArtifactStore` trennt dauerhafte Artefakte von lokalen Validator-Arbeitsdateien. Veröffentlichung
und Benachrichtigung sind separate Schritte. Cayenne-Operationen verwenden kurze, getrennte
Kontexte im gemeinsamen Runtime.

1. Die normale lokale Anwendung stoppen. Sicherstellen, dass keine andere Worker-Instanz dieselbe
   Datenbank verarbeitet. Alle JobRunr-Worker müssen angehalten sein: Nur den Dispatcher zu
   deaktivieren verhindert nicht die Ausführung bereits übergebener Jobs.
2. Den Webprozess mit deaktiviertem Dispatcher und Background-Jobserver starten:

   ```sh
   DBUSR=postgres DBPWD=secret CONFIG_DB_SCHEMA=agi_datahub_config_v1 \
     LOG_DB_SCHEMA=agi_datahub_log_v1 JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1. \
     ./gradlew bootRun --args='--spring.profiles.active=dev --app.dispatch-enabled=false --jobrunr.background-job-server.enabled=false'
   ```

3. In einem zweiten Terminal die Lieferung aus dem Lieferbeispiel erneut senden. Aus deren
   Status-URL den aktuellen offenen Versuch bestimmen:

   ```sh
   delivery_id=${delivery_status_url##*/}
   attempt_id=$(docker compose -f dev/docker-compose.yml exec -T edit-db \
     psql -U postgres -d edit -At -v ON_ERROR_STOP=1 -v delivery_id="$delivery_id" <<'SQL'
   SELECT a.id
   FROM agi_datahub_log_v1.validation_attempt a
   JOIN agi_datahub_log_v1.deliveries_delivery d ON d.t_id=a.delivery_id AND d.attempt_id=a.id
   WHERE d.jobid=:'delivery_id' AND a.state='ENQUEUED' AND a.dispatch_state='PENDING';
   SQL
   )
   printf '%s\n' "$attempt_id"
   ```

   Nur mit genau einer ausgegebenen Versuch-ID fortfahren. Die öffentliche Lieferungs-ID ist
   `deliveries_delivery.jobid`, nicht die Versuch-ID.
4. Den Einzelprozess aus dem zweiten Terminal starten. Bei mehreren vorhandenen Exec-Jars den
   Dateinamen des soeben gebauten Jars ausdrücklich einsetzen:

   ```sh
   ./gradlew bootJar
   DBUSR=postgres DBPWD=secret CONFIG_DB_SCHEMA=agi_datahub_config_v1 \
     LOG_DB_SCHEMA=agi_datahub_log_v1 JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1. \
     java -jar build/libs/datahub-*-exec.jar --spring.profiles.active=dev \
     --app.mode=worker-single --app.attempt-id="$attempt_id"
   printf 'Exitcode: %s\n' "$?"
   ```

Der Prozess startet weder HTTP noch JobRunr oder periodische Verwaltungsaufgaben. Gültige und
ungültige Daten ergeben Exitcode 0; technische Fehler und unbekannte Versuche einen von null
verschiedenen Exitcode. Der weiterlaufende Webprozess übernimmt anschliessend Veröffentlichung
und Mailversand, auch bei deaktiviertem Dispatcher. Für die ungültige und modellfremde Datei jeweils
eine neue Lieferung und deren neuen Versuch verwenden. Nach dem Test den Webprozess wieder mit
den normalen Einstellungen starten. Ein fehlgeschlagener Versuch wird nicht automatisch neu validiert.

## Modell, SQL und Cayenne aktualisieren

Flyway wird über das Werkzeug und in Tests ausgeführt, niemals beim Produktionsstart.
Das [INTERLIS-Modell](../dev/SO_AGI_Datahub_20240403.ili) bleibt die fachliche Quelle:

1. `dev/SO_AGI_Datahub_20240403.ili` ändern.
2. `jbang sql/create_schema_sql.java` erzeugt SQL-Entwürfe unter `build/model-ddl`, ohne
   Datenbankzugriff und ohne Überschreiben des historischen V1-Schemas. JBang ist nur hierfür nötig.
3. SQL, Datenübernahme und Constraints prüfen; eine neue Flyway-Migration unter `sql/migrations`
   schreiben. Physische Namen bestehender Tabellen erhalten. `python3 sql/model_metadata.py`
   erzeugt den passenden Exportmetadaten-Entwurf `build/model-ddl/metadata.sql`.
4. `devUp` mit den lokalen Migrationseinstellungen ausführen; Cayenne-Mapping prüfen/aktualisieren
   und `./gradlew cgen` ausführen. `cdbimport` verwendet die lokale Datenbank, kann jedoch
   Mapping-Details verändern: Den Diff vor Übernahme prüfen.
5. Modell, Migration, Mapping und generierte Klassen gemeinsam einchecken und `./gradlew check`
   ausführen. Tests migrieren frische und historische V1-Schemata; Dubletten müssen zum Abbruch führen.

Ein separater lokaler Migrationslauf verwendet dieselben Einstellungen:

```sh
DBURL=jdbc:postgresql://localhost:55432/edit DBUSR=postgres DBPWD=secret \
  CONFIG_DB_SCHEMA=agi_datahub_config_v1 LOG_DB_SCHEMA=agi_datahub_log_v1 \
  JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1 ./gradlew dbMigrate
```

Die einmalige Baseline einer überprüften Bestandsinstallation ist im
[Betriebshandbuch](admin-manual-de.md) beschrieben; eine vorhandene Installation wird nicht neu angelegt.

## Tests und Produktionsimage

`./gradlew test` führt schnelle Unit-Tests ohne Docker aus. `./gradlew check` ergänzt Integration
und E2E mit isoliertem PostgreSQL, echten HTTP-Requests, lokaler INTERLIS-Validierung, Mailpit,
Einzelauftragsmodus und Produktionscontainer. Dafür muss Docker laufen; die Compose-Entwicklungsdatenbank
wird durch diese Tests nicht verändert. `./gradlew build` enthält `check`.

`./gradlew buildImage` erstellt lokal `sogis/datahub:local`. Dieses Image verwendet das Profil
`prod` mit vorkompilierten JTE-Templates und benötigt explizite Datenbank- und Ablagekonfiguration
gemäss [Betriebshandbuch](admin-manual-de.md).

Der Boot-3.5-Zwischenstand wurde vor Boot 4/Jackson 3/JobRunr 8 geprüft. CI prüft Pull Requests mit
`build` inklusive `check`; Images werden erst nach erfolgreichen Checks bei Push auf `main` oder
einen Versionstag publiziert.
