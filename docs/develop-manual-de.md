# Entwicklung

JDK 21, Gradle Wrapper 8.14.3 und Docker mit Compose genügen. Die Compose-Datenbank ist an
`127.0.0.1:55432` gebunden. Mailpit empfängt auf Port 1025 und zeigt E-Mails auf Port 8025 an.
Port 8080 bleibt für Datahub frei.

```sh
./gradlew devSeed
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Die Anmeldedaten im Dev-Profil gelten für die lokale Demo. `devSeed` registriert den synthetischen
Lieferanten, das Thema `Demo` und das Operat `sample`. Der folgende Test-Key gilt nur für diese Demo.

```sh
curl -i -H 'X-API-KEY: dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8' \
  -F theme=Demo -F operat=sample -F file=@dev/valid.xtf \
  http://localhost:8080/api/deliveries
```

Die Lieferungs-ID steht in `Operation-Location`. Das Ergebnis erscheint unter
[Jobs](http://localhost:8080/jobs) und die optionale Ergebnis-Mail in [Mailpit](http://localhost:8025).
`dev/invalid.xtf` enthält einen fachlichen Fehler. `dev/other-model.xtf` gehört zu einem fremden
Modell. Beide Lieferungen erzeugen ein Prüfprotokoll.

### Lokalen Admin-Key einrichten und Dateien ansehen

Falls die lokale Datenbank schon eine Admin-Organisation enthält, überspringe den folgenden
Befehlsblock. Starte Datahub wie oben beschrieben und verwende für die Anmeldung den vorhandenen
Admin-Key. Falls es noch keinen Admin gibt und der konfigurierte Admin-Name bereits vergeben
ist, setze beim Start zusätzlich `ADMIN_ACCOUNT_NAME` auf einen freien Organisationsnamen.

Für den Bootstrap stoppst du eine laufende App mit `Ctrl+C` und führst die Befehle im
Repository aus. Wenn ein Befehl fehlschlägt, behebe den Fehler, bevor du weitergehst.
`mktemp` legt eine Datei an, die nur dein Benutzerkonto lesen und schreiben kann. `uuidgen`
erzeugt die beiden UUIDs, die `printf` im Format `KEY_ID.SECRET` in die Datei schreibt.

```sh
./gradlew devSeed

datahub_admin_key_file=$(mktemp "${TMPDIR:-/tmp}/datahub-admin.XXXXXXXX")
printf '%s.%s\n' "$(uuidgen)" "$(uuidgen)" > "$datahub_admin_key_file"
printf 'Admin-Key-Datei: %s\n' "$datahub_admin_key_file"

ADMIN_KEY_FILE="$datahub_admin_key_file" \
  ./gradlew bootRun --args='--spring.profiles.active=dev'
```

1. Führe den obigen `curl`-Aufruf mit `dev/valid.xtf` und dem Demo-Lieferanten-Key in einem
   zweiten Terminal aus dem Repository aus. Warte unter [Jobs](http://localhost:8080/jobs)
   auf die erfolgreiche Verarbeitung.
2. Öffne [den Ordner Demo im Dateibrowser](http://localhost:8080/ui/files?path=Demo). Gib in
   der HTTP-Basic-Anmeldung die UUID vor dem Punkt als Benutzername und die UUID nach dem
   Punkt als Passwort ein. Beim Bootstrap findest du beide UUIDs in der Datei am
   ausgegebenen Pfad.
3. Im Ordner `Demo` kannst du `sample.xtf` und `sample.xtf.log` herunterladen. Auf dem
   Dateisystem liegen sie unter `build/dev/target/Demo/`.

Nach erfolgreicher Anmeldung kannst du den neuen Schlüssel im Passwortmanager sichern, die
temporäre Datei löschen und die App künftig ohne `ADMIN_KEY_FILE` starten. Datahub speichert
die Schlüssel-ID und einen Hash des Secrets in der lokalen Datenbank.

### Lokale Dienste stoppen oder zurücksetzen

`./gradlew devDown` stoppt die lokalen Dienste. Die Datenbank-Initialisierung läuft beim ersten
Start eines neuen Compose-Volumes. Ein bereits benutztes Volume entweder wie eine bestehende
Installation aktualisieren oder, wenn es nur entbehrliche Demodaten enthält, mit `./gradlew devReset`
löschen und `devSeed` erneut ausführen. `devSeed` migriert bestehende Volumes nicht.
`cdbimport` erst gegen das aktuelle Schema ausführen, da es die Mappings aus dem physischen
Stand der verbundenen Datenbank aktualisiert.

## INTERLIS, PostgreSQL und Cayenne

1. Das Konfigurations- oder Lieferungsmodell in `dev/SO_AGI_Datahub_20240403.ili` ändern.
2. Mit `./gradlew generateSchema` die ili2pg-DDL unter `build/schema/` erzeugen.
3. Die DDL auf eine lokale Datenbank anwenden. `./gradlew devUp` erledigt dies mit einem neuen
   Compose-Volume und richtet die JobRunr-Tabellen ein.
4. `./gradlew cdbimport cgen` ausführen. `cdbimport` liest standardmässig die lokale
   Compose-Datenbank. `-PcayenneJdbcUrl=jdbc:postgresql://127.0.0.1:PORT/edit` wählt eine andere
   lokale Datenbank mit denselben Schemanamen. Für eine unveränderte Generierung
   `git diff --exit-code -- src/main/resources/cayenne src/main/java/ch/so/agi/datahub/cayenne`
   ausführen. Geänderte Mappings und generierten Java-Code gemeinsam einchecken.

## Verifikation

```sh
./gradlew test
./gradlew check
./gradlew buildImage
```

`integrationTest` verwendet PostgreSQL mit Testcontainers. Es prüft Annahme und Autorisierung über
HTTP, XTF-Validierung, wiederholte Verarbeitung, Lieferungshistorie und das Upgrade des
historischen Schemas aus `7df15c5`. `e2eTest` baut das Produktionsimage mit demselben Dockerfile wie die Auslieferung.
Es startet den Container mit einer zufälligen UID, einem schreibgeschützten Root-Dateisystem und
gemounteten Ablagen, reicht eine Lieferung per HTTP ein und wartet auf JobRunr, Übernahme und SMTP.
Die CI führt beide Testaufgaben und die Cayenne-Generierungsprüfung aus.
