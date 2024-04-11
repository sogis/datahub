# datahub

Datendrehscheibe - Anlieferung von INTERLIS-Transferdateien.

## Beschreibung

Datahub ist eine Datendrehscheine und dient der Anlieferung von INTERLIS-Transferdateien. Die Anlieferung besteht vor allem aus zwei Teilen: Einer Autorisierung des Senders für den bestimmten Datensatz und der Validierung der gelieferten INTERLIS-Transferdatei gegenüber dem Datenmodell und allfälligen Validierungsmodellen.

Die Anlieferung erfolgt über ein REST-API, die Authentifizierung über einen API-Key. Den Status einer Lieferung kann entweder über das REST-API oder eine Webseite erfolgen. Beide Varianten benötigen keine Authentifizierung resp. Autorisierung. Ebenfalls wird eine E-Mail mit dem Prüfresultat verschickt.

API-Keys können durch die Benutzer selber erstellt und widerrufen werden. Die Keys sind nicht personalisiert, sondern an eine Organisation gebunden. Es gibt eine Admin-Organisation, die für jede andere Organisation Keys erstellen kann und auch jedes Operat anliefern darf. Die Administration der Autorisierung muss mit einem Datenbank-Client gemacht werden und kann nicht mit der Anwendung gemacht werden. Das Autorisierungsmodell ist mit INTERLIS modelliert. Es gibt zusätzlich ein INTERLIS-Modell, welches die einzelnen Lieferungen loggt. 

Die Daten werden nach erfolgreicher Prüfung an einen definierten Ort im Filesystem kopiert. Die Verzeichnisse müssen vorhanden sein.

## Anleitungen

- [Benutzerhandbuch](docs/user-manual-de.md)
- [Betriebshandbuch](docs/admin-manual-de.md)
- [Entwicklerhandbuch](docs/develop-manual-de.md)

## Komponenten

Die funktionale Einheit besteht aus dieser Webanwendung und einer Datenbank für die Speicherung von Autorisierungsobjekten und Lieferinformationen. Ebenfalls wird die Datenbank für die Jobqueue-Bibliothek benötigt. Die eingesetzte Technologie liesse einen Betrieb mit einer filebasierten DB zu, jedoch wird Stand heute eine PostgreSQL-Datenbank erwartet. 

## Konfigurieren und Starten

Siehe [Betriebshandbuch](docs/admin-manual-de.md).

## Externe Abhängigkeiten

- SMTP-Server

## Konfiguration und Betrieb in der GDI

Siehe [Betriebshandbuch](docs/admin-manual-de.md).

## Interne Struktur

Siehe [Entwicklerhandbuch](docs/develop-manual-de.md).
