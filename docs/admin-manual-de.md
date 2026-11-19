# Betriebshandbuch

## Neue Datenbank

`./gradlew generateSchema` erzeugt aus dem INTERLIS-Modell die SQL-Dateien
`build/schema/config.sql` und `build/schema/log.sql`. Als Schema-Owner in der leeren Datenbank
anwenden und das Schema für JobRunr anlegen.

```sh
psql -X -v ON_ERROR_STOP=1 -f build/schema/config.sql -f build/schema/log.sql \
  -c 'CREATE SCHEMA agi_datahub_jobrunr_v1'
```

Mit dem JobRunr-8.8.2-Artefakt und dessen Runtime-Abhängigkeiten auf dem Klassenpfad den
[JobRunr-Tabellenersteller](https://www.jobrunr.io/en/documentation/installation/storage/)
einmal offline aufrufen. `JOBRUNR_CLASSPATH` enthält auch den PostgreSQL-Treiber.

```sh
java -cp "$JOBRUNR_CLASSPATH" org.jobrunr.storage.sql.common.DatabaseCreator \
  "$JDBC_URL" "$SCHEMA_OWNER" "$SCHEMA_OWNER_PASSWORD" agi_datahub_jobrunr_v1.
psql -X -v ON_ERROR_STOP=1 -f sql/grants.sql
```

Die Rolle `datahub` vor der Rechtevergabe anlegen und die Laufzeitidentität Mitglied dieser Rolle
machen. Der Betriebsuser der Anwendung benötigt DML und Sequenzzugriff. Das [JobRunr-Dashboard](https://www.jobrunr.io/en/documentation/deployment/) läuft
bei Aktivierung auf einem eigenen internen Port, standardmässig 8000. Dafür
`JOBRUNR_DASHBOARD_ENABLED=true`, `JOBRUNR_DASHBOARD_USER` und `JOBRUNR_DASHBOARD_PWD`
mit eigenen Zugangsdaten setzen. Der öffentliche Dienst exponiert Port 8080. Die JobRunr-Tabellen werden vor dem Anwendungsstart eingerichtet.

## Bestehende Datenbank aktualisieren

Dieser Ablauf gilt für die Standardschemas aus `main` oder dem bisherigen Modernisierungsbranch.
Auf einer Wiederherstellungskopie proben. Den Export von Konfiguration und Lieferungshistorie,
die installierte PostGIS-Erweiterung und doppelte Thema-Operat-Paare vor dem Wechsel prüfen.

1. Annahme am Gateway pausieren, laufende Lieferungen vollständig abarbeiten und die bisherige
   Anwendung samt Workern stoppen. Bestehende JobRunr-Zeitpläne und fehlgeschlagene Altläufe sichten.
2. Einen vollständigen `pg_dump -Fc`, die Rollendefinitionen und konsistente Sicherungen der Arbeits-
   und Zielablage erstellen. Alte Image-Referenz, Konfiguration und den Wiederherstellungspunkt
   festhalten.
3. `psql -X -v ON_ERROR_STOP=1 -f sql/upgrade-r0.sql` als Schema-Owner ausführen. Das Skript
   prüft, ob die Jobs abgearbeitet sind, erhält Lieferungen, Key-Hashes und Widerrufszeitpunkte.
   Früher unvollständig ausgestellte Keys werden gesperrt. Vorhandene XTF-Dateien bleiben am
   bisherigen Ort. Für modernisierte Lieferungen bleiben die Protokollpfade und die einfachen
   Modell- und Validatorangaben erhalten.
4. Die Modellmetadaten mit ili2pg 5.5.2 neu registrieren und danach JobRunr offline mit dem oben
   beschriebenen Tabellenersteller aktualisieren. Bei Installationen von `main` enthält das
   Upgrade-SQL die Einträge für bereits vorhandene JobRunr-Tabellen. Erst der Tabellenersteller
   wendet die noch ausstehende Bibliotheksmigration an.

   ```sh
   for spec in Config:config Log:log; do
     java -jar "$ILI2PG_JAR" --schemaimport --dbparams "$ILI2PG_DBPARAMS" \
       --modeldir dev --models "SO_AGI_Datahub_${spec%:*}_20240403" \
       --dbschema "agi_datahub_${spec#*:}_v1" --nameByTopic --createFk --createFkIdx \
       --createUnique --createImportTabs --createMetaInfo || exit 1
   done
   java -cp "$JOBRUNR_CLASSPATH" org.jobrunr.storage.sql.common.DatabaseCreator \
     "$JDBC_URL" "$SCHEMA_OWNER" "$SCHEMA_OWNER_PASSWORD" agi_datahub_jobrunr_v1.
   psql -X -v ON_ERROR_STOP=1 -f sql/grants.sql
   ```

5. In `core_theme.expected_models` je Thema die zulässigen Modellnamen mit Semikolon trennen.
   Bestehende Themen ohne Modellliste bleiben gespeichert, bis zur Pflege pausiert ihre Annahme.
   Altdateien aus `main` ohne Modellnachweis sind direkt abrufbar. Eine neue gültige Lieferung
   nimmt das Dataset in `ilidata.xml` auf.
6. Das neue Image starten und zunächst bei pausierter Annahme Health, einen bestehenden Key,
   öffentliche Historie und Protokolle prüfen. Danach Annahme öffnen und eine kontrollierte
   Lieferung samt Zieldatei prüfen.

Bei einem Fehler vor dem Commit des SQL-Upgrades rollt PostgreSQL das Skript zurück. Für einen
Rückfall nach dem Commit zuerst die Annahme sperren und das neue Image stoppen. Datenbank und
beide Ablagen aus demselben Sicherungspunkt auf einem sauberen Ziel wiederherstellen und das
bisherige Image mit seiner Konfiguration starten. Falls bereits neue Lieferungen angenommen wurden,
deren Datenbankzeilen und Arbeitsdateien vor dem Rückfall sichern und nach der Wiederherstellung
gezielt erneut einreichen.

## Container und Konfiguration

`./gradlew buildImage` baut das Image `sogis/datahub:<Version>-local`. Das Image verwendet UBI9
OpenJDK 25 und startet mit `1001:0`. Unter OpenShift kann eine zufällige UID mit Gruppe 0 verwendet
werden. Das Root-Dateisystem kann schreibgeschützt bleiben. Zwei persistente Volumes an `/work` und
`/datahub` sowie ein beschreibbares temporäres Dateisystem an `/tmp` mounten. Die Volumes müssen
die Arbeits- und Zielverzeichnisse mit Gruppenrechten bereitstellen. Bei mehreren Instanzen
verwenden Web und JobRunr dieselben Volumes. Die Zielablage muss atomare Umbenennungen erlauben.
Die separate Dateisystemstufe übernimmt UBI9 und entfernt den vom Basis-Image geerbten OCI-Port 8443. Das Image belässt die JVM-Minderung `-XX:-UseBASE64Intrinsics` für CVE-2026-114938.

| Einstellung | Verwendung |
| --- | --- |
| `DBURL`, `DBUSR`, `DBPWD` | Laufzeitverbindung mit PostgreSQL |
| `CONFIG_DB_SCHEMA`, `LOG_DB_SCHEMA`, `JOBRUNR_DB_SCHEMA` | Schemas, JobRunr-Präfix mit abschliessendem Punkt |
| `WORK_DIRECTORY`, `TARGET_DIRECTORY` | Arbeits- und Zielablage, im Image `/work` und `/datahub` |
| `PREFERRED_ILI_REPO`, `PUBLIC_BASE_URL` | INTERLIS-Repository und öffentliche HTTPS-URL |
| `ACCEPT_DELIVERIES` | Annahme vor Wartungsfenstern pausieren |
| `JOBRUNR_WORKER_COUNT` | Standard 1. Bei mehr Workern JDBC-Pool und Validator-Last berücksichtigen |
| `MAIL_ENABLED`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | Optionaler SMTP-Versand für Ergebnisse |
| `ADMIN_KEY_FILE` | Pfad zu einer einmalig eingehängten Bootstrap-Secret-Datei |

Das Gateway terminiert HTTPS und setzt vertrauenswürdige Forwarded-Header. Direkter Zugriff auf
Port 8080 gehört ins interne Netz. Der Bootstrap einer leeren Installation findet mit einer
Instanz statt. Zwei zufällige UUIDs als `KEY_ID.SECRET` im Secret-Speicher ablegen. Die Datei mit
restriktiven Leserechten für die Laufzeitidentität an `ADMIN_KEY_FILE` bereitstellen. Mit
`ADMIN_ACCOUNT_NAME` und `ADMIN_ACCOUNT_MAIL` bei Bedarf die Organisation konfigurieren.
Existiert bereits eine Admin-Organisation, überspringt der Start die Anlage. Nach dem ersten
erfolgreichen administrativen Zugriff die Bootstrap-Datei aus der Deployment-Konfiguration nehmen.

## Verarbeitung und Wiederherstellung

Datahub speichert die Datei und registriert die Lieferung, bevor JobRunr sie übernimmt. Fehlt der
JobRunr-Eintrag einer angenommenen Lieferung oder begonnenen Übernahme, stellt Datahub ihn
unter derselben ID wieder her.
Technische Fehler wiederholt JobRunr. Eine fehlgeschlagene Übernahme kann nach Beheben der
Ursache im internen Dashboard über denselben Job erneut angestossen werden. Die Prüfeingabe bleibt dafür erhalten.

Vor der Übernahme speichert Datahub die Freigabeabsicht. PostgreSQL serialisiert die Übernahme
für identische Zielpfade. Datahub legt beide Dateien vollständig bereit und ersetzt dann das
Protokoll und zuletzt den Transfer atomar. Scheitert der letzte Dateitausch, stellt Datahub das
bisherige Protokoll wieder her. Nach einem Prozessabbruch zwischen den Umbenennungen stellt
derselbe Job das Paar erneut her. Der Datenindex enthält das Ziel wieder, sobald die Übernahme
bestätigt ist. Dadurch sind neu geschriebene Bytes während eines Fehlers keiner älteren
Modellangabe im Index zugeordnet.

Ergebnis-Mail ist optional. Der Versand erfolgt einmal nach Abschluss der Verarbeitung und kann
bei einem Abbruch unmittelbar nach dem Datenbank-Commit entfallen. Der Arbeitsbereich abgeschlossener
Lieferungen wird standardmässig nach drei Tagen bereinigt. Prüfeingaben bei technischen Fehlern
sowie verwaiste Dateien aus unterbrochenen Uploads sind bei der Betriebsbereinigung zu sichten.

## GDI-Infrastruktur

Die installationsbezogene Infrastruktur beschreibt die [GDI-Dokumentation](https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/datahub/datahub.md).
Für Datenbank-Upgrade und Laufzeitkonfiguration gilt die Anleitung in diesem Handbuch.
