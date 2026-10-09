# Betriebshandbuch

## Voraussetzungen und Ausführungsrollen

Java 21, PostgreSQL und ein zuverlässiges Filesystem sind erforderlich. Alle Web-/Worker-Instanzen
verwenden dieselbe Datenbank sowie dieselben Arbeits- und Zielablagen. JobRunr 8.8.2 verteilt die
Prüfversuche. Standardmässig läuft pro Instanz eine Validierung; Dispatcher, Veröffentlichung und
Mailversand koordinieren sich unabhängig vom Background-Jobserver über PostgreSQL.

Ein Webfrontend kann mit `JOBRUNR_SERVER_ENABLED=false` laufen. Dann muss mindestens eine andere
Instanz mit aktiviertem JobRunr-Server dieselbe Datenbank und Ablage verwenden, damit Prüfungen
ausgeführt werden. Diese Worker-Instanz ist ebenfalls ein Webprozess, dessen HTTP-Port nicht nach
aussen veröffentlicht werden muss. `worker-single` ist dagegen ein separater Einzelauftragsmodus.

Das Produktionsimage aktiviert `prod` und verwendet vorkompilierte JTE-Templates. Arbeits- und
Zielverzeichnis sowie das Benutzerverzeichnis müssen beschreibbar sein. Das Image unterstützt
Gruppe 0 und zufällige OpenShift-UIDs; die Containerprüfung verwendet UID 34567. Host-Mounts müssen
passende Gruppenrechte besitzen. Lokale Pfadvoreinstellungen aus `application.properties` sind
keine geeignete Containerkonfiguration: Produktionsablagen immer ausdrücklich setzen.

## Schema und kontrollierte Migration

Die Anwendung führt keine Flyway-Migrationen aus. `JOBRUNR_SKIP_CREATE=true` verhindert auch
JobRunr-DDL beim Start. Schemaänderungen werden mit einem Administrationskonto vor dem gemeinsamen
Versionswechsel ausgeführt; das Laufzeitkonto benötigt die vorgesehenen Datenzugriffsrechte.

Das Werkzeug liest `DBURL`, `DBUSR`, `DBPWD`, `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA` und
`JOBRUNR_DB_SCHEMA`. Schemaoptionen müssen einfache Namen nach `[a-z][a-z0-9_]*` enthalten, ohne
abschliessenden Punkt. Flyway registriert seine Historie in der Default-Schemaauflösung der
Werkzeugverbindung; bei Folgeaufrufen dieselbe Datenbank und Schemaauflösung verwenden.

Ein vollständiger Werkzeugaufruf aus dem Repository-Root sieht so aus. Die drei
`DATAHUB_MIGRATION_*`-Variablen vorher über eine geschützte Umgebung bereitstellen; fehlt eine,
bricht die Shell vor dem Aufruf ab:

```sh
DBURL="${DATAHUB_MIGRATION_URL:?Migration-URL fehlt}" \
  DBUSR="${DATAHUB_MIGRATION_USER:?Migrationskonto fehlt}" \
  DBPWD="${DATAHUB_MIGRATION_PASSWORD:?Migrationspasswort fehlt}" \
  CONFIG_DB_SCHEMA=agi_datahub_config_v1 LOG_DB_SCHEMA=agi_datahub_log_v1 \
  JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1 ./gradlew dbMigrate
```

Für die **Anwendung** benötigt `JOBRUNR_DB_SCHEMA` dagegen den Punkt, beispielsweise
`agi_datahub_jobrunr_v1.`: JobRunr verwendet den Wert als Tabellenpräfix. Diese Laufzeitvariable
nicht unverändert für das Werkzeug übernehmen. Abweichende Schemanamen in allen Werkzeug- und
Laufzeiteinstellungen gemeinsam anpassen; sie werden auch auf das Cayenne-Mapping angewendet.

### Neuinstallation und Bestandsinstallation

Bei einer neuen Datenbank führt `dbMigrate` V1 und alle Folgemigrationen aus. Das separate
Laufzeitkonto benötigt die Mitgliedschaft in der von den Migrationen eingerichteten Rolle
`datahub`, beispielsweise `GRANT datahub TO datahub_runtime`. Login und Zugangsdaten richtet der
Datenbankbetrieb ein. Anschliessend Organisationen, Operate und Themenkonfiguration kontrolliert
einrichten. Es gibt keine produktiven Demo-Daten.

Bei einer bestehenden Installation das Schema zuerst mit
`sql/migrations/V1__existing_datahub.sql` vergleichen. Nur eine überprüfte V1-Installation ohne
Flyway-Historie wird einmalig als V1 registriert: Den Werkzeugaufruf oben mit
`./gradlew dbMigrate -PmigrationAction=baseline` als letzter Befehlszeile verwenden und danach den
normalen Migrationsaufruf ausführen. Es gibt keine automatische Baseline; `clean` ist im Werkzeug
deaktiviert. Das historische `sql/setup_gdi.sql` ersetzt den aktuellen Migrationslauf nicht.

Vorher Operatsdubletten prüfen und fachlich bereinigen; die Migration meldet Konflikte und bricht ab:

```sql
SELECT theme_r, aname, array_agg(t_id), count(*)
FROM agi_datahub_config_v1.core_operat
GROUP BY theme_r, aname HAVING count(*) > 1;
```

V2 übernimmt vorhandene Keys als `ACTIVE` und bekannte Jobzustände in die Lieferhistorie.
Lieferungs-IDs bleiben erhalten. V3 registriert die bereits im alten SQL enthaltenen JobRunr-
Migrationen; V5 enthält die zusätzliche JobRunr-8-Migration. V6 ergänzt Prüfprovenienz und
Exportmetadaten; V7 stellt die Rechte auf der JobRunr-View wieder her. Historische Lieferungen
ohne Jobinformationen bleiben `UNKNOWN`; Übernahme und Benachrichtigung sind `LEGACY`.
Fehlende Artefakte und unvollständige Altinformationen sind kein erfolgreicher Validierungsnachweis.

Je Thema `core_theme.expected_models` konfigurieren: eine ausdrückliche, mit Semikolon getrennte
Liste der erwarteten Transfermodelle. Fehlt sie, verweigert die Anwendung neue Lieferungen mit
`503`. Reine Abhängigkeitsdeklarationen im Transferheader zählen nicht automatisch als gelieferte
Modelle. Die fachliche Operatsprüfung bleibt bei den INTERLIS-Regeln der Prüfkonfiguration.
Modell, SQL-Migrationen und Mapping werden gemäss [Entwicklerhandbuch](develop-manual-de.md) gemeinsam gepflegt.

## Auslieferung und Rückfall

1. Uploadannahme pausieren, alte JobRunr-Jobs vollständig abarbeiten. Unterstützt die bisherige
   Version `ACCEPT_DELIVERIES` noch nicht, die Annahme an der bestehenden Infrastruktur sperren.
2. Alle bisherigen Instanzen stoppen. Datenbank, Arbeitsablage und Endablage als zusammengehörigen
   ruhenden Stand sichern; Imageversion und Konfiguration festhalten.
3. Dubletten bereinigen, geprüfte Baseline beziehungsweise Migrationen mit dem Administrationskonto
   ausführen und erwartete Modelle je Thema konfigurieren.
4. Alle Instanzen koordiniert mit der neuen Version starten, zunächst mit `ACCEPT_DELIVERIES=false`.
   Unterschiedliche Verarbeitungsversionen werden nicht gleichzeitig betrieben.
5. Für einen kontrollierten Probelieferanten die Annahme freigeben; gültige und ungültige Datei,
   öffentliche Status-/Log-URLs, normale Dateipfade und SMTP prüfen. Danach reguläre Annahme freigeben.

`ACCEPT_DELIVERIES=false` antwortet auf neue Uploads mit `503`; bestehende Aufträge laufen weiter.
Ein Rückfall stellt Anwendung, Datenbank **und** Ablagen aus der gemeinsamen Sicherung wieder her.
Ein altes Image allein ist nach der Migration kein vollständiger Rückfall.

Alte Dateien bleiben an ihren Zielpfaden verfügbar. Der neue `ilidata.xml`-Index enthält nur neue,
nachvollziehbar gültige Veröffentlichungen; Legacy-Endablagen werden nicht automatisch aufgenommen.

## Laufzeitkonfiguration und Startbeispiel

Die Tabellen zeigen die Einstellungen ohne `dev`-Profil. Bestehende Umgebungsvariablen bleiben
verfügbar; JobRunr-Spring-Einstellungen verwenden jetzt `jobrunr.*`. Strukturierte Anwendungseinstellungen
stehen in `AppProperties` und `application.properties`.

### Datenbank und Ausführung

| Option | Bedeutung / Voreinstellung |
| --- | --- |
| `DBURL`, `DBUSR`, `DBPWD` | PostgreSQL; lokal ohne Profil `jdbc:postgresql://localhost:54321/edit`, `postgres`, `secret`; produktiv ausdrücklich setzen |
| `HIKARI_MAX_POOL_SIZE` | Poolgrösse `5` |
| `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA` | `agi_datahub_config_v1`, `agi_datahub_log_v1` |
| `JOBRUNR_DB_SCHEMA` | Laufzeit `agi_datahub_jobrunr_v1.`; Werkzeug ohne Punkt |
| `JOBRUNR_SKIP_CREATE` | `true`; keine JobRunr-DDL beim Start |
| `JOBRUNR_SERVER_ENABLED`, `JOBRUNR_WORKER_COUNT` | `true`, `1` |
| `JOBRUNR_POLL_INTERVAL` | `10` Sekunden |
| `JOBRUNR_DASHBOARD_ENABLED` | `false` |
| `JOBRUNR_DASHBOARD_USER`, `JOBRUNR_DASHBOARD_PWD` | Dashboard-Anmeldung, beide `admin`; bei Aktivierung ausdrücklich setzen; unabhängig von Tomcat-Dateilistenzugang |
| `APP_MODE`, `ATTEMPT_ID` | `web`; im Modus `worker-single` ist eine gespeicherte Versuch-ID erforderlich |
| `ACCEPT_DELIVERIES`, `DISPATCH_ENABLED` | Beide `true`; neue Annahme und Übergabe getrennt schaltbar |
| `TIME_ZONE`, `TZ` | Anwendungsuhr beziehungsweise Prozesszeitzone; beide auf `Europe/Zurich` abstimmen |

### Ablage, Modelle und öffentliche URLs

| Option | Bedeutung / Voreinstellung |
| --- | --- |
| `WORK_DIRECTORY`, `TARGET_DIRECTORY` | Gemeinsame Arbeits-/Zielablagen; produktiv absolute Containerpfade setzen |
| `FOLDER_PREFIX` | `datahub_` |
| `CREATE_DIRECTORIES`, `CLEANER_ENABLED` | Beide `true`; fehlende Zielverzeichnisse anlegen / Bereinigung aktivieren |
| `PUBLIC_BASE_URL` | `http://localhost:8080`; externen HTTPS-Ursprung inklusive allfälligem Pfadpräfix setzen |
| `PREFERRED_ILI_REPO` | `https://geo.so.ch/models`; danach folgen die Validator-Standardrepositories |
| `JOB_RESPONSE_LIST_LIMIT` | REST-Standardlimit `300`; Listenzeitraum vierzehn Tage |
| `MAX_FILE_SIZE` | Datei- und Requestlimit, beide `200` MB |
| `API_KEY_HEADER_NAME`, `API_KEY_QUERY_PARAM_NAME` | `X-API-KEY`, `token`; Query-Zugriff nur für die geschützte Dateiliste |

### Mail und Bootstrap

| Option | Bedeutung / Voreinstellung |
| --- | --- |
| `MAIL_ENABLED`, `MAIL_FROM` | `false`, `datahub@example.test`; Absender unabhängig vom SMTP-Benutzernamen |
| `MAIL_HOST`, `MAIL_PORT` | `smtp.elasticemail.com`, `2525`; produktiv ausdrücklich setzen |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP-Anmeldung; produktiv aus geschützter Konfiguration |
| `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` | Beide `true`; Dev-Profil deaktiviert beide |
| `ADMIN_ACCOUNT_INIT` | Ohne Dev-Profil `true`: fehlende Admin-Organisation samt Bootstrap-Key erzeugen |
| `ADMIN_ACCOUNT_NAME`, `ADMIN_ACCOUNT_MAIL` | Name und Adresse der Bootstrap-Organisation; produktiv ausdrücklich setzen |
| `HTTP_WHITELIST` | Hosts ohne schemaabhängigen Widerruf; Voreinstellung `localhost,datahub-agi-apps-test.apps.ocp.so.ch` |

Verbindungs-, Lese- und Schreibtimeout des SMTP-Clients sind jeweils fest auf fünf Sekunden
konfiguriert. `MAIL_ENABLED=false` verhindert neue Key-Ausstellungen; Ergebnis-Mails einer dann
angenommenen Lieferung erhalten `DISABLED`.

### Weitere Spring-Einstellungen

| Einstellung | Bedeutung / Voreinstellung |
| --- | --- |
| `TOMCAT_THREADS_MAX`, `TOMCAT_ACCEPT_COUNT`, `TOMCAT_MAX_CONNECTIONS` | `5`, `50`, `200` |
| `LOG_LEVEL_SPRING`, `LOG_LEVEL_DB_CONNECTION_POOL`, `LOG_LEVEL_CAYENNE`, `LOG_LEVEL_APPLICATION` | `INFO`, `INFO`, `WARN`, `DEBUG` |
| `app.heartbeat-timeout` | Claim-Timeout `2m` |
| `app.artifact-retention` | Arbeitsartefakte und öffentliche Logs `3d` |
| `app.default-ili-repositories` | `true`; im Dev-Profil `false` |
| `app.dispatchInterval`, `app.followUpInterval` | Je `5000` Millisekunden; Bereinigung separat stündlich |

Die letzten Einstellungen können als Spring-Properties beziehungsweise CLI-Argumente gesetzt
werden. Alle Instanzen müssen dieselbe Zeitzone und dieselben Aufbewahrungsregeln verwenden.

### Produktionscontainer starten

Beispiel für eine Instanz mit Weboberfläche und einem JobRunr-Worker. Datenbank vorher migrieren,
Hostablagen anlegen und für die Containergruppe 0 beschreibbar machen. Die Werte in
`/etc/datahub/runtime.env` durch die tatsächliche Betriebskonfiguration ersetzen; diese Datei
mit produktiven Zugangsdaten gehört nicht ins Repository:

```dotenv
DBURL=jdbc:postgresql://postgres.internal:5432/datahub
DBUSR=datahub_runtime
DBPWD=REPLACE_WITH_SECRET
CONFIG_DB_SCHEMA=agi_datahub_config_v1
LOG_DB_SCHEMA=agi_datahub_log_v1
JOBRUNR_DB_SCHEMA=agi_datahub_jobrunr_v1.
JOBRUNR_SKIP_CREATE=true
JOBRUNR_SERVER_ENABLED=true
JOBRUNR_WORKER_COUNT=1
JOBRUNR_DASHBOARD_ENABLED=false
WORK_DIRECTORY=/data/work
TARGET_DIRECTORY=/data/target
PUBLIC_BASE_URL=https://datahub.example.org
TIME_ZONE=Europe/Zurich
TZ=Europe/Zurich
ADMIN_ACCOUNT_INIT=false
ADMIN_ACCOUNT_NAME=Example Admin
ADMIN_ACCOUNT_MAIL=admin@example.org
MAIL_ENABLED=true
MAIL_FROM=datahub@example.org
MAIL_HOST=smtp.internal
MAIL_PORT=587
MAIL_USERNAME=REPLACE_WITH_SMTP_USER
MAIL_PASSWORD=REPLACE_WITH_SMTP_SECRET
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
ACCEPT_DELIVERIES=true
DISPATCH_ENABLED=true
```

Das Beispiel setzt eine bereits vorhandene Admin-Organisation voraus. Für einen kontrollierten
Erststart kann `ADMIN_ACCOUNT_INIT=true` den Bootstrap-Key erzeugen; die ausdrücklich vorgesehene
Logausgabe bleibt erhalten. Bei getrennten Rollen beim Webfrontend `JOBRUNR_SERVER_ENABLED=false`
setzen und mindestens einen Worker mit `true` und denselben Ablagen betreiben.

```sh
docker run --rm --name datahub --user 34567:0 \
  -p 127.0.0.1:8080:8080 --env-file /etc/datahub/runtime.env \
  --mount type=bind,source=/srv/datahub/work,target=/data/work \
  --mount type=bind,source=/srv/datahub/target,target=/data/target \
  sogis/datahub:local
```

`sogis/datahub:local` stammt aus `./gradlew buildImage`; produktiv die freigegebene Imageversion
einsetzen. TLS-Termination, Pfadpräfix und bereinigte Forwarded-Header werden durch die bestehende
Infrastruktur bereitgestellt. Spring verwendet `server.forward-headers-strategy=FRAMEWORK`.
`PUBLIC_BASE_URL` bestimmt Links in Antworten und Mails; es setzt keinen Servlet-Kontextpfad.
Die öffentliche Health-URL ist `/actuator/health`.

## Lieferungen, Artefakte und Wiederholungen

`Delivery` führt Historie und Status; die öffentliche `jobId` ist ihre stabile UUID. Die Versuch-ID
ist davon unabhängig; der JobRunr-Adapter verwendet sie auch als technische Job-ID.
Upload, Lieferung und erster Versuch werden vor
der `202`-Antwort dauerhaft gespeichert. Jobargumente enthalten nur Lieferungs- und Versuch-ID.

Claims, Heartbeat und Version schützen vor doppelter Ausführung und verspäteten Ergebnissen.
Ein überalterter Claim wird als technischer Fehler abgeschlossen. Ungültige Daten und technisch
abgebrochene Versuche werden nicht automatisch neu validiert. Ein neuer POST bleibt eine neue Lieferung.

Mit dem Standardpräfix liegen Eingaben unter
`WORK_DIRECTORY/datahub_<deliveryId>/input.xtf`, Ergebnisse darunter in
`attempts/<attemptId>-<version>/result.log`. Temporäre Validator-Dateien liegen unter
`WORK_DIRECTORY/workspaces/`. Modellversionen, Modellquellen samt SHA-256, Validator-Version und
Prüfkonfiguration werden beim Versuch gespeichert; nicht verfügbare Quellen sind gekennzeichnet.

Nur gültige Ergebnisse werden veröffentlicht. PostgreSQL sperrt je Operat; ein älterer Lieferstand
ersetzt keinen neueren gültigen Stand. `Thema/Operat.xtf.log` und `Thema/Operat.xtf` werden einzeln
atomar ersetzt. Eine gemeinsame Momentaufnahme zweier unabhängiger Lesezugriffe wird nicht zugesichert.
Die gespeicherte Veröffentlichungsabsicht ermöglicht Wiederholung nach Teilfehlern ohne neue Prüfung.

| Schritt | Verhalten nach Fehler |
| --- | --- |
| Übergabe an JobRunr | Bleibt bei offenem `ENQUEUED`-Versuch `PENDING`; Dispatcher versucht erneut, standardmässig alle fünf Sekunden |
| Validierung | Ungültige Daten: `SUCCEEDED` / `validationStatus=FAILED`; technischer Abbruch: `FAILED`, ohne automatischen neuen Versuch |
| Veröffentlichung | Drei Wiederholungen nach 1, 5 und 15 Minuten; danach `FAILED` |
| Ergebnis-/Widerrufsmail | Drei Wiederholungen nach 1, 5 und 15 Minuten; danach `FAILED` |
| Key-Ausstellung | Einmaliger Versand im Request; Fehler lässt keinen verwendbaren neuen Key zurück |

Drei Wiederholungen bedeuten höchstens vier Gesamtversuche einschliesslich des ersten Versuchs.
Die Wartezeit beginnt jeweils nach dem fehlgeschlagenen Versuch. SMTP-Annahme bestätigt keine
fachliche Übernahme; bei verlorener Versandbestätigung kann eine Wiederholung eine doppelte Mail erzeugen.

### Aufbewahrung

Arbeitsartefakte werden standardmässig drei Tage nach `Delivery.finished_at` bereinigt. Die Prüfung
setzt diese Abschlusszeit; anschliessende Übernahmeversuche aktualisieren sie. Mailversand aktualisiert
sie nicht. Aktive Versuche, offene Veröffentlichungen/Benachrichtigungen und aktuell veröffentlichte
Artefakte werden von der Bereinigung ausgeschlossen. Verwaiste Uploads werden erst nach Altersprüfung
und erfolgreichem Datenbankabgleich entfernt. Liefermetadaten und aktuelle Endablagen bleiben erhalten.

Die Übersicht zeigt vierzehn Tage unabhängig von der JobRunr-Aufbewahrung. Die öffentliche Log-URL
liefert nach Ablauf der Dreitagesfrist `410`, auch wenn eine Datei aufgrund einer Bereinigungsausnahme
noch existiert. Unbekannte Lieferungen oder fehlende Protokolle liefern `404`. Die normale veröffentlichte
Datei `Thema/Operat.xtf.log` unterliegt nicht dieser öffentlichen Arbeitsprotokoll-URL.

## Schlüssel und festgelegte Sicherheitsentscheidungen

Keys werden zuerst als `PENDING` mit BCrypt-Hash gespeichert. Der ausstellende Request hält das
Secret, versendet es einmalig und aktiviert danach den Key. Bei Fehler bleibt der Key unbrauchbar;
`PENDING` nach Prozessabbruch ist ebenfalls unbrauchbar und erfordert eine neue Ausstellung.
Secrets stehen nicht in gespeicherten Benachrichtigungsaufträgen.

Organisationen dürfen weiterhin eigene Keys erzeugen. Bootstrap-Key-Ausgabe, automatischer Widerruf
bei erkanntem HTTP ausserhalb der Whitelist, öffentliche Oberfläche/Status-/Log-Endpunkte und
Tomcat-Basic-Auth der externen Dateiliste bleiben die festgelegten Betriebsentscheidungen.
Die integrierte Dateiliste `/ui/files` beziehungsweise `/api/files` erfordert dagegen einen Admin-API-Key.
Tomcat-URLs und Basic-Zugangsdaten sind installationsabhängig; Dashboard-Zugangsdaten sind davon unabhängig.

## Einzelauftragsmodus

Vor einem manuellen Lauf alle JobRunr-Worker für diese Datenbank anhalten und den Dispatcher auf
allen verbleibenden Webinstanzen mit `DISPATCH_ENABLED=false` deaktivieren. Das Deaktivieren des
Dispatchers allein hält bereits übergebene Jobs nicht an. Einen neuen, noch nicht übergebenen
`ENQUEUED`-Versuch wählen; die Abfrage und ein vollständiger lokaler Ablauf stehen im
[Entwicklerhandbuch](develop-manual-de.md#einzelauftragsmodus-nachvollziehen).

```sh
java -jar build/libs/datahub-*-exec.jar --app.mode=worker-single --app.attempt-id=UUID
```

Den aktuellen Jar-Dateinamen und die echte Versuch-ID einsetzen. Alternativ `APP_MODE=worker-single`
und `ATTEMPT_ID` setzen. Datenbank, Schemaoptionen, Zeitzone, Modellrepositories und Arbeitsablage
müssen zur Webinstanz passen. Der Prozess startet keine HTTP-Endpunkte, keinen JobRunr-Server/Storage
und keine periodischen Verwaltungsaufgaben. Gültige und ungültige Dateien ergeben Exitcode 0;
technische Fehler und unbekannte Versuche einen von null verschiedenen Exitcode.

Veröffentlichung und Mailversand erfolgen durch die separaten Web-Verwaltungsaufgaben. Diese laufen
auch bei deaktiviertem Dispatcher und JobRunr-Server. Nach Abschluss die normalen Ausführungsrollen
wiederherstellen. Der Modus bereitet eine spätere Ausführungsplattform vor; Kubernetes ist nicht umgesetzt.

## Überwachung und Fehlerdiagnose

Die öffentliche Übersicht `/jobs` zeigt Ausführung, Validierung, Übernahme und Mail getrennt.
Folgende Abfragen verwenden die Standardschemata und `Europe/Zurich`; bei abweichender Konfiguration
Namen und Zeitzone gemeinsam anpassen:

```sql
-- Nur offene Übergaben des aktuellen Versuchs, keine abgeschlossenen Einzelaufträge.
SELECT d.jobid, a.id, a.dispatch_state, a.state, a.created_at
FROM agi_datahub_log_v1.validation_attempt a
JOIN agi_datahub_log_v1.deliveries_delivery d ON d.t_id=a.delivery_id AND d.attempt_id=a.id
WHERE a.dispatch_state='PENDING' AND a.state='ENQUEUED';

-- Laufende Versuche mit überaltertem Heartbeat, passend zum standardmässigen Claim-Timeout.
SELECT id, delivery_id, state, heartbeat_at
FROM agi_datahub_log_v1.validation_attempt
WHERE state='PROCESSING'
  AND COALESCE(heartbeat_at, started_at, created_at)
      < (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Zurich') - INTERVAL '2 minutes';

SELECT jobid, publication_status, publication_tries, publication_next_at, notification_status
FROM agi_datahub_log_v1.deliveries_delivery
WHERE publication_status IN ('RETRY','FAILED') OR notification_status IN ('RETRY','FAILED');

SELECT id, recipient, state, tries, next_at, lease_until
FROM agi_datahub_log_v1.notification WHERE state <> 'SENT';
```

Bei offenen Übergaben JobRunr-/Datenbankverfügbarkeit und Ausführungsrollen prüfen. Bei alten Claims
Workerprozess, Speicherzugriff und Validatorfehler untersuchen. Bei Übernahmefehlern Zielrechte,
freien Speicher und beide Zieldateien prüfen; bei Mailfehlern SMTP-Erreichbarkeit, Anmeldung, TLS
und Absender prüfen. `RETRY` bleibt bis zum nächsten vorgesehenen Versuch sichtbar.

Nach Ausschöpfen der Wiederholungen bleibt `FAILED` erhalten; es gibt keinen allgemeinen
Wiederholungsendpunkt. Fehlerursache und betroffene Lieferungs-/Versuch-ID sichern. Eine notwendige
Neulieferung bewusst als neuen POST auslösen; Status-/Versionszähler nicht pauschal zurücksetzen.
Keine API-Key-Secrets in Monitoring oder Jobargumente übernehmen.

## Konfiguration GDI

Siehe [GDI-Dokumentation](https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/datahub/datahub.md)
für installationsbezogene Infrastruktur. Diese separate Anleitung wurde bei der Dokumentationskorrektur
nicht aktualisiert. Für den aktuellen Migrationsablauf und die Anwendungskonfiguration gilt dieses Betriebshandbuch.
