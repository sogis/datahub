package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.so.agi.datahub.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import tools.jackson.databind.json.JsonMapper;

class RevokeApiKeyFilterTest {
  @Test
  void insecureNonWhitelistedRequestRevokesAndStopsChain() throws Exception {
    var keys = mock(KeyManagementService.class);
    var p = new AppProperties();
    var filter = new RevokeApiKeyFilter(keys, p, JsonMapper.builder().findAndAddModules().build());
    var request = new MockHttpServletRequest();
    request.setServerName("example.test");
    request.setScheme("http");
    request.addHeader("X-API-KEY", "key");
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();
    filter.doFilter(request, response, chain);
    verify(keys).revokeInsecure("key");
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(chain.getRequest()).isNull();
  }

  @Test
  void httpsAndWhitelistedHttpProceed() throws Exception {
    for (boolean secure : new boolean[] {true, false}) {
      var keys = mock(KeyManagementService.class);
      var filter =
          new RevokeApiKeyFilter(
              keys, new AppProperties(), JsonMapper.builder().findAndAddModules().build());
      var request = new MockHttpServletRequest();
      request.setServerName(secure ? "example.test" : "localhost");
      request.setScheme(secure ? "https" : "http");
      request.addHeader("X-API-KEY", "key");
      var chain = new MockFilterChain();
      filter.doFilter(request, new MockHttpServletResponse(), chain);
      assertThat(chain.getRequest()).isNotNull();
      verifyNoInteractions(keys);
    }
  }
}
