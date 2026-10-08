# Betriebshandbuch

## Voraussetzungen und Ausführungsrollen

Java 21, PostgreSQL und ein zuverlässiges Filesystem bleiben erforderlich. Alle Web-/Worker-Instanzen
verwenden dieselbe Datenbank und dieselben Arbeits- und Zielverzeichnisse. JobRunr 8.8.2 verteilt
Prüfversuche; pro Instanz läuft höchstens eine Validierung. Das öffentliche Webfrontend kann mit
`JOBRUNR_SERVER_ENABLED=false` betrieben werden. Dispatcher, Veröffentlichung und Mailversand
arbeiten unabhängig vom Background-Jobserver und koordinieren sich über PostgreSQL.

Das Image startet das Profil `prod` mit vorkompilierten JTE-Templates. Schreibzugriff wird nur auf
`WORK_DIRECTORY`, `TARGET_DIRECTORY` und das Benutzerverzeichnis benötigt. Gruppenrechte für
Gruppe 0 erlauben eine zufällige OpenShift-UID. Die Containerprüfung verwendet UID 34567.

## Schema und kontrollierte Migration

Die Anwendung führt keine Flyway-Migrationen aus. `JOBRUNR_SKIP_CREATE=true` bleibt der Standard.
DDL wird vor dem gemeinsamen Anwendungswechsel mit einem Administrationskonto angewendet:

```sh
./gradlew dbMigrate
```

`DBURL`, `DBUSR`, `DBPWD`, `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA` und `JOBRUNR_DB_SCHEMA` gelten auch
für das Werkzeug. Zugangsdaten geschützt als Umgebungsvariablen bereitstellen. Beim Werkzeug
werden Schemanamen ohne abschliessenden Punkt angegeben. Für die Anwendung benötigt
`JOBRUNR_DB_SCHEMA` weiterhin den Punkt, beispielsweise `agi_datahub_jobrunr_v1.`.

Eine vorhandene, überprüfte Installation des bisherigen Schemas wird einmalig mit
`./gradlew dbMigrate -PmigrationAction=baseline` als V1 registriert. Vorher das vorhandene Schema
mit `sql/migrations/V1__existing_datahub.sql` vergleichen. Keine automatische Baseline und kein
produktives `clean` verwenden. Neuinstallationen erhalten V1 und alle Folgemigrationen.

Die Migration meldet Operatsdubletten und bricht ab. Vorher prüfen und fachlich bereinigen:

```sql
SELECT theme_r, aname, array_agg(t_id), count(*)
FROM agi_datahub_config_v1.core_operat GROUP BY theme_r, aname HAVING count(*) > 1;
```

V2 übernimmt vorhandene Keys als `ACTIVE` und kopiert bekannte Jobzustände in die Lieferhistorie.
Lieferungs-IDs bleiben erhalten. V3 registriert die bereits im alten SQL enthaltenen JobRunr-
Migrationen; V5 enthält die zusätzliche JobRunr-8-Migration. V6 ergänzt die Prüfprovenienz und
aktualisiert die aus dem INTERLIS-Modell abgeleiteten Exportmetadaten. V7 stellt die Rechte
auf der neu erzeugten JobRunr-View wieder her. Historische Lieferungen
ohne Jobdaten bleiben `UNKNOWN`; ihre Veröffentlichung und Benachrichtigung werden als `LEGACY`
gekennzeichnet. Es werden keine erfolgreichen Prüfungen für alte Endablagen erfunden.

Je Thema muss `core_theme.expected_models` eine ausdrückliche, mit Semikolon getrennte Liste der
zulässigen Transfermodelle enthalten. Ohne diese Einstellung verweigert die Anwendung die Annahme
mit 503. Abhängigkeitsmodelle im Transferheader zählen nicht automatisch als gelieferte Modelle.
Die fachliche Operatsprüfung erfolgt weiterhin über die INTERLIS-Prüfkonfiguration des Themas.

## Auslieferung und Rückfall

1. Uploadannahme in der bisherigen Umgebung pausieren, alte JobRunr-Jobs vollständig abarbeiten.
2. Datenbank, Arbeitsverzeichnis und Endablage als zusammengehörigen Stand sichern.
3. Dubletten beseitigen, Migrationen ausführen, erwartete Modelle je Thema konfigurieren.
4. Alle bisherigen Instanzen stoppen und die neue Version gemeinsam starten. Unterschiedliche
   Verarbeitungsversionen werden nicht gleichzeitig betrieben.
5. Gültige/ungültige Probe liefern, öffentliche Status-/Log-URLs, Dateipfade und SMTP prüfen;
   Uploadannahme freigeben.

`ACCEPT_DELIVERIES=false` pausiert neue Uploads (503); bestehende Aufträge laufen weiter.
Ein Rückfall stellt Anwendung, Datenbank **und** Endablage aus der gemeinsamen Sicherung wieder her.
Ein altes Image allein ist nach den Migrationen kein vollständiger Rückfallplan.

Alte Dateien bleiben an ihren bisherigen Zielpfaden verfügbar. Der neue `ilidata.xml`-Index
enthält ausschliesslich neue, nachvollziehbar validierte Veröffentlichungen. Legacy-Dateien
werden nicht automatisch in diesen Index übernommen.

## Lieferungen, Artefakte und Wiederholungen

`Delivery` führt Historie und Status; die öffentliche `jobId` ist ihre stabile UUID. Versuch-ID und
JobRunr-ID sind separate technische Identitäten. Ein Upload wird unter einer internen UUID gespeichert;
Lieferung und erster Versuch werden gemeinsam committed. Erst danach wird 202 mit
`Operation-Location` zurückgegeben. Der Dispatcher kann eine offene Übergabe erneut versuchen.
Jobargumente enthalten ausschliesslich Lieferungs- und Versuch-ID.

Versuche werden mit Datenbank-Claim, Heartbeat und Version abgesichert. Ein überalterter Claim
wird als technischer Fehler abgeschlossen. Ungültige Daten und abgebrochene Versuche werden nicht
automatisch neu validiert. Doppelte Ausführung und verspätete Ergebnisse verändern keinen neueren
Lieferungsstand. Ein neuer HTTP-POST bleibt eine neue Lieferung.

Rohdateien liegen unter `WORK_DIRECTORY/datahub_<deliveryId>/input.xtf`, Ergebnisse unter
`attempts/<attemptId>-<version>/result.log`; temporäre Validator-Dateien unter `workspaces/`.
Die konfigurierte Präfixoption bleibt verwendbar. Aufgelöste Modellversionen, Modellquellen samt
SHA-256 und Prüfkonfiguration werden dauerhaft beim Versuch gespeichert. Nicht verfügbare
Quellen werden in der Provenienz ausdrücklich gekennzeichnet.

Nur gültige Ergebnisse werden veröffentlicht. PostgreSQL sperrt je Operat; ein älterer Lieferstand
ersetzt keinen neueren veröffentlichten Stand. Im Zielfilesystem werden die beiden normalen Dateien
`Thema/Operat.xtf.log` und `Thema/Operat.xtf` einzeln atomar ersetzt. Eine gemeinsame Momentaufnahme
zweier unabhängiger Dateizugriffe wird nicht zugesichert. Eine gespeicherte Veröffentlichungsabsicht
bleibt bei Teilfehlern offen und ermöglicht eine erneute Übernahme ohne erneute Validierung.

Veröffentlichung und Ergebnis-/Widerrufsmails erhalten nach einem Fehler höchstens drei automatische
Wiederholungen nach 1, 5 und 15 Minuten. Danach bleibt `FAILED` sichtbar. SMTP bestätigt keine
fachliche Übernahme. Wenn nach Annahme einer Mail die Bestätigung verloren geht, kann ein erneuter
Versand eine doppelte Mail erzeugen; der Lieferauftrag wird dadurch nicht erneut validiert.

Arbeitsartefakte werden drei Tage nach Abschluss bereinigt. Aktive Versuche, offene Veröffentlichungen,
noch offene Ergebnisbenachrichtigungen und aktuell veröffentlichte Artefakte sind ausgeschlossen.
Liefermetadaten und der aktuelle veröffentlichte Stand bleiben bestehen. Die Übersicht zeigt 14 Tage,
auch wenn technische JobRunr-Einträge bereits gelöscht sind. Öffentliche Logs liefern nach Ablauf
410, unbekannte/nicht vorhandene Protokolle 404. Verwaiste Uploads werden erst nach Altersprüfung
und erfolgreichem Abgleich mit der Datenbank gelöscht.

## Mail und Schlüssel

`MAIL_FROM` ist der Absender; `MAIL_USERNAME` dient ausschliesslich zur SMTP-Anmeldung.
Verbindungs-, Lese- und Schreibtimeout sind jeweils fünf Sekunden. Das Dev-Profil verwendet Mailpit
auf localhost:1025 ohne Authentifizierung oder TLS; die Produktion verwendet die konfigurierten
SMTP-Zugangsdaten und TLS-Einstellungen.

Keys werden zunächst als `PENDING` mit BCrypt-Hash gespeichert. Nur der ausstellende Request hält
das Secret, versendet es einmalig per SMTP und aktiviert danach den Key. Bei Versandfehlern bleibt
kein benutzbarer neuer Key zurück. `PENDING` nach einem Prozessabbruch ist ebenfalls unbrauchbar;
eine neue Ausstellung ist notwendig. Bei `MAIL_ENABLED=false` wird die Ausstellung vor dem
Speichern abgelehnt. Secrets stehen nicht in den gespeicherten Benachrichtigungsaufträgen.

Organisationen dürfen weiterhin eigene Keys erzeugen. Die Bootstrap-Key-Ausgabe, der automatische
Widerruf bei als HTTP erkanntem Request, die öffentliche Oberfläche/Status-/Log-Endpunkte und
Tomcat-Basic-Auth der externen Dateiliste bleiben die festgelegten Betriebsentscheidungen.
TLS-Termination und Forwarded-Header müssen deshalb in der bestehenden Infrastruktur korrekt
konfiguriert sein. Links in Antworten und Mails verwenden `PUBLIC_BASE_URL`.

## Wichtige Optionen

| Option | Bedeutung / Standard |
| --- | --- |
| `DBURL`, `DBUSR`, `DBPWD` | PostgreSQL-Verbindung; administratives Werkzeug separat betreiben |
| `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA` | `agi_datahub_config_v1`, `agi_datahub_log_v1` |
| `JOBRUNR_DB_SCHEMA` | Anwendung: `agi_datahub_jobrunr_v1.`; Werkzeug: ohne Punkt |
| `JOBRUNR_SKIP_CREATE` | `true`, keine DDL-Änderungen beim Anwendungsstart |
| `JOBRUNR_SERVER_ENABLED`, `JOBRUNR_WORKER_COUNT` | `true`, `1` |
| `JOBRUNR_POLL_INTERVAL` | `10` Sekunden |
| `JOBRUNR_DASHBOARD_ENABLED` | `false`; bestehende Dashboard-Zugangsdaten bleiben konfigurierbar |
| `PUBLIC_BASE_URL` | Externe Basis-URL inklusive allfälligem Pfadpräfix; explizit setzen |
| `WORK_DIRECTORY`, `TARGET_DIRECTORY`, `FOLDER_PREFIX` | Gemeinsame Artefakt-/Zielablage und `datahub_` |
| `TIME_ZONE`, `TZ` | `Europe/Zurich`; alle Instanzen verwenden dieselbe Zeitzone für TIMESTAMP-Daten |
| `ACCEPT_DELIVERIES`, `DISPATCH_ENABLED` | `true`; getrennte Wartungsschalter |
| `MAIL_ENABLED`, `MAIL_FROM` | `false`, `datahub@example.test`; produktiv explizit konfigurieren |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP-Server und Anmeldung |
| `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` | Produktionsstandard `true` |
| `PREFERRED_ILI_REPO` | Bevorzugtes Modellrepository; Standardrepositories folgen |
| `CLEANER_ENABLED`, `CREATE_DIRECTORIES` | `true` |
| `JOB_RESPONSE_LIST_LIMIT`, `MAX_FILE_SIZE` | REST-Standardlimit `300`, Uploadgrösse `200` MB |

JobRunr-Konfiguration verwendet jetzt `jobrunr.*`; bisherige exponierte Umgebungsvariablen bleiben
bestehen. Weitere strukturierte Einstellungen stehen in `AppProperties` und `application.properties`.

## Einzelauftragsmodus

```sh
java -jar build/libs/datahub-*-exec.jar --app.mode=worker-single --app.attempt-id=UUID
```

Alternativ `APP_MODE=worker-single` und `ATTEMPT_ID` setzen. Der Prozess benötigt Datenbank- und
Arbeitsablagenzugriff sowie die Modellrepositories. Er lädt genau einen gespeicherten Versuch,
validiert, speichert das Ergebnis und beendet sich. HTTP-Endpunkte, JobRunr-Server/Storage und
periodische Verwaltungsaufgaben werden nicht gestartet. Gültige und ungültige Daten ergeben 0;
technische Fehler und unbekannte Versuche einen von null verschiedenen Exitcode. Veröffentlichung
und Mailversand erfolgen anschliessend durch die separaten Web-Verwaltungsaufgaben.
Für einen manuellen Einsatz zuerst `DISPATCH_ENABLED=false` setzen, damit JobRunr den Versuch nicht
parallel übernimmt. Dieser Modus bildet die Grundlage für eine spätere Ausführungsplattform.

## Überwachung

Die öffentliche Übersicht `/jobs` zeigt Ausführung, Validierung, Übernahme und Mail getrennt.
Offene Übergaben, überalterte laufende Versuche und Fehler sind zusätzlich in PostgreSQL sichtbar:

```sql
SELECT id, dispatch_state, state, heartbeat_at FROM agi_datahub_log_v1.validation_attempt
WHERE dispatch_state='PENDING' OR state='PROCESSING';
SELECT jobid, publication_status, notification_status FROM agi_datahub_log_v1.deliveries_delivery
WHERE publication_status IN ('RETRY','FAILED') OR notification_status IN ('RETRY','FAILED');
SELECT id, recipient, state, tries, next_at FROM agi_datahub_log_v1.notification
WHERE state <> 'SENT';
```

Keine API-Key-Secrets in Monitoringsysteme oder gespeicherte Jobargumente übernehmen.
Die externe Tomcat-Dateiliste bleibt durch ihre bisherige Basic-Authentication-Konfiguration geschützt;
die Anwendung besitzt zusätzlich die vorhandene Admin-Dateiliste unter `/ui/files` und `/api/files`.

## Konfiguration GDI

Siehe [GDI-Dokumentation](https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/datahub/datahub.md).
