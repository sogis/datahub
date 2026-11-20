# Arbeiten an Datahub

## Arbeitsweise und Verträge

- Prüfe Git-Status, Ausgangscommit, betroffene Implementierung und vorhandene Tests. Schütze bestehende Arbeit und arbeite lokal auf einem eigenen Branch. Push, Merge und Deployment erfordern einen ausdrücklichen Auftrag.
- Wende Ockhams Rasiermesser an. Prüfe zuerst, was entfallen kann. Bevorzuge direkte Lösungen und wenige persistierte Zustände. Nutze Spring, PostgreSQL, Cayenne und JobRunr. Eine Abstraktion braucht einen heutigen Verbraucher oder eine konkrete Grenze für Korrektheit.
- Halte einen Scheduler und eine verbindliche Quelle für die Lieferungshistorie. Sicherheit und Datenintegrität haben Vorrang, danach Korrektheit, Einfachheit, Kompatibilität und Komfort.
- Erhalte die Autorisierung des exakten Lieferziels, Deny-by-default und den öffentlichen Lesezugriff auf Jobstatus und Prüfprotokolle.
- INTERLIS bleibt für das Schema massgebend. Erzeuge die DDL mit ili2pg und das Cayenne-Mapping und die Java-Klassen mit `cdbimport cgen`. Verwende bei Modell-, Mapping- und GDI-Änderungen [datahub-schema-change](.agents/skills/datahub-schema-change/SKILL.md). Produktionsschemaänderungen ausdrücklich anwenden und Sicherung sowie Rückfall dokumentieren.

## Verifikation und Review

- Verwende den Gradle Wrapper mit JDK 21 und führe `./gradlew test` und `./gradlew check` aus. Prüfe Sicherheits- und Persistenzverhalten über echtes HTTP und PostgreSQL. Prüfe bei Laufzeitänderungen das Java-25-Produktionsimage. Die Abläufe stehen im [Entwicklerhandbuch](docs/develop-manual-de.md) und im [Betriebshandbuch](docs/admin-manual-de.md).
- Wende vor grösseren Änderungen [simplify-change](.agents/skills/simplify-change/SKILL.md) an. Lass nach der Implementierung den vollständigen Diff gegen `origin/main` mit [review-change](.agents/skills/review-change/SKILL.md) und `simplify-change` unabhängig per lokalem OpenCode 2.x und DeepSeek Flash prüfen, sofern verfügbar. Verlange konkrete, reproduzierbare Befunde nach Schweregrad, einschliesslich unnötiger Komplexität. Korrigiere bestätigte Befunde und lass den geänderten Diff erneut prüfen.
- Verwende die vorhandenen Werkzeuge direkt. Implementierung und Review laufen ausserhalb von Codex Cloud. Halte Secrets aus Logs, Review-Unterlagen und Commits heraus. Berichte tatsächlich ausgeführte Prüfungen und weise ein ausgefallenes Review aus.
