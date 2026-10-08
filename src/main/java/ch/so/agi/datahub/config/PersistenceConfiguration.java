package ch.so.agi.datahub.config;

import ch.so.agi.datahub.*;
import ch.so.agi.datahub.auth.ApiKeyFormat;
import ch.so.agi.datahub.cayenne.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import org.apache.cayenne.*;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PersistenceConfiguration {
  @Bean(destroyMethod = "shutdown")
  ServerRuntime cayenneRuntime(DataSource source, AppProperties p) {
    var runtime =
        ServerRuntime.builder().dataSource(source).addConfig("cayenne/cayenne-project.xml").build();
    for (var map : runtime.getDataDomain().getDataMaps())
      for (var entity : map.getDbEntities()) {
        if ("agi_datahub_config_v1".equals(entity.getSchema())) entity.setSchema(p.configSchema());
        if ("agi_datahub_log_v1".equals(entity.getSchema())) entity.setSchema(p.logSchema());
      }
    return runtime;
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  ObjectContext objectContext(CayenneContexts contexts) {
    return contexts.newContext();
  }

  @Bean
  CommandLineRunner init(
      CayenneContexts contexts, AppProperties p, PasswordEncoder encoder, Clock clock) {
    return args -> {
      if (!p.getMode().equals("web")) return;
      if (p.isAdminAccountInit()) {
        contexts.transaction(
            ctx -> {
              if (ObjectSelect.query(CoreOrganisation.class)
                      .where(CoreOrganisation.ANAME.eq(p.getAdminAccountName()))
                      .selectFirst(ctx)
                  == null) {
                var org = ctx.newObject(CoreOrganisation.class);
                org.setAname(p.getAdminAccountName());
                org.setArole(AppConstants.ROLE_NAME_ADMIN);
                org.setEmail(p.getAdminAccountMail());
                var id = UUID.randomUUID();
                var secret = UUID.randomUUID();
                var key = ctx.newObject(CoreApikey.class);
                key.setApikey(ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())));
                key.setCreatedat(LocalDateTime.now(clock));
                key.setIssuanceState("ACTIVE");
                key.setCoreOrganisation(org);
                ctx.commitChanges();
                // Deliberate bootstrap behaviour, mandated by AGENTS.md.
                LoggerFactory.getLogger(DatahubApplication.class)
                    .warn("Bootstrap admin key: {}", ApiKeyFormat.buildApiKey(id, secret));
              }
              return null;
            });
      }
      if (p.isCreateDirectories()) {
        Files.createDirectories(Path.of(p.getWorkDirectory()));
        Files.createDirectories(Path.of(p.getTargetDirectory()));
      }
    };
  }
}
