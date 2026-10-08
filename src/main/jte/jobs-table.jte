@param ch.so.agi.datahub.model.JobPage result
@param ch.so.agi.datahub.model.JobSearch search
@param String previous
@param String next
<div id="jobs-table"><table><thead><tr><th>Organisation</th><th>Thema</th><th>Operat</th><th>Position</th><th>Lieferung</th><th>Validierung</th><th>Übernahme</th><th>E-Mail</th><th>Logdatei</th><th>Lieferdatum</th></tr></thead><tbody>
@for(var job:result.rows())
<tr><td>${job.getOrganisation()}</td><td>${job.getTheme()}</td><td>${job.getOperat()}</td><td>@if(job.getQueuePosition()!=null)<span class="badge-warning">${job.getQueuePosition()}</span>@endif</td>
<td><span class="badge-job-status">${job.getStatus()}</span></td>
<td>@if(job.getValidationStatus()!=null)<span class='${"DONE".equals(job.getValidationStatus())?"badge-done":"badge-failed"}'>${job.getValidationStatus()}</span>@endif</td>
<td><span class='${"DELIVERED".equals(job.getDeliveryStatus())?"badge-done":"FAILED".equals(job.getDeliveryStatus())?"badge-failed":"badge-job-status"}'>${job.getDeliveryStatus()}</span></td>
<td>${job.getNotificationStatus()}</td><td>@if(job.getLogFileLocation()!=null)<a class="black-link" href="${job.getLogFileLocation()}" target="_blank" rel="noopener">${job.getMessage()==null?"Protokoll":"Abgelaufen"}</a>@endif</td><td>${job.getCreatedAt()==null?"":job.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}</td></tr>
@endfor
</tbody></table><nav class="pagination"><span>${result.count()} Lieferungen</span>
@if(result.hasPrevious())<a href="${previous}" hx-get="${previous}" hx-target="#jobs-table" hx-select="#jobs-table" hx-swap="outerHTML" hx-push-url="true">Zurück</a>@endif
@if(result.hasNext())<a href="${next}" hx-get="${next}" hx-target="#jobs-table" hx-select="#jobs-table" hx-swap="outerHTML" hx-push-url="true">Weiter</a>@endif
</nav></div>
