# Betriebshandbuch

## Neue Datenbank

Die Befehle in diesem Handbuch werden im Repository-Root ausgeführt. Für `psql` die
Zieldatenbank und den Schema-Owner über `PGHOST`, `PGPORT`, `PGDATABASE` und `PGUSER`
festlegen. Die Zugangsdaten über eine geschützte Passwortdatei (`PGPASSFILE`, Modus 0600)
oder ein anderes in PostgreSQL eingerichtetes Verfahren bereitstellen. Die Beispiele verwenden
die drei Standardschemas.

Die Datenbankadministration legt vorab die Gruppenrolle `datahub` an und weist sie der
Laufzeitidentität zu. Der Schema-Owner benötigt das Recht, Schemas in der Datenbank anzulegen.
`./gradlew generateSchema` erzeugt `build/schema/config.sql` und `build/schema/log.sql`.
Die Konfigurations-DDL enthält zusätzlich den zusammengesetzten Operat-Index aus
`sql/config-constraints.sql`, den ili2pg 5.5.2 für die INTERLIS-Regel mit einer Beziehung nicht erzeugt.

```sh
./gradlew generateSchema
psql -X -v ON_ERROR_STOP=1 -f build/schema/config.sql -f build/schema/log.sql \
  -c 'CREATE SCHEMA agi_datahub_jobrunr_v1'
```

Für Neuinstallation und Upgrade den JobRunr-Tabellenersteller offline mit den Bibliotheken
der zu installierenden Datahub-Version aufrufen. Dazu das ausführbare Release-JAR
(`DATAHUB_JAR`) verwenden. Das JAR liegt im Image unter `/opt/app/application.jar` und
kann mit `docker create` und `docker cp` entnommen werden. Aus dem passenden Commit
erzeugt `./gradlew bootJar` das JAR unter `build/libs/*-exec.jar`. `JDBC_URL` für dieselbe Datenbank
setzen, etwa `jdbc:postgresql://db.example:5432/datahub`. `SCHEMA_OWNER_PASSWORD` aus dem
Secret-Speicher laden. JobRunr und ili2pg erhalten das Passwort als Prozessargument. Beide Befehle in einer
geschützten Betriebssitzung ohne Shell-Tracing ausführen.

```sh
JOBRUNR_LIBDIR=$(mktemp -d)
unzip -q "$DATAHUB_JAR" 'BOOT-INF/lib/*' -d "$JOBRUNR_LIBDIR"
JOBRUNR_CLASSPATH="$JOBRUNR_LIBDIR/BOOT-INF/lib/*"
java -cp "$JOBRUNR_CLASSPATH" org.jobrunr.storage.sql.common.DatabaseCreator \
  "$JDBC_URL" "$PGUSER" "$SCHEMA_OWNER_PASSWORD" agi_datahub_jobrunr_v1.
psql -X -v ON_ERROR_STOP=1 -f sql/grants.sql
```

`sql/grants.sql` erteilt der Rolle `datahub` Zugriff auf Tabellen und Sequenzen. Die bereits
vorhandene optionale Rolle `datahub_read` erhält Zugriff auf die JobRunr-Statistik. Vor dem
Anwendungsstart müssen die JobRunr-Tabellen vorhanden sein.

## Bestehende Datenbank aktualisieren

Dieser einmalige Ablauf gilt für den historischen Datahub vor der Modernisierung
(Referenz `7df15c5`, `sql/setup_gdi.sql`) und für Installationen des damaligen
Modernisierungsbranches vor R0. Auf einer Wiederherstellungskopie proben.
Doppelte Organisations- oder Themennamen und doppelte Operatnamen innerhalb eines Themas
vor dem Wechsel fachlich bereinigen. API-Keys und Operate müssen auf vorhandene
Organisationen verweisen, Operate zudem auf vorhandene Themen. Ein Fehler beim Anlegen
eines eindeutigen Indexes oder Fremdschlüssels bricht die SQL-Transaktion ab.

Enthält die Installation bereits die Spalte `deliveries_delivery.processing_status`, wurde
das R0-Schema eingerichtet. Auf einer bereits aktualisierten Installation die fehlenden Konfigurationsregeln als
Schema-Owner ergänzen. Den Befehl nach der Vorprüfung und bei gestoppter Anwendung ausführen.

```sh
psql -X -v ON_ERROR_STOP=1 --single-transaction -f sql/config-constraints.sql
```

1. Annahme am Gateway pausieren, laufende Lieferungen vollständig abarbeiten und die bisherige
   Anwendung samt Workern und Cleanup stoppen. Historische JobRunr-Zeitpläne entfernen und
   fehlgeschlagene Altläufe sichten.
2. Mit `pg_dump -Fc -f "$DATAHUB_DB_DUMP"` die gesamte Datenbank sichern.
   `DATAHUB_DB_DUMP` ist der Pfad zur Sicherungsdatei in der geschützten Backup-Ablage.
   Rollendefinitionen sowie Arbeits- und Zielablage am selben Wiederherstellungspunkt sichern.
   Alte Image-Referenz und Konfiguration festhalten.
3. `psql -X -v ON_ERROR_STOP=1 -f sql/upgrade-r0.sql` als Schema-Owner ausführen. Das Skript
   lädt `config-constraints.sql` aus demselben Verzeichnis, prüft den Jobbestand und lehnt einen
   zweiten Lauf ab. Lieferungen, Key-Hashes und Widerrufszeitpunkte bleiben erhalten.
   Früher unvollständig ausgestellte Keys werden gesperrt. Vorhandene XTF-Dateien bleiben am
   bisherigen Ort. Für modernisierte Lieferungen bleiben die Protokollpfade und die einfachen
   Modell- und Validatorangaben erhalten.
4. Die Modellmetadaten mit ili2pg 5.5.2 neu registrieren und danach JobRunr offline mit dem oben
   beschriebenen Tabellenersteller aktualisieren. Das SQL registriert die bereits vorhandenen
   historischen JobRunr-Tabellen im Migrationsjournal. Der Tabellenersteller spielt die
   ausstehenden Bibliotheksmigrationen ein. `ILI2PG_JAR` ist das JAR aus der entpackten
   ili2pg-5.5.2-Distribution samt benachbartem `libs/`. Die Verbindungswerte stammen aus
   derselben Betriebssitzung wie im Abschnitt für neue Datenbanken.

   ```sh
   for spec in Config:config Log:log; do
     java -jar "$ILI2PG_JAR" --schemaimport \
       --dbhost "$PGHOST" --dbport "$PGPORT" --dbdatabase "$PGDATABASE" \
       --dbusr "$PGUSER" --dbpwd "$SCHEMA_OWNER_PASSWORD" \
       --modeldir dev --models "SO_AGI_Datahub_${spec%:*}_20240403" \
       --dbschema "agi_datahub_${spec#*:}_v1" --nameByTopic --createFk --createFkIdx \
       --createUnique --createImportTabs --createMetaInfo || exit 1
   done
   java -cp "$JOBRUNR_CLASSPATH" org.jobrunr.storage.sql.common.DatabaseCreator \
     "$JDBC_URL" "$PGUSER" "$SCHEMA_OWNER_PASSWORD" agi_datahub_jobrunr_v1.
   psql -X -v ON_ERROR_STOP=1 -f sql/grants.sql
   ```

5. In `core_theme.expected_models` je Thema die zulässigen Modellnamen mit Semikolon trennen.
   Bestehende Themen ohne Modellliste bleiben gespeichert, bis zur Pflege pausiert ihre Annahme.
   Transferdateien des historischen Datahub bleiben direkt abrufbar. War die letzte Übernahme
   ungültig oder fehlt ihr Modellnachweis, erscheint das Dataset erst nach einer neuen gültigen
   Übernahme wieder in `ilidata.xml`.
6. Das neue Image starten und zunächst bei pausierter Annahme Health, einen bestehenden Key,
   öffentliche Historie und Protokolle prüfen. Danach Annahme öffnen und eine kontrollierte
   Lieferung samt Zieldatei prüfen.

Bei einem Fehler vor dem Commit des SQL-Upgrades rollt PostgreSQL das Skript zurück. Für einen
Rückfall nach dem Commit zuerst die Annahme sperren und das neue Image samt Workern stoppen.
Die benötigten Rollen bereitstellen und mit
`pg_restore --exit-on-error -d "$PGDATABASE" "$DATAHUB_DB_DUMP"` in eine leere Datenbank
zurückspielen. Arbeits- und Zielablage aus demselben Sicherungspunkt wiederherstellen und
das bisherige Image mit der dazugehörigen Konfiguration starten. Falls bereits neue Lieferungen angenommen wurden,
deren Datenbankzeilen und Arbeitsdateien vor dem Rückfall sichern und nach der Wiederherstellung
gezielt erneut einreichen.

## Container und Konfiguration

`./gradlew buildImage` baut das Image `sogis/datahub:<Version>-local`. Das Image verwendet UBI9
OpenJDK 25 und startet mit `1001:0`. Unter OpenShift kann eine zufällige UID mit Gruppe 0 verwendet
werden. Das Root-Dateisystem kann schreibgeschützt bleiben. Zwei persistente Volumes an `/work` und
`/datahub` sowie ein beschreibbares temporäres Dateisystem an `/tmp` mounten. Die Volumes müssen
die Arbeits- und Zielverzeichnisse mit Gruppe 0 und Gruppen-Schreibrecht bereitstellen.
Bei mehreren Instanzen verwenden Web und JobRunr dieselben Volumes. Bereits existierende
Arbeits- und Themenverzeichnisse vor dem Start mit abweichenden UIDs ebenfalls mit diesen
Gruppenrechten versehen. Die Zielablage muss atomare Umbenennungen erlauben
und unterschiedliche Schreibweisen und Unicode-Normalformen getrennt speichern. Das Thema
bestimmt das Verzeichnis, das Operat den Dateinamen. In diesen Namen keine Schrägstriche,
Backslashes, Prozentzeichen, Semikolons oder Steuerzeichen verwenden.
Das Dockerfile erbt direkt vom gepinnten UBI9-OpenJDK-25-Runtime-Image. Das Basisimage kann Port 8443 als OCI-Metadatum deklarieren; der Datahub-Dienst veröffentlicht nur Port 8080. Das gepinnte Image enthält auf amd64 und arm64 das RPM `25.0.5.0.8-1.0.el9`. Red Hat nennt
`25.0.5.1.2-1.1.el9` als korrigierten Stand. Deshalb bleibt die JVM-Minderung
`-XX:-UseBASE64Intrinsics` aktiv. [Red Hat Security Advisory RHSA-2026:100571](https://access.redhat.com/errata/RHSA-2026:100571)

| Einstellung | Verwendung |
| --- | --- |
| `DBURL`, `DBUSR`, `DBPWD` | Laufzeitverbindung mit PostgreSQL |
| `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA`, `JOBRUNR_DB_SCHEMA` | Schemas, JobRunr-Präfix mit abschliessendem Punkt |
| `WORK_DIRECTORY`, `TARGET_DIRECTORY` | Arbeits- und Zielablage, im Image `/work` und `/datahub` |
| `PREFERRED_ILI_REPO`, `PUBLIC_BASE_URL` | INTERLIS-Repository und öffentliche HTTPS-URL |
| `ACCEPT_DELIVERIES` | Annahme vor Wartungsfenstern pausieren |
| `JOBRUNR_SERVER_ENABLED` | Standard `true`. Auf einem Web-Pod ohne Verarbeitung auf `false` setzen |
| `CLEANER_ENABLED`, `APP_ARTIFACTRETENTION` | Bereinigung aktivieren und Aufbewahrungsdauer setzen, Standard `true` und 3 Tage |
| `JOBRUNR_WORKER_COUNT` | Standard 1. Bei mehr Workern JDBC-Pool und Validator-Last berücksichtigen |
| `MAIL_ENABLED`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | Optionaler SMTP-Versand für Ergebnisse |
| `ADMIN_KEY_FILE` | Pfad zu einer einmalig eingehängten Bootstrap-Secret-Datei |

Ein separater Worker verwendet `JOBRUNR_SERVER_ENABLED=true` und kann mit
`SPRING_MAIN_WEB_APPLICATION_TYPE=none` ohne HTTP-Dienst starten. Web und Worker benötigen
dieselbe Datenbank-, Schema-, Ablage- und Modellkonfiguration. Cleanup kann auf einer
Instanz laufen. Der JobRunr-Tabellenersteller bleibt ein separater Wartungsschritt.
OpenShift-Mounts, UID/Gruppenrechte und Netzwerkregeln sind im Zielcluster abzunehmen.

Das Gateway terminiert HTTPS, entfernt vom Client gesetzte Forwarded-Header und setzt die
eigenen Header. Direkter Zugriff auf Port 8080 gehört ins interne Netz. Die öffentliche
Basis-URL mit `PUBLIC_BASE_URL` setzen. `/actuator/health`, `/actuator/health/liveness` und
`/actuator/health/readiness` dienen den HTTP-Probes am Web-Pod. `/ping` prüft nur die
Erreichbarkeit des HTTP-Servers. Der SMTP-Check läuft bei aktiviertem Mailversand.
Anwendungslogs gehen auf Standardausgabe. Zugangsdaten
gehören weder in URLs noch in Gateway- oder Header-Logging. Das optionale JobRunr-Dashboard
mit `JOBRUNR_DASHBOARD_ENABLED=true`, `JOBRUNR_DASHBOARD_USER` und `JOBRUNR_DASHBOARD_PWD`
auf einem internen Port (Standard 8000) betreiben.

Der Bootstrap einer leeren Installation findet mit einer Instanz statt. Zwei zufällige UUIDs als `KEY_ID.SECRET` im Secret-Speicher ablegen. Die Datei mit
restriktiven Leserechten für die Laufzeitidentität an `ADMIN_KEY_FILE` bereitstellen. Mit
`ADMIN_ACCOUNT_NAME` und `ADMIN_ACCOUNT_MAIL` bei Bedarf die Organisation konfigurieren.
Existiert bereits eine Admin-Organisation oder eine Organisation unter demselben Namen,
überspringt der Start die Anlage. Nach dem ersten
erfolgreichen administrativen Zugriff die Bootstrap-Datei aus der Deployment-Konfiguration nehmen.

## Verarbeitung und Wiederherstellung

Datahub speichert die Datei und registriert die Lieferung, bevor JobRunr sie übernimmt. Fehlt der
JobRunr-Eintrag einer angenommenen Lieferung oder begonnenen Übernahme, stellt Datahub ihn
unter derselben ID wieder her.
Technische Fehler wiederholt JobRunr. Eine fehlgeschlagene Übernahme kann nach Beheben der
Ursache im internen Dashboard über denselben Job erneut angestossen werden. Die Prüfeingabe bleibt dafür erhalten.
Jobs aus Versionen vor R0 bleiben als Historie erhalten. Eine fehlgeschlagene Altlieferung bei
Bedarf mit der gesicherten Transferdatei neu einreichen.

Während der Validierung hält die Anwendung eine PostgreSQL-Transaktion mit einer Sperre
auf genau der Lieferungszeile. Damit erzeugt eine doppelte Ausführung keine konkurrierenden
Prüfprotokolle. Lange Validierungen belegen jeweils eine Verbindung. Workerzahl und JDBC-Pool
entsprechend dimensionieren.

Vor der Übernahme speichert Datahub die Freigabeabsicht. PostgreSQL serialisiert Veröffentlichungen
für denselben Zielpfad. Datahub stellt beide Dateien bereit und ersetzt dann das Prüfprotokoll
und den XTF-Transfer über je eine atomare Umbenennung. Bei einem Fehler während des Dateitauschs
oder beim anschliessenden Synchronisieren versucht Datahub, die vorherigen Dateien
wiederherzustellen. Dafür wird vorab eine Kopie der vorhandenen Zieldateien angelegt.
Scheitert auch die Wiederherstellung, bleibt die Sicherung erhalten und Datahub protokolliert
ihren Pfad. Eine bereits bestätigte neuere Publikation hat Vorrang
vor älteren Jobs. Der Index folgt der letzten bestätigten Publikation mit Modellangaben.

Dateisystem und Datenbank haben getrennte Commit-Grenzen. Bei einem Prozessabbruch zwischen
Dateitausch und Datenbank-Commit können Index und Dateien vorübergehend unterschiedliche Stände
zeigen. Solange JobRunr einen Versuch ausführt oder einen Retry plant, kann es beide Dateien
erneut schreiben. Nach ausgeschöpften Wiederholungen die Ursache beheben und den Job im
Dashboard erneut ausführen. Transfer, Protokoll und Index anschliessend vergleichen.
Falls die Eingabe inzwischen abgelaufen ist, die Lieferung aus einer gesicherten Transferdatei
erneut einreichen.

Ergebnis-Mail ist optional. Der Versand erfolgt nach dem Datenbank-Commit und kann bei einem
Abbruch in diesem Zeitfenster entfallen. Cleanup entfernt Arbeitsdateien nach der Aufbewahrungsfrist
und überspringt laufende und geplante JobRunr-Versuche. Endgültige Fehler und verwaiste Uploads
werden ebenfalls bereinigt. Verwaiste temporäre Dateien im Zielverzeichnis werden für bestätigte
oder überholte Lieferungen nach der Aufbewahrungsfrist entfernt. Bei endgültig fehlgeschlagenen
Übernahmen verbleiben Wiederherstellungskopien bis zur manuellen Prüfung.
Lieferungshistorie und veröffentlichte Lieferungsdateien bleiben erhalten.

## GDI-Infrastruktur

Die installationsbezogene Infrastruktur beschreibt die [GDI-Dokumentation](https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/datahub/datahub.md).
Für Datenbank-Upgrade und Laufzeitkonfiguration gilt die Anleitung in diesem Handbuch.
