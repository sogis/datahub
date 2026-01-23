package ch.so.agi.datahub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;
import org.primefaces.model.FilterMeta;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ch.so.agi.datahub.model.JobResponse;

class JobResponseServiceTest {

    @Test
    void getJobResponseListAppliesFiltersAndLimitAndBuildsLogUrl() {
        ObjectContext objectContext = mock(ObjectContext.class);
        JobResponseService service = new JobResponseService(objectContext);
        ReflectionTestUtils.setField(service, "jobResponseListLimit", 25);

        DataRow row = mock(DataRow.class);
        when(row.get("jobid")).thenReturn("job-1");
        when(row.get("createdat")).thenReturn(LocalDateTime.now());
        when(row.get("updatedat")).thenReturn(LocalDateTime.now());
        when(row.get("status")).thenReturn("SUCCEEDED");
        when(row.get("queueposition")).thenReturn(1L);
        when(row.get("operat")).thenReturn("operat");
        when(row.get("theme")).thenReturn("theme");
        when(row.get("organisation")).thenReturn("org");
        when(row.get("message")).thenReturn("ok");
        when(row.get("validationstatus")).thenReturn("OK");

        when(objectContext.select(any())).thenReturn(List.of(row));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("example.com");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            FilterMeta filterMeta = mock(FilterMeta.class);
            when(filterMeta.getFilterValue()).thenReturn("theme");
            Map<String, FilterMeta> filters = new HashMap<>();
            filters.put("theme", filterMeta);

            List<JobResponse> responses = service.getJobResponseList(filters);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getLogFileLocation()).isEqualTo("http://example.com/api/logs/job-1");

            @SuppressWarnings("unchecked")
            var queryCaptor = org.mockito.ArgumentCaptor.forClass(ObjectSelect.class);
            verify(objectContext).select(queryCaptor.capture());
            ObjectSelect<DataRow> captured = queryCaptor.getValue();
            assertThat(captured.getLimit()).isEqualTo(25);
            assertThat(captured.getWhere()).isNotNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void getJobResponseListSkipsLogUrlWhenNoValidationStatus() {
        ObjectContext objectContext = mock(ObjectContext.class);
        JobResponseService service = new JobResponseService(objectContext);
        ReflectionTestUtils.setField(service, "jobResponseListLimit", 10);

        DataRow row = mock(DataRow.class);
        when(row.get("jobid")).thenReturn("job-2");
        when(row.get("createdat")).thenReturn(LocalDateTime.now());
        when(row.get("updatedat")).thenReturn(LocalDateTime.now());
        when(row.get("status")).thenReturn("FAILED");
        when(row.get("queueposition")).thenReturn(2L);
        when(row.get("operat")).thenReturn("operat");
        when(row.get("theme")).thenReturn("theme");
        when(row.get("organisation")).thenReturn("org");
        when(row.get("message")).thenReturn("oops");
        when(row.get("validationstatus")).thenReturn(null);

        when(objectContext.select(any())).thenReturn(List.of(row));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("example.com");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            List<JobResponse> responses = service.getJobResponseList(Map.of());

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getLogFileLocation()).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
