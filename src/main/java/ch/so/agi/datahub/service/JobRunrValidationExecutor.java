package ch.so.agi.datahub.service;

import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication
public class JobRunrValidationExecutor implements ValidationExecutor {
  private final JobScheduler scheduler;
  private final StorageProvider storage;
  private final ValidationAttemptRunner runner;

  public JobRunrValidationExecutor(
      JobScheduler scheduler, StorageProvider storage, ValidationAttemptRunner runner) {
    this.scheduler = scheduler;
    this.storage = storage;
    this.runner = runner;
  }

  @Override
  public void submit(String delivery, String attempt) {
    var id = UUID.fromString(attempt);
    try {
      storage.getJobById(id);
      return;
    } catch (JobNotFoundException e) {
      /* First submission. */
    }
    scheduler.enqueue(id, () -> execute(delivery, attempt));
  }

  @Job(name = "Delivery validation", retries = 0)
  public void execute(String delivery, String attempt) {
    if (!runner.run(delivery, attempt))
      throw new IllegalStateException("Validation attempt did not complete: " + attempt);
  }
}
