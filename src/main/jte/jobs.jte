@param ch.so.agi.datahub.model.JobPage result
@param ch.so.agi.datahub.model.JobSearch search
@param String previous
@param String next
@param String context
<!doctype html>
<html lang="de"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Datahub – Jobs</title>
<link rel="stylesheet" href="${context}/css/datahub.css"><script src="${context}/js/htmx.min.js" defer></script><script src="${context}/js/jobs.js" defer></script></head>
<body><h1>Datahub</h1>
<form class="filters" action="${context}/jobs" method="get" hx-get="${context}/jobs/table" hx-target="#jobs-table" hx-swap="outerHTML" hx-push-url="true">
<label>Organisation<input name="organisation" value="${search.organisation()}"></label>
<label>Thema<input name="theme" value="${search.theme()}"></label>
<label>Operat<input name="operat" value="${search.operat()}"></label>
<label>Lieferung<select name="status"><option value="">Alle</option>@for(var status:java.util.List.of("ENQUEUED","PROCESSING","SUCCEEDED","FAILED"))<option value="${status}" selected="${status.equals(search.status())}">${status}</option>@endfor</select></label>
<label>Validierung<select name="validationStatus"><option value="">Alle</option>@for(var status:java.util.List.of("DONE","FAILED"))<option value="${status}" selected="${status.equals(search.validationStatus())}">${status}</option>@endfor</select></label>
<label>Sortierung<select name="sort">@for(var sort:java.util.List.of("date-desc","date-asc","theme","operat"))<option value="${sort}" selected="${sort.equals(search.sort())}">${sort}</option>@endfor</select></label>
<button type="submit">Refresh / Filtern</button><a href="${context}/jobs">Filter löschen</a></form>
@template.jobs-table(result=result,search=search,previous=previous,next=next,context=context)
</body></html>
