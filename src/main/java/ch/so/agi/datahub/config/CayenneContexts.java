package ch.so.agi.datahub.config;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.springframework.stereotype.Component;

@Component
public class CayenneContexts {
  private final ServerRuntime runtime;

  public CayenneContexts(ServerRuntime runtime) {
    this.runtime = runtime;
  }

  public ObjectContext newContext() {
    return runtime.newContext();
  }

  public <T> T transaction(java.util.function.Function<ObjectContext, T> action) {
    return runtime.performInTransaction(() -> action.apply(newContext()));
  }
}
