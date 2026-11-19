# Benutzerhandbuch

## Daten anliefern

Lieferanten erhalten einen organisationsgebundenen API-Key von der Administration. Die
Anlieferung benötigt das Thema, das Operat und die INTERLIS-XTF-Datei als Multipart-Felder.

```sh
curl -i -H "X-API-KEY: $DATAHUB_KEY" \
  -F theme=Demo -F operat=sample -F file=@lieferung.xtf \
  "$DATAHUB_URL/api/deliveries"
```

`202 Accepted` bestätigt die Annahme. Der Header `Operation-Location` verweist auf den öffentlichen
Status dieser Lieferung. Fehlende oder ungültige API-Keys ergeben `401`, fehlende
Lieferberechtigungen `403`. Doppelte Multipart-Felder und widersprüchliche Themen- oder
Operat-Angaben ergeben `400`. Für neue Clients sind Multipart-Felder die einzige benötigte Quelle
für Thema und Operat.

## Status, Protokoll und Ablage

`/jobs` und `GET /api/jobs` zeigen die Lieferungen der letzten 14 Tage. Die GUI bietet Filter
für Organisation, Thema und Operat.
`GET /api/jobs/{id}` liefert den Status, `GET /api/logs/{id}` das Prüfprotokoll, solange es verfügbar
ist. Ein abgelaufenes Protokoll ergibt `410`. Die Lieferung bleibt in PostgreSQL erhalten.

| Anzeige | Bedeutung |
| --- | --- |
| `ENQUEUED`, `PROCESSING`, `SCHEDULED` | JobRunr wartet, verarbeitet oder plant einen neuen Versuch |
| Validierung `FAILED`, Übernahme `SKIPPED` | Datei ist fachlich ungültig oder enthält ein fremdes Modell |
| Übernahme `DELIVERED` | Gültige Datei und Protokoll wurden übernommen |
| Übernahme `SUPERSEDED` | Eine neuere gültige Lieferung hat für dasselbe Ziel Vorrang |
| Lieferung `FAILED` | Technischer Fehler bei Prüfung oder Übernahme |

Gültige Übernahmen erscheinen als `Thema/Operat.xtf` und `Thema/Operat.xtf.log`.
`/ilidata.xml` enthält den Index der aktuell übernommenen Dateien mit erfassten Modellangaben.
Bei aktiviertem SMTP kann der Lieferant eine Ergebnis-Mail erhalten.

## API-Keys verwalten

Administratoren verwenden ihren eigenen Schlüssel im Header. Das Anlegen gibt den neuen Schlüssel
und seine öffentliche ID einmal im HTTP-Response zurück. Den neuen Schlüssel direkt im vorgesehenen
Secret-Speicher hinterlegen und über einen geeigneten sicheren Kanal übergeben.

```sh
curl --fail-with-body -H "X-API-KEY: $DATAHUB_ADMIN_KEY" \
  -F 'organisation=Demo Supplier' "$DATAHUB_URL/api/keys"
curl --fail-with-body -X DELETE -H "X-API-KEY: $DATAHUB_ADMIN_KEY" \
  "$DATAHUB_URL/api/keys/$PUBLIC_KEY_ID"
```

Der administrative Dateibrowser `/ui/files` verwendet HTTP Basic. Im Browser ist die öffentliche
UUID der Benutzername, die zweite UUID des API-Keys das Passwort. `GET /api/files` verwendet den
API-Key-Header.
