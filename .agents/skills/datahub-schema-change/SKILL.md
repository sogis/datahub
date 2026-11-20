---
name: datahub-schema-change
description: Führt Änderungen am Datahub-INTERLIS-Modell, an Cayenne-Mappings oder am GDI-Datenbanksetup über die vorhandenen Gradle-Tasks und prüft die erzeugten Artefakte.
---

# Datahub-Schema ändern

Arbeite nach den Abschnitten «INTERLIS, PostgreSQL und Cayenne» und «GDI-Setup neu
erzeugen» im [Entwicklerhandbuch](../../../docs/develop-manual-de.md#interlis-postgresql-und-cayenne).

1. Ändere das Schema an seiner Quelle im INTERLIS-Modell. Erzeuge die aktuelle
   PostgreSQL-DDL mit `generateSchema` und wende sie für Mapping-Änderungen
   zuerst auf eine frische, isolierte lokale Datenbank an. `devUp` erledigt
   beides mit einem neuen Compose-Volume.
2. Prüfe vor `cdbimport cgen` die Zielverbindung. `cdbimport` liest das
   physische Schema der verbundenen Datenbank. Setze für eine separate Instanz
   `-PcayenneJdbcUrl` auf deren lokale JDBC-URL. Verwende die im Handbuch
   beschriebenen Schemanamen und übernimm geänderte Mappings und generierte
   Java-Klassen gemeinsam.
3. Erzeuge nach Änderungen am Schema, an der JobRunr-Version oder an den Grants
   `sql/setup_gdi.sql` mit `./gradlew generateGdiSetupSql`. Prüfe die Datei auf
   frischem PostgreSQL 16 nach [Betriebshandbuch](../../../docs/admin-manual-de.md#neue-datenbank).
   Die GDI-Datei dient der Neuinstallation. Für Produktionsänderungen gelten
   die Regeln zu Sicherung und Rückfall in [AGENTS.md](../../../AGENTS.md).
