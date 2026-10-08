package ch.so.agi.datahub.config;

import ch.so.agi.datahub.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mode", havingValue = "worker-single")
public class SingleWorker implements CommandLineRunner, ExitCodeGenerator {
  private final AppProperties p;
  private final DeliveryRepository r;
  private final ValidationAttemptRunner runner;
  private int exitCode = 1;

  public SingleWorker(AppProperties p, DeliveryRepository r, ValidationAttemptRunner runner) {
    this.p = p;
    this.r = r;
    this.runner = runner;
  }

  @Override
  public void run(String... args) {
    if (p.getAttemptId() == null || p.getAttemptId().isBlank())
      throw new IllegalArgumentException("worker-single requires app.attemptId");
    var rows = r.deliveryForAttempt(p.getAttemptId());
    if (rows.size() == 1 && runner.run((String) rows.getFirst().get("jobid"), p.getAttemptId()))
      exitCode = 0;
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }
}
