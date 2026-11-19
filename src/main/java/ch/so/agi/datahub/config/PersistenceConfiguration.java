package ch.so.agi.datahub.config;

import ch.so.agi.datahub.*;
import ch.so.agi.datahub.auth.ApiKeyFormat;
import ch.so.agi.datahub.cayenne.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import org.apache.cayenne.CayenneRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PersistenceConfiguration {
  @Bean(destroyMethod = "shutdown")
  CayenneRuntime cayenneRuntime(DataSource source, AppProperties p) {
    var runtime =
        CayenneRuntime.of().addConfig("cayenne/cayenne-project.xml").defaultDataNode(source).build();
    for (var map : runtime.getDataDomain().getDataMaps())
      for (var entity : map.getDbEntities()) {
        if ("agi_datahub_config_v1".equals(entity.getSchema())) entity.setSchema(p.configSchema());
        if ("agi_datahub_log_v1".equals(entity.getSchema())) entity.setSchema(p.logSchema());
      }
    return runtime;
  }

  @Bean
  CommandLineRunner init(
      CayenneRuntime cayenne, AppProperties p, PasswordEncoder encoder, Clock clock) {
    return args -> {
      if (!p.getAdminKeyFile().isBlank()) {
        var parts = ApiKeyFormat.parseApiKey(Files.readString(Path.of(p.getAdminKeyFile())).strip())
            .orElseThrow(() -> new IllegalArgumentException("Invalid bootstrap API key file"));
        cayenne.performInTransaction(() -> {
          var ctx = cayenne.newContext();
          if (ObjectSelect.query(CoreOrganisation.class)
              .where(CoreOrganisation.AROLE.eq(AppConstants.ROLE_NAME_ADMIN)
                  .orExp(CoreOrganisation.ANAME.eq(p.getAdminAccountName())))
              .selectFirst(ctx) != null) return null;
          var org = ctx.newObject(CoreOrganisation.class);
          org.setAname(p.getAdminAccountName());
          org.setArole(AppConstants.ROLE_NAME_ADMIN);
          org.setEmail(p.getAdminAccountMail());
          var key = ctx.newObject(CoreApikey.class);
          key.setApikey(ApiKeyFormat.buildStoredValue(parts.keyId(),
              encoder.encode(parts.secret().toString())));
          key.setCreatedat(LocalDateTime.now(clock));
          key.setOrganisationR(org);
          ctx.commitChanges();
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
