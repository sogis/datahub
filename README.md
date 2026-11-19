# Datahub

Datahub nimmt INTERLIS-Lieferungen für autorisierte Themen und Operate entgegen. JobRunr führt die
Validierung aus und PostgreSQL hält die Lieferungshistorie. Gültige Lieferungen erscheinen unter
`Thema/Operat.xtf` mit dem zugehörigen Protokoll. Status, Prüfprotokolle und die JTE/HTMX-Übersicht
sind öffentlich. Administratoren verwalten API-Keys und können die Zielablage durchsuchen.

## Lokal starten

Mit JDK 21 und Docker aus dem Repository-Root ausführen. Die Dev-Konfiguration startet eine lokale
PostgreSQL-Datenbank und Mailpit.

```sh
./gradlew devSeed
./gradlew bootRun --args='--spring.profiles.active=dev'
```

```sh
./gradlew test
./gradlew check
```

`check` umfasst PostgreSQL, HTTP und das UBI9-Produktionsimage mit Java 25. Die Datenbankstruktur
entsteht aus INTERLIS mit ili2pg 5.5.2, das Java-Mapping mit Cayenne 5.0-RC1.

- [Benutzerhandbuch](docs/user-manual-de.md)
- [Entwicklerhandbuch](docs/develop-manual-de.md)
- [Betriebshandbuch](docs/admin-manual-de.md)
