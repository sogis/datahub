package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.model.JobResponse;
import ch.so.agi.datahub.service.JobResponseService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
  private final JobResponseService jobs;

  public JobController(JobResponseService jobs) {
    this.jobs = jobs;
  }

  @GetMapping
  public ResponseEntity<List<JobResponse>> getJobsApi() {
    var list = jobs.getJobResponseList();
    return list.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(list);
  }

  @GetMapping("/{jobId}")
  public ResponseEntity<JobResponse> getJobApiById(@PathVariable String jobId) {
    var job = jobs.getJobResponseById(jobId);
    return job == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(job);
  }
}
