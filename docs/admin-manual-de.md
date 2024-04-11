# Betriebshandbuch

## Abhängigkeiten

Der Betrieb der Anwendung erfordert eine vorhandene PostgreSQL-Datenbank und ein Filesystem.

### Datenbank

Die Datenbank wird für die Autorisierung und für das Registrieren (aka Logging) der Datenlieferungen verwendet. Die verwendete Bibliothek für die Jobqueue benötigt ebenfalls eine Datenbank zum Tracken der Jobs. Für die ersten beiden fälle wurde je ein INTERLIS-Datenmodell geschrieben:

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

### Filesystem

Es wird ein Filesystem für das temporäre Speichern der Dateien (während der Anlieferungsphase) und ein Zielverzeichnis für das Ablegen der Daten nach erfolgreicher Validierung benötigt. In einer geclusterten Umgebung müssen sämtliche Nodes (o.ä.) Zugriff auf das gleiche Filesystem haben. Die beiden Verzeichnisse sind als Option exponiert.

### SMTP-Server

Die Anwendung benötigt für das Verschicken von E-Mail einen SMTP-Server. Die notwendigen Einstellungen sind als Optionen exponiert. Die Optionen müssen gesetzt werden, sonst kann die Anwendung nicht gestartet werden. Es können jedoch auch Fantisiewerte eingesetzt werden. Dies führt zu einer Exception auf dem Server. Die Anlieferung kann trotzdem erfolgen, nur erhält der Lieferant keine E-Mail.

## Optionen

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `MAX_FILE_SIZE` | Die maximale Grösse einer Datei, die hochgeladen werden kann in Megabyte. | `200` |
| `LOG_LEVEL` | Das Logging-Level des Spring Boot Frameworks. | `INFO` |
| `LOG_LEVEL_DB_CONNECTION_POOL` | Das Logging-Level des DB-Connection-Poolsocket. | `INFO` |
| `LOG_LEVEL_CAYENNE` | Das Logging-Level der ORM-Bibliothek. | `WARN` |
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
| `MAIL_HOST` | SMTP-Host. | `smtp.elasticemail.com` |
| `MAIL_PORT` | SMTP-Port. | `2525` |
| `MAIL_USERNAME` | Benutzername / Absender der E-Mail. | |
| `MAIL_PASSWORD` | E-Mail-Passwort. | |
| `MAIL_SMTP_AUTH` | - | `true` |
| `MAIL_SMTP_STARTTLS` | - | `true` |
| `CONFIG_DB_SCHEMA` | Schemanamen für die Autorisierungstabellen.  | `agi_datahub_config_v1` |
| `LOG_DB_SCHEMA` | Schemanamen für die Logtabellen | `agi_datahub_log_v1` |
| `JOB_RESPONSE_LIST_LIMIT` | Anzahl der Lieferungen, die zurückgeliefert werden sollen. Betrifft REST-API (`/api/jobs`) und GUI (`/web/jobs.xhtml`) | `300` |
| `API_KEY_HEADER_NAME` | Name des API-Key-Headers. | `X-API-KEY` |
| `ADMIN_ACCOUNT_INIT` | Soll beim Starten der Anwendung eine Admin-Organisation im Autorisierungschema erstellt werden? Fall bereits eine Organisation mit gleichem Namen vorhanden ist, wird nichts gemacht. Der API-Key wird einmalig nach Stdout geloggt. | `true` |
| `ADMIN_ACCOUNT_NAME` | Name der Admin-Organisation | `AGI SO` |
| `ADMIN_ACCOUNT_MAIL` | E-Mail-Adresse der Admin-Organisation | `stefan.ziegler@bd.so.ch` |
| `WORK_DIRECTORY` | Verzeichnis, in das die hochgeladenen Dateien temporär gespeichter werden. | `/Users/stefan/tmp/` |
| `FOLDER_PREFIX` | Prefix für das pro Anlieferung temporär erstellt Verzeichnis im `WORK_DIRECTORY` | `datahub_` |
| `TARGET_DIRECTORY` | Zielverzeichnis, in das erfolgreich geprüfte Daten gespeichert werden. Nicht vorhandene Themenverzeichnisse werden erstellt. | `/Users/stefan/tmp/target_datahub/` |
| `PREFERRED_ILI_REPO` | Modellrepositories, die bei der Modellsuche prioritär berücksichtigt werden. | `https://geo.so.ch/models` |
| `CLEANER_ENABLED` | Soll der Cronjob, welcher veraltete Daten aus dem  `WORK_DIRECTORY` löscht, eingeschaltet werden. | `true` |


Beispiel mit docker-compose. DB etc. secrets?


## Autorisierung

## Cluster

## Konfiguration GDI

- DB/DDL: Mit Andi diskutieren
- Email

Aus Benutzersicht wird der Datahub hauptsächlich mittels REST-API verwendet. Die Authentifizerung der meisten Operationen der API erfolgt über einen API-Key. Die Autorisierung über die dem Key zugeordnete Organisation und der Organisation zugewiesenen Operate (`operat=xxxx`) eines Themas (`theme=yyyyy`).

## Daten anliefern

Anlieferung des Operates `2471` des Themas `IPW_2020`. Der Dateiname `2471_gep.xtf` kann frei gewählt werden und muss keiner Logik folgen:

```
curl -i -X POST --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" -F 'file=@2471_gep.xtf' -F 'theme=IPW_2020' -F 'operat=2471' https://geo.so.ch/datahub/api/deliveries
```

**Achtung:** Es muss zwingend `https` verwendet werden. Wird die Anfrage nur mit `http` gemacht, wird der API-Key auf dem Server sofort ungültig gemacht.

Bei erfolgreicher Authentifizierung und Autorisierung wird folgende Antwort mit Statuscode `202` zurückgeliefert:

```
HTTP/1.1 202
Operation-Location: https://geo.so.ch/datahub/api/jobs/e6083332-857e-4f98-82d7-c15f458286ce
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Wed, 10 Apr 2024 14:28:51 GMT
```

Es wurde ein Anlieferungsjob mit der ID `e6083332-857e-4f98-82d7-c15f458286ce` erstellt und in die Warteschlange gestellt. Eine Validierung hat zu diesem Zeitpunkt noch nicht stattgefunden. Mit der in der Antwort enthaltenen URL `https://geo.so.ch/datahub/api/jobs/e6083332-857e-4f98-82d7-c15f458286ce` kann man in der Konsole oder im Browser den Status des Jobs anfragen. Diese Anfrage benötigt keine Authentifizierung. Eine mögliche Antwort ist:

```
{
"jobId": "e6083332-857e-4f98-82d7-c15f458286ce",
"createdAt": "2024-04-10T16:28:51.648642",
"updatedAt": "2024-04-10T16:29:11.400489",
"status": "SUCCEEDED",
"operat": "2471",
"theme": "IPW_2020",
"organisation": "ACME GmbH",
"validationStatus": "DONE",
"logFileLocation": "https://geo.so.ch/datahub/api/logs/e6083332-857e-4f98-82d7-c15f458286ce"
}
```

Die Attribute haben folgende Bedeutung:

- `status` beschreibt den Status des Anlieferungsjobs (nicht der eigentlichen INTERLIS-Validierung). Mögliche Werte sind:
  * `ENQUEUED`: Der Job ist in der Warteschlange und es wird noch nicht validiert. In diesem Fall gibt es ein weiteres Attribut `queuePosition`, welches die Position in der Warteschlange beschreibt.
  * `PROCESSING`: Der Job wird gerade abgearbeitet, d.h. die Validierung findet jetzt statt.
  * `SUCCESSFULL`: Der Job wurde erfolgreich beendet. Das bedeutet nicht zwingend, dass die Daten gültig sind. Siehe dazu das Attribut `validationStatus`.
  * `FAILED`: Der Job konnte nicht erfolgreich beendet werden.
  * `DELETED`: Erfolgreiche Jobs bekommen nach einer gewissen Zeit diesen Status. Als Anwender hat dies keine weitere Bedeutung.
- `validationStatus` beschreibt das Resultat der INTERLIS-Validierung. Es gibt zwei, ilivalidator entsprechende, Werte:
  * `DONE`: Es wurden _keine_ Fehler gefunden. Die Daten gelten als geliefert.
  * `FAILED`: Es wurden Fehler gefunden. Die Daten gelten als _nicht_ geliefert.
- `logFileLocation`: URL des Logfiles. Insbesondere bei einer nicht erfolgreichen Prüfung zwecks Fehlerkorrektur notwendig.

Nach Beendigung des Jobs wird eine E-Mail verschickt. Im E-Mail-Betreff stehen der `validationStatus` sowie Operats- und Themenname. 

Für eine rasche Gesamtübersicht steht eine Webseite mit einer Tabelle sämtlicher Jobs zur Verfügung: https://geo.so.ch/datahub/web/jobs.xhtml. Es wird keine Authentisierung benötigt. Verschiedene Attribute (z.B. Organisation) könnten gefiltert werden.

![Jobsübersicht](./images/user-manual/jobs_uebersicht.png)

### Nicht erfolgreiche Anlieferungen

Anlieferungen können aus verschiedenen Gründen nicht erfolgreich sein:

(1) Falls keine API-Key bei der Anlieferung verwendet wird, antwortet der Server mit einem Statuscode `401`. Die vollständige Ausgabe in der Konsole sieht so aus:

```
HTTP/1.1 401
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 0
Date: Thu, 11 Apr 2024 05:45:22 GMT
Connection: close
```

(2) Wird ein falscher API-Key verwendet, antwortet der Server mit dem Statuscode `403` und mit folgendem Inhalt:

```
HTTP/1.1 403
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 11 Apr 2024 06:06:20 GMT

{"timestamp":"2024-04-11T06:06:20.463+00:00","status":403,"error":"Forbidden","message":"Forbidden","path":"/api/deliveries"}
```

(3) Falls ein API-Key verwendet wird, der existiert, dessen Organisation aber nicht für Lieferung des Operates autorisiert ist, antwortet der Server ebenfalls mit dem Statuscode `403` und diese Inhalt:

```
HTTP/1.1 403
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 138
Date: Thu, 11 Apr 2024 06:07:25 GMT

{"code":"ch.so.agi.datahub.auth.DeliveryAuthorizationFilter","message":"User is not authorized","timestamp":"2024-04-11T06:07:25.143747Z"}
```

## API-Keys anfordern und löschen

Die API-Keys müssen erstmalig vom Admin-Account erstellt werden:

```
curl -i -X POST --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" -F 'organisation=Acme GmbH' https://geo.so.ch/datahub/api/keys
```

Die Angabe der Organisation ist zwingend. Ohne diese wird eine neuer API-Key für den Admin-Account erstellt. Konnte ein neuer API-Key erstellt werden, erscheint folgend Meldung in der Konsole:

```
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 11 Apr 2024 06:16:02 GMT

{"message":"Sent email with new api key.","timestamp":"2024-04-11T06:16:02.329886Z"}
```

Der API-Key erscheint bewusst nicht in der Ausgabe, sondern wird an die hinterlegte E-Mail-Adresse verschickt. Die E-Mail-Adresse kann nur vom Admin-Account in der Datenbank verändert werden.

Wenn die Organisation selber einen neuen API-Key erstellen will, reicht gleiche Befehl ohne Angabe der Organisation:

```
curl -i -X POST --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" https://geo.so.ch/datahub/api/keys
```

Die Ausgabe ist identisch.

Will man einen Key löschen, muss folgender Request gemacht werden:

```
curl -i -X DELETE --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" https://geo.so.ch/datahub/api/keys/5b4fd340-adbb-441a-b9c3-e1d2f13cb1e0
```

Konnte der Schlüssel gelöscht werden, wird der Statuscode `200` zurückgeliefert und diesem Inhalt

```
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 11 Apr 2024 06:23:20 GMT

{"message":"API key deleted.","timestamp":"2024-04-11T06:23:20.670535Z"}
```

Wurde der API-Key nicht gefunden und konnte nicht gelöscht werden, ist die Antwort:

```
HTTP/1.1 500
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 11 Apr 2024 06:24:56 GMT
Connection: close

{"code":"ch.so.agi.datahub.controller.ApiKeyController","message":"API key not deleted.","timestamp":"2024-04-11T06:24:56.218306Z"}
```

Zukünftig werden API-Keys immer ein Ablaufdatum haben und es müssen regelmässig neue Keys angefordert werden.


