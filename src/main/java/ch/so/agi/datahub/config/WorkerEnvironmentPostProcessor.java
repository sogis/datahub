package ch.so.agi.datahub.config;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class WorkerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    if ("worker-single".equals(environment.getProperty("app.mode"))) {
      environment
          .getPropertySources()
          .addFirst(
              new MapPropertySource(
                  "single-worker",
                  overrides(
                      environment,
                      Map.of(
                          "spring.main.web-application-type",
                          "none",
                          "jobrunr.background-job-server.enabled",
                          "false",
                          "jobrunr.dashboard.enabled",
                          "false",
                          "jobrunr.job-scheduler.enabled",
                          "false",
                          "spring.devtools.restart.enabled",
                          "false"))));
    }
  }

  private static Map<String, Object> overrides(
      ConfigurableEnvironment environment, Map<String, Object> flags) {
    var values = new java.util.HashMap<String, Object>(flags);
    var excluded = new java.util.LinkedHashSet<String>();
    String configured = environment.getProperty("spring.autoconfigure.exclude");
    if (configured != null && !configured.isBlank())
      excluded.addAll(java.util.Arrays.asList(configured.split(",")));
    try {
      var resources =
          Thread.currentThread()
              .getContextClassLoader()
              .getResources(
                  "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
      while (resources.hasMoreElements())
        try (var reader =
            new java.io.BufferedReader(
                new java.io.InputStreamReader(
                    resources.nextElement().openStream(),
                    java.nio.charset.StandardCharsets.UTF_8))) {
          reader
              .lines()
              .map(String::trim)
              .filter(line -> line.startsWith("org.jobrunr."))
              .forEach(excluded::add);
        }
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot isolate worker configuration", e);
    }
    values.put("spring.autoconfigure.exclude", String.join(",", excluded));
    return values;
  }
}
