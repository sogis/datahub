package ch.so.agi.datahub.service;

import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
  private final DeliveryRepository repository;
  private final ValidationAttemptRunner runner;

  public DeliveryService(DeliveryRepository repository, ValidationAttemptRunner runner) {
    this.repository = repository;
    this.runner = runner;
  }

  @Job(name = "Legacy delivery", retries = 0)
  public void deliver(
      JobContext context,
      String email,
      String theme,
      String operat,
      String filename,
      String config,
      String meta,
      String host) {
    var delivery = repository.find(context.getJobId().toString());
    if (delivery == null || delivery.get("attempt_id") == null)
      throw new IllegalStateException("Legacy jobs must be drained before this upgrade");
    runner.run((String) delivery.get("jobid"), (String) delivery.get("attempt_id"));
  }
}
