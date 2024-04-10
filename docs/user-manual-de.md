# Benutzerhandbuch

Aus Benutzersicht wird der Datahub hauptsächlich mittels REST-API verwendet. Die Authentifizerung der meisten Operationen der API erfolgt über einen API-Key. Die Autorisierung über die dem Key zugeordnete Organisation und der Organisation zugewiesenen Operate (`operat=xxxx`) eines Themas (`theme=yyyyy`).

## Daten anliefern

Anlieferung des Operates `2622` des Themas `IPW_2020`. Der Dateiname `2622_gep.xtf` kann frei gewählt werden und muss keiner Logik folgen:

```
curl -i -X POST --header "X-API-KEY:ca20e14c-faa7-4920-b0a5-c5a44476d80c" -F 'file=@2622_gep.xtf' -F 'theme=IPW_2020' -F 'operat=2622' https://geo.so.ch/datahub/api/deliveries
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

Es wurde ein Anlieferungsjob mit der ID `e6083332-857e-4f98-82d7-c15f458286ce` erstellt und in die Warteschlange gestellt. Eine Validierung hat zu diesem Zeitpunkt noch nicht stattgefunden. Mit der in der Antwort enthaltene URL `https://geo.so.ch/datahub/api/jobs/e6083332-857e-4f98-82d7-c15f458286ce` kann man in der Konsole oder im Browser den Status des Jobs anfragen. Diese Anfrage benötigt keine Authentifizierung. Eine mögliche Antwort ist:

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
  * `ENQUEUED`: Der Job ist in der Warteschlagen und es wird noch nicht validiert. In diesem Fall gibt es ein weiteres Attribut `queuePosition`, welches die Position in der Warteschlange beschreibt.
  * `PROCESSING`: Der Job wird gerade abgearbeitet, d.h. die Validierung findet jetzt statt.
  * `SUCCESSFULL`: Der Job wurde erfolgreich beendet. Das bedeutet nicht zwingend, dass die Daten gültig sind. Siehe dazu das Attribut `validationStatus`.
  * `FAILED`: Der Job konnte nicht erfolgreich beendet werden.
  * `DELETED`: Erfolgreiche Jobs bekommen nach einer gewissen Zeit diesen Status. Als Anwender hat dies keine weitere Bedeutung.
- `validationStatus` beschreibt das Resultat der INTERLIS-Validierung. Es gibt zwei, ilivalidator entsprechende, Werte:
  * `DONE`: Es wurden _keine_ Fehler gefunden. Die Daten gelten als geliefert.
  * `FAILED`: Es wurden Fehler gefunden. Die Daten gelten als _nicht_ geliefert.
- `logFileLocation`: URL des Logfiles. Insbesondere bei einer nicht erfolgreichen Prüfung zwecks Fehlerkorrektur notwendig.

Nach Beendigung des Jobs wird eine E-Mail verschickt. Im E-Mail-Betreff stehen der `validationStatus` sowie Operats- und Themenname. 




