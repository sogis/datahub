# Entwicklerhandbuch

## Interne Struktur

_Databub_ wird mit Spring Boot entwickelt.

Die Authentifizierung und Autorisierung ist mit Spring Security umgesetzt. Nach Rücksprache mit dem AIO wurde beschlossen, dass die Authentifizierung "nur" mit einem API-Key umgesetzt wird. 

Speziell autorisiert mittels Spring-Filter-Implementierung wird nur der Delivery-Request. Die Key-Requests werden entweder (neuer Key) nicht autorisiert (da die anfordernde Organisation einen neuen Key bekommt) oder implizt (beim Löschen): Es kann nur gelöscht werden, wenn der zu löschende Key der gleichen Organisation wie der Key im Request-Header zugewiesen ist.

Die Keys werden randonmässig gesaltet und gehashed in der Datenbank abgespeichert.

Job-Requests sind nicht authentifiziert.

Als ORM wird _Apache Cayenne_ eingesetzt, welches den Vorteil hat, dass aus bestehenden Tabellen Java-Klassen erstellt werden können. Zudem können auch sehr leichtgewichtige Abfragen gemacht werden, indem man als Rückgabewert bloss eine Map<String,Object> anfordert und keine gemanagte Entity.

Für das Jobmanagement wir _Jobrunr_ einesetzt.

Das Frontend (die Jobübersicht) ist mit Jakarta Faces umgesetzt (Joinfaces und Primefaces).

## Entwicklung

### Datenbank

Es müssen mit _ili2pg_ das Autorisierungsschema und das Logschema erstellt werden. Siehe dazu Betriebshandbuch. Die Tabellen für Jobrunr werden bei genügender Berechtigung des Datenbankusers beim Starten der Anwendung selberständig erstellt. Das Schema muss jedoch bereits vorhanden sein. Erstmalig muss es manuell erstellt werden:

```
CREATE SCHEMA IF NOT EXISTS agi_datahub_jobrunr_v1;
```

Mit der `docker-compose.yml`-Datei im `dev/`-Ordner kann die DB mit einem persistierenden Volume gestartet werden. Die Datei `datahub_key_20240403.xtf` beinhaltet bereits einige Organisation, Key, Themen und Operate. Die im XTF vorhanden Keys entsprechen den Testrequests weiter unten.

### Apache Cayenne

Nachdem die beiden leeren Scheman mit _ili2pg_ erstellt wurden, müssen (einmalig) die Java-Klassen für _Apache Cayenne_ erstellt werden. Siehe dazu vor allem die Konfigurationen im  `build.gradle`. Wichtig ist, dass es beide Dateien `cayenne-project.xml` und `datamap.map.xml` bereits gibt. Der Inhalt von `datamap.map.xml` ist minimal:

```
<?xml version="1.0" encoding="utf-8"?>
<data-map xmlns="http://cayenne.apache.org/schema/10/modelMap"
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 xsi:schemaLocation="http://cayenne.apache.org/schema/10/modelMap https://cayenne.apache.org/schema/10/modelMap.xsd"
	 project-version="10">
	<property name="defaultPackage" value="ch.so.agi.datahub.cayenne"/>
</data-map>
```

Anschliessen können die Java-Klassen mit _Gradle_ erstellen werden:

```
./gradlew cdbimport cgen
```

Besonderheiten:

- UUID-Datentyp hat Probleme gemacht und wurde mit `TEXT*36` umgesetzt.
- Bei Views muss darauf geachtet werden, dass Cayenne nicht zu grosse VARCHAR-Typen interpretiert. Dies führt zu einem Fehler und ist nachvollziehbar und einsehbar in der `datamap.map.xml`-Datei: `VARCHAR(123456789123456)` (nicht genau so, aber einfach zu gross). Damit das nicht vorkommt, muss man in der View-Definition explizit die Länge von Text-Attributen setzen.

### Jbang-Skript

Das _jbang_-Skript dient zum Erstellen der DDL-SQL-Skripte, die v.a. in der Produktion verwendet werden können.

```
jbang edit -b create_schema_sql.java
```

Mit der Option `-b` wird es in einer Sandbox ausgeführt. Das ist zwingend notwendig, wenn _VSCodium_ irritiert ist wegen vorhandener build.gradle-Dateien.

```
jbang create_schema_sql.java
```

### Testrequests

#### Admin-Account

Testrequest auf geschützten Endpunkt:

```
curl -i -X GET --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" http://localhost:8080/protected/hello
```

```
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: text/plain;charset=UTF-8
Content-Length: 34
Date: Thu, 11 Apr 2024 12:17:21 GMT

Hello, this is a secured endpoint!
```

Schlüssel für Organisation erstellen:

```
curl -i -X POST --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" -F 'organisation=W+H AG' http://localhost:8080/api/keys
```

```
curl -i -X POST --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" -F 'organisation=SWG' http://localhost:8080/api/keys
```

```
curl -i -X POST --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" -F 'organisation=Märki AG' http://localhost:8080/api/keys
```

Admin darf beliebige Operate schicken:

```
curl -i -X POST --header "X-API-KEY:c0bb04eb-789b-4063-95ad-bd86a06c6aff" -F 'file=@Grenchen_LKMap_wasser.xtf' -F 'theme=LKMAP_2015' -F 'operat=2546_was' http://localhost:8080/api/deliveries
```

#### Andere Benutzer

Märki AG:
```
curl -i -X GET --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" http://localhost:8080/protected/hello
```

```
curl -i -X POST --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" -F 'file=@2471.xtf' -F 'theme=IPW_2020' -F 'operat=2471' http://localhost:8080/api/deliveries
```

```
curl -i -X POST --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" -F 'file=@2622_gep.xtf' -F 'theme=IPW_2020' -F 'operat=2622' http://localhost:8080/api/deliveries
```

SWG:
```
curl -i -X GET --header "X-API-KEY:ab539116-210a-4ebf-ab9f-ee8786957fb6" http://localhost:8080/protected/hello
```

```
curl -i -X POST --header "X-API-KEY:ab539116-210a-4ebf-ab9f-ee8786957fb6" -F 'file=@Grenchen_LKMap_wasser.xtf' -F 'theme=LKMAP_2015' -F 'operat=2546_was' http://localhost:8080/api/deliveries
```

```
curl -i -X POST --header "X-API-KEY:ab539116-210a-4ebf-ab9f-ee8786957fb6" -F 'file=@Grenchen_LKMap_wasser_error.xtf' -F 'theme=LKMAP_2015' -F 'operat=2546_was' http://localhost:8080/api/deliveries
```

W+H AG:
```
curl -i -X GET --header "X-API-KEY:fda5f71f-8875-4d4a-af0c-87d9ba2832b6" http://localhost:8080/protected/hello
```
```
curl -i -X POST --header "X-API-KEY:fda5f71f-8875-4d4a-af0c-87d9ba2832b6" http://localhost:8080/api/keys
```

```
curl -i -X DELETE --header "X-API-KEY:fda5f71f-8875-4d4a-af0c-87d9ba2832b6" http://localhost:8080/api/keys/<vorgängig erzeugter Key>
```

