package ch.so.agi.datahub.service;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.auth.ApiKeyUser;
import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.model.AuthorizedDataset;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeliveryAuthorizationService {
  private final JdbcTemplate jdbc;
  private final AppProperties p;

  public DeliveryAuthorizationService(JdbcTemplate jdbc, AppProperties p) {
    this.jdbc = jdbc;
    this.p = p;
  }

  public AuthorizedDataset resolve(String theme, String operat, Authentication authentication) {
    var actor = (ApiKeyUser) authentication.getPrincipal();
    boolean admin =
        authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(AppConstants.ROLE_NAME_ADMIN));
    String s = p.configSchema();
    var rows =
        jdbc.query(
            "SELECT d.t_id,t.aname,d.aname,o.aname,o.email,t.config,t.metaconfig,t.expected_models"
                + " FROM "
                + s
                + ".core_operat d JOIN "
                + s
                + ".core_theme t ON t.t_id=d.theme_r JOIN "
                + s
                + ".core_organisation o ON o.t_id=d.organisation_r WHERE t.aname=? AND d.aname=?"
                + (admin ? "" : " AND o.t_id=?"),
            (r, n) ->
                new AuthorizedDataset(
                    r.getLong(1),
                    r.getString(2),
                    r.getString(3),
                    r.getString(4),
                    r.getString(5),
                    r.getString(6),
                    r.getString(7),
                    r.getString(8)),
            admin
                ? new Object[] {theme, operat}
                : new Object[] {theme, operat, actor.getOrganisationId()});
    if (rows.size() != 1)
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized");
    var target = rows.getFirst();
    if (target.expectedModels() == null || target.expectedModels().isBlank())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Theme requires expected transfer models.");
    return target;
  }
}
