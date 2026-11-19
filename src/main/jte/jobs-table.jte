@param ch.so.agi.datahub.model.JobPage result
@param String previous
@param String next
@param String context
<div id="jobs-table"><table><thead><tr><th>Organisation</th><th>Thema</th><th>Operat</th><th>Position</th><th>Lieferung</th><th>Validierung</th><th>Übernahme</th><th>E-Mail</th><th>Logdatei</th><th>Lieferdatum</th></tr></thead><tbody>
@for(var job:result.rows())
<tr><td>${job.organisation()}</td><td>${job.theme()}</td><td>${job.operat()}</td><td>@if(job.queuePosition()!=null)<span class="badge-warning">${job.queuePosition()}</span>@endif</td>
<td><span class="badge-job-status">${job.status()}</span></td>
<td>@if(job.validationStatus()!=null)<span class='${"DONE".equals(job.validationStatus())?"badge-done":"badge-failed"}'>${job.validationStatus()}</span>@endif</td>
<td><span class='${"DELIVERED".equals(job.deliveryStatus())?"badge-done":"FAILED".equals(job.deliveryStatus())?"badge-failed":"badge-job-status"}'>${job.deliveryStatus()}</span></td>
<td>${job.notificationStatus()}</td><td>@if(job.logFileLocation()!=null)<a class="black-link" href="${job.logFileLocation()}" target="_blank" rel="noopener">${job.message()==null?"Protokoll":"Abgelaufen"}</a>@endif</td><td>${job.createdAt()==null?"":job.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}</td></tr>
@endfor
</tbody></table><nav class="pagination"><span>${result.count()} Lieferungen</span>
@if(result.hasPrevious())<a href="${previous}" hx-get="${previous.replace("/jobs?", "/jobs/table?")}" hx-target="#jobs-table" hx-select="#jobs-table" hx-swap="outerHTML" hx-push-url="true">Zurück</a>@endif
@if(result.hasNext())<a href="${next}" hx-get="${next.replace("/jobs?", "/jobs/table?")}" hx-target="#jobs-table" hx-select="#jobs-table" hx-swap="outerHTML" hx-push-url="true">Weiter</a>@endif
</nav></div>
