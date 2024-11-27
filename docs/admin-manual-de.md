# Betriebshandbuch

## Abhängigkeiten

Der Betrieb der Anwendung erfordert eine vorhandene PostgreSQL-Datenbank und ein Filesystem.

### Datenbank

Die Datenbank wird für die Autorisierung und für das Registrieren (aka Logging) der Datenlieferungen verwendet. Die verwendete Bibliothek für die Jobqueue benötigt ebenfalls eine Datenbank zum Tracken der Jobs. Für die ersten beiden Fälle wurde je ein INTERLIS-Datenmodell geschrieben:

- `SO_AGI_Datahub_Config_20240403`: Autorisierung
- `SO_AGI_Datahub_Log_20240403`: Logging

Die zwei Datenbankschemen können mit _ili2pg_ erzeugt werden:

```
java -jar ili2pg-4.9.1.jar --dbhost localhost --dbport 54321 --dbdatabase edit --dbusr postgres --dbpwd secret --defaultSrsCode 2056 --createGeomIdx  --createFk --createFkIdx --createEnumTabs --createMetaInfo --nameByTopic --strokeArcs --createUnique --createNumChecks --createTextChecks --createDateTimeChecks --createImportTabs --createUnique --dbschema agi_datahub_config_v1 --models SO_AGI_Datahub_Config_20240403 --schemaimport
```

```
java -jar ili2pg-4.9.1.jar --dbhost localhost --dbport 54321 --dbdatabase edit --dbusr postgres --dbpwd secret --defaultSrsCode 2056 --createGeomIdx  --createFk --createFkIdx --createEnumTabs --createMetaInfo --nameByTopic --strokeArcs --createUnique --createNumChecks --createTextChecks --createDateTimeChecks --createImportTabs --createUnique --dbschema agi_datahub_log_v1 --models SO_AGI_Datahub_Log_20240403 --schemaimport
```

_Jobrunr_ (die Jobqueue-Bibliothek) erzeugt das DDL entweder automatisch (z.B. während der Entwicklung) oder man kann mit einem Java-Tool die SQL-Dateien selber herstellen. Dazu müssen die beiden Jar-Dateien `jobrunr-6.3.4.jar` und `slf4j-api-2.0.12.jar` heruntergeladen werden. 

```
java -cp jobrunr-6.3.4.jar:slf4j-api-2.0.12.jar org.jobrunr.storage.sql.common.DatabaseSqlMigrationFileProvider postgres agi_datahub_jobrunr_v1.
```

**Achtung:** Der Punkt am Ende des Schemanamens ist zwingend notwendig.

Die Inbetriebname in die GDI-SO ist im [Kapitel Konfiguration GDI](#Konfiguration-GDI) beschrieben.

Es steht ein _jbang_-Skript zur Verfügung, das die SQL-DDL-Skripte vollständig erzeugt und vom DB-Admin anschliessend verwendet werden können, um alle Schemen und Tabellen anzulegen:

```
jbang create_schema_sql.java
```

Oder falls _jbang_ nicht vorhanden ist:

```
curl -Ls https://sh.jbang.dev | bash -s - create_schema_sql.java
```

Die Schemanamen und Rollennamen sind hardcodiert im Quellcode. Im Repository ist eine `setup_gdi.sql`-Datei bereits eingecheckt.

### Filesystem

Es wird ein Filesystem für das temporäre Speichern der Dateien (während der Anlieferungsphase) und ein Zielverzeichnis für das Ablegen der Daten nach erfolgreicher Validierung benötigt. In einer geclusterten Umgebung müssen sämtliche Nodes (o.ä.) Zugriff auf das gleiche Filesystem haben. Die beiden Verzeichnisse sind als Option exponiert.

### SMTP-Server

Die Anwendung benötigt für das Verschicken von E-Mail einen SMTP-Server. Die notwendigen Einstellungen sind als Optionen exponiert. Die Optionen müssen gesetzt werden, sonst kann die Anwendung nicht gestartet werden. Es können jedoch auch Fantisiewerte eingesetzt werden. Dies führt zu einer Exception auf dem Server. Die Anlieferung kann trotzdem erfolgen, nur erhält der Lieferant keine E-Mail.

## Optionen

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `TZ` | Zeitzone des Linux-Betriebssystems. Muss auf `Europe/Zurich` gesetzt werden, falls Cluster eine andere Zeitzone verwendet. | |
| `MAX_FILE_SIZE` | Die maximale Grösse einer Datei, die hochgeladen werden kann in Megabyte. | `200` |
| `LOG_LEVEL` | Das Logging-Level des Spring Boot Frameworks. | `INFO` |
| `LOG_LEVEL_DB_CONNECTION_POOL` | Das Logging-Level des DB-Connection-Poolsocket. | `INFO` |
| `LOG_LEVEL_CAYENNE` | Das Logging-Level der ORM-Bibliothek. | `WARN` |
| `LOG_LEVEL_FTP` | Das Logging-Level des FTP-Servers. | `INFO` |
| `LOG_LEVEL_APPLICATION` | Das Logging-Level der Anwendung (= selber geschriebener Code). | `INFO` |
| `TOMCAT_THREADS_MAX` | Maximale Anzahl Threads, welche die Anwendung gleichzeitig bearbeitet. Muss abgestimmt sein mit der Anwendungscharakteristik (z.B. lange DB-Queries) und der Anzahl DB-Connections im Pool. | `5` |
| `TOMCAT_ACCEPT_COUNT` | Maximale Grösser der Queue, falls keine Threads mehr verfügbar. | `50` |
| `TOMCAT_MAX_CONNECTIONS` | Maximale Threads des Servers. | `200` |
| `HIKARI_MAX_POOL_SIZE` | Grösse des DB-Connections-Pools | `5` |
| `DBURL` | JDBC-Url für die Datenbank | `jdbc:postgresql://localhost:54321/edit` |
| `DBUSR` | Datenbankbenutzer | `postgres` |
| `DBPWD` | Datenbankpasswort | `secret` |
| `JOBRUNR_SERVER_ENABLED` | Dient die Instanz als sogenannter Background-Jobserver, d.h. werden mittels REST-API hochgeladene INTERLIS-Transferdateien validiert. Wird nur eine Instanz betrieben, muss die Option zwingend `true` sein, da sonst der Job nicht ausgeführt wird. | `true` |
| `JOBRUNR_POLL_INTERVAL` | Es wird im Intervall (in Sekunden) nach neuen Validierungsjobs geprüft. | `10` |
| `JOBRUNR_WORKER_COUNT` | Anzahl Jobs, die in einem "Worker"/Background-Server gleichzeitig durchgeführt werden können. Im Prinzip nicht sehr relevant, da der Validierungsjob synchronisiert ist (nicht thread safe). | `1` |
| `JOBRUNR_DASHBOARD_ENABLED` | Das Jobrunr-Dashboard wird auf dem Port 8000 gestartet. | `false` |
| `JOBRUNR_DASHBOARD_USER` | Username für Jobrunr-Dasboard. Achtung: Basic Authentication. | `admin` |
| `JOBRUNR_DASHBOARD_PWD` | Passwort für Jobrunr-Dasboard. Achtung: Basic Authentication. | `admin` |
| `JOBRUNR_DB_SCHEMA` | Schemanamen für Jobrunr-Tabellen und -views. Abschliessender Punkt ist zwingend. | `agi_datahub_jobrunr_v1.` |
| `JOBRUNR_SKIP_CREATE` | Sollen die Jobrunr-Tabellen automatisch erstellt werden? | `false` |
| `MAIL_ENABLED` | E-Mailversand des Prüfresultates ja/nein. | `true` |
| `MAIL_HOST` | SMTP-Host. | `smtp.elasticemail.com` |
| `MAIL_PORT` | SMTP-Port. | `2525` |
| `MAIL_USERNAME` | Benutzername / Absender der E-Mail. | |
| `MAIL_PASSWORD` | E-Mail-Passwort | |
| `MAIL_SMTP_AUTH` | - | `true` |
| `MAIL_SMTP_STARTTLS` | - | `true` |
| `CONFIG_DB_SCHEMA` | Schemanamen für die Autorisierungstabellen.  | `agi_datahub_config_v1` |
| `LOG_DB_SCHEMA` | Schemanamen für die Logtabellen | `agi_datahub_log_v1` |
| `JOB_RESPONSE_LIST_LIMIT` | Anzahl der Lieferungen, die zurückgeliefert werden sollen. Betrifft REST-API (`/api/jobs`) und GUI (`/web/jobs.xhtml`) | `300` |
| `API_KEY_HEADER_NAME` | Name des API-Key-Headers. | `X-API-KEY` |
| `ADMIN_ACCOUNT_INIT` | Soll beim Starten der Anwendung eine Admin-Organisation im Autorisierungschema erstellt werden? Fall bereits eine Organisation mit gleichem Namen vorhanden ist, wird nichts gemacht. Der API-Key wird einmalig nach Stdout geloggt. | `true` |
| `ADMIN_ACCOUNT_NAME` | Name der Admin-Organisation | `AGI SO` |
| `ADMIN_ACCOUNT_MAIL` | E-Mail-Adresse der Admin-Organisation | `stefan.ziegler@bd.so.ch` |
| `WORK_DIRECTORY` | Verzeichnis, in das die hochgeladenen Dateien temporär gespeichtert werden. | `/Users/stefan/tmp/` |
| `FOLDER_PREFIX` | Prefix für das pro Anlieferung temporär erstellt Verzeichnis im `WORK_DIRECTORY` | `datahub_` |
| `TARGET_DIRECTORY` | Zielverzeichnis, in das erfolgreich geprüfte Daten gespeichert werden. Nicht vorhandene Themenverzeichnisse werden erstellt. | `/Users/stefan/tmp/target_datahub/` |
| `CREATE_DIRECTORIES` | Die Anwendung versucht beim Starten die beiden Verzeichnisse `WORK_DIRECTORY` und `TARGET_DIRECTORY` zu erstellen. | `true` |
| `PREFERRED_ILI_REPO` | Modellrepositories, die bei der Modellsuche prioritär berücksichtigt werden. | `https://geo.so.ch/models` |
| `CLEANER_ENABLED` | Soll der Cronjob, welcher veraltete Daten aus dem  `WORK_DIRECTORY` löscht, eingeschaltet werden. | `true` |
| `HTTP_WHITELIST` | Liste mit Hostnamen für die ein HTTP-Request (ohne "s") _nicht_ zum deaktiveren des API-Keys führt. | `localhost,datahub-agi-apps-test.apps.ocp.so.ch` |
| `FTP_ENABLED` | FTP-Server einschalten. | `false` |
| `FTP_USERS_FILE` | Dateipfad der Userdatei für den FTP-Server, die beim Hochfahren leer erstellt wird. Eine vorhandende Datei wird überschrieben. | `/tmp/ftp_user.properties` |
| `FTP_PORT` | Port des FTP-Servers. | `21` |
| `FTP_USERNAME` | Read-only FTP-Benutzer. | `admin` |
| `FTP_PASSWORD` | Read-only FTP-Passwort. | `admin` |
| `DIRECTORY_LISTING_USERNAME` | Benutzername für Directory Listing des `TARGET_DIRECTORY`. | `admin` |
| `DIRECTORY_LISTING_PASSWORD` | Password für Directory Listing des `TARGET_DIRECTORY`. | `admin` |

## Autorisierung

Siehe [Benutzerhandbuch](user-manual-de.md) -> Autorisierung / Konfiguration

## Rest-API

Das Rest-API verwendet folgende Befehle (Verben), Header und Statuscodes. Insbesondere die Statuscodes müssen transparent dem Benutzer transparent zurückgeliefert werden.

### Befehle

- GET:
  - "/api/jobs"
  - "/api/jobs/{jobId}"
  - "/api/logs/{jobId}"
  - "/ping"
  - "/protected/hello"
  - "/public/hello"
- POST:
  - "/api/deliveries"
  - "/api/keys"
- DELETE:
  - "/api/keys/{apiKey}"

### Header

- "X-API-KEY" muss von WAF etc. weitergeleitet werden.

### Statuscode

- 200
- 202
- 401
- 403

### Varia

- Dateigrössen bis 200 MB müssen hochgeladen werden können.

## Cluster

Idealerweise wird _datahub_ in einem einfachen Cluster betrieben. Eine Instanz ist verantworlich für die Entgegennahme der Dateien und die Jobqueue. Mindestens eine zweite Instanz ist für das Abbarbeiten der Jobs aus der Jobqueue zuständig. Somit verhindert man, dass eine hohe Joblast auf die Kommunikation mit dem Benutzer negativen Einfluss hat. Es können beliebig viele "Worker"-Instanzen hochgefahren werden. Diese müssten nicht im Internet exponiert werden. Sie müssen lediglich Zugriff auf die Datenbank haben und das gleiche Filesystem (Volume) teilen (Optionen `WORK_DIRECTORY` und `TARGET_DIRECTORY`)

## FTP-Server

Der FTP-Server kann ein- und ausgeschaltet (`FTP_ENABLED`). Standardmässig läuft er auf dem Port 2221 (`FTP_PORT`). In Openshift darf ein not-root-Image nicht Ports < 1024 verwenden. Es müssten zwingend die passiv Ports geöffnet sein. Diese sind momentan hardodiert (2121-2199).

Folgender Docker-Befehl funktioniert lokal:

```
docker run -p8080:8080 -p2221:2221 -p 2121-2199:2121-2199 -e MAIL_PASSWORD=foo -e MAIL_USERNAME=bar -e DBURL=jdbc:postgresql://docker.for.mac.host.internal:54321/edit -e CREATE_DIRECTORIES=false -e WORK_DIRECTORY=/tmp -e TARGET_DIRECTORY=/tmp  sogis/datahub:0
```

## Directory Listing

Auf dem Endpunkt `/datahub`ist ein read-only Directory Listing vorhanden. 


## Konfiguration GDI

Siehe [sogis/doc](https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/datahub/datahub.md)

