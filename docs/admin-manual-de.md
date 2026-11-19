# Betriebshandbuch

## Neue Datenbank

`sql/setup_gdi.sql` richtet die drei Standardschemas für Konfiguration, Lieferhistorie und
JobRunr in einer leeren PostgreSQL-16-Datenbank ein. Die Datei enthält die INTERLIS-Metadaten
und das JobRunr-Migrationsjournal. Vor dem Start der Anwendung führt die
Datenbankadministration das Skript einmal aus.

Die Datenbankadministration stellt die Gruppenrolle `datahub` bereit und weist sie der
Laufzeitidentität zu. Die Identität, die das Skript ausführt, benötigt das Recht, Schemas in der
Datenbank anzulegen und bei Bedarf die Gruppenrollen `datahub_read` und `datahub_write` zu
erstellen. Bereits vorhandene Gruppenrollen werden wiederverwendet.

Im Repository-Root ausführen. `PGHOST`, `PGPORT`, `PGDATABASE` und `PGUSER` legen das Ziel
fest. Bei Passwortauthentifizierung kann eine geschützte Passwortdatei (`PGPASSFILE`, Modus
0600) verwendet werden. Einen aktuellen PostgreSQL-16-Client oder neueren Client verwenden.

```sh
psql -X -f sql/setup_gdi.sql
```

`datahub` erhält Zugriff auf Tabellen und Sequenzen in allen drei Schemas. `datahub_read` darf
alle Tabellen in diesen Schemas lesen, einschliesslich der API-Key-Hashes. `datahub_write`
erhält zusätzlich zum Lesen Schreib- und Sequenzrechte im Konfigurationsschema. Die Vergabe
der Gruppenmitgliedschaften liegt bei der Datenbankadministration.

Bei einem Fehler rollt PostgreSQL die Installation zurück. Für einen Wechsel von einer
bestehenden Installation die neue Datenbank separat einrichten. Vor der Umschaltung die
bisherige Datenbank mit `pg_dump -Fc` und die Arbeits- und Zielablage am selben
Wiederherstellungspunkt sichern. Für einen Rückfall die bisherige Anwendung mit diesen
Daten und ihrer bisherigen Konfiguration starten. Das Setup übernimmt keine Bestandsdaten.

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
Für Datenbankinstallation und Laufzeitkonfiguration gilt die Anleitung in diesem Handbuch.
