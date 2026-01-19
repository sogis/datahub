package ch.so.agi.datahub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.cayenne.ObjectContext;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.so.agi.datahub.model.ApiError;
import ch.so.agi.datahub.service.DeliveryService;
import ch.so.agi.datahub.service.FilesStorageService;

class DeliveryControllerTest {

    @Test
    void errorReturnsApiError() {
        DeliveryController controller = new DeliveryController(
                mock(PasswordEncoder.class),
                mock(JobScheduler.class),
                mock(DeliveryService.class),
                mock(ObjectContext.class),
                mock(FilesStorageService.class));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/deliveries");
        ResponseEntity<?> response = controller.error(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isInstanceOf(ApiError.class);
        ApiError error = (ApiError) response.getBody();
        assertThat(error.message()).isEqualTo("Please contact service provider. Delivery is not queued.");
        assertThat(error.path()).isEqualTo("/api/deliveries");
        assertThat(error.timestamp()).isNotNull();
    }
}
