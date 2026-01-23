package ch.so.agi.datahub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.cayenne.ObjectContext;
import org.interlis2.validator.Validator;
import org.jobrunr.jobs.context.JobContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.util.ReflectionTestUtils;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.datahub.cayenne.DeliveriesDelivery;

class DeliveryServiceTest {

    @Test
    void deliverUpdatesStatusAndSendsEmail(@TempDir Path tempDir) throws IOException {
        FilesStorageService filesStorageService = mock(FilesStorageService.class);
        ObjectContext objectContext = mock(ObjectContext.class);
        EmailService emailService = mock(EmailService.class);
        ResourceBundle resourceBundle = mock(ResourceBundle.class);
        DeliveryValidationService validationService = mock(DeliveryValidationService.class);

        DeliveryService service = new DeliveryService(filesStorageService, objectContext, emailService, validationService, resourceBundle);
        ReflectionTestUtils.setField(service, "workDirectory", tempDir.toString());
        ReflectionTestUtils.setField(service, "folderPrefix", "folder");
        ReflectionTestUtils.setField(service, "targetDirectory", tempDir.toString());
        ReflectionTestUtils.setField(service, "preferredIliRepo", "http://example.com/ili");
        ReflectionTestUtils.setField(service, "mailEnabled", true);

        UUID jobId = UUID.randomUUID();
        JobContext jobContext = mock(JobContext.class);
        when(jobContext.getJobId()).thenReturn(jobId);

        Path transferFile = tempDir.resolve("delivery.xtf");
        Files.writeString(transferFile, "content");

        Path logFile = tempDir.resolve(jobId + ".log");
        Files.writeString(logFile, "log");

        when(filesStorageService.load("delivery.xtf", jobId.toString(), "folder", tempDir.toString()))
                .thenReturn(new FileSystemResource(transferFile.toFile()));
        doNothing().when(filesStorageService).save(any(), any(), any(), any(), any());

        when(validationService.validate(eq(transferFile.toFile().getAbsolutePath()), any(Settings.class))).thenReturn(true);

        DeliveriesDelivery delivery = mock(DeliveriesDelivery.class);
        when(objectContext.selectOne(any())).thenReturn(delivery);

        when(resourceBundle.getString("deliveryEmailSubject")).thenReturn("Status %s %s %s");
        when(resourceBundle.getString("deliveryEmailBody")).thenReturn("Body %s %s %s");

        service.deliver(jobContext, "user@example.com", "Theme", "Operat", "delivery.xtf", "config.xml", "meta.xml", "http://localhost");

        verify(validationService).validate(eq(transferFile.toFile().getAbsolutePath()), any(Settings.class));
        verify(filesStorageService).save(any(), eq("delivery.xtf"), eq("Theme"), eq(null), eq(tempDir.toString()));
        verify(filesStorageService).save(any(), eq("delivery.xtf.log"), eq("Theme"), eq(null), eq(tempDir.toString()));
        verify(delivery).setIsvalid(true);
        verify(delivery).setIsdelivered(true);
        verify(objectContext).commitChanges();
        verify(emailService).send(eq("user@example.com"),
                eq("Status DONE Theme Operat"),
                eq("Body " + jobId + " http://localhost/api/logs/" + jobId + " http://localhost/web/jobs.xhtml"));
    }

    @Test
    void deliverConfiguresValidatorSettings(@TempDir Path tempDir) throws IOException {
        FilesStorageService filesStorageService = mock(FilesStorageService.class);
        ObjectContext objectContext = mock(ObjectContext.class);
        EmailService emailService = mock(EmailService.class);
        ResourceBundle resourceBundle = mock(ResourceBundle.class);
        DeliveryValidationService validationService = mock(DeliveryValidationService.class);

        DeliveryService service = new DeliveryService(filesStorageService, objectContext, emailService, validationService, resourceBundle);
        ReflectionTestUtils.setField(service, "workDirectory", tempDir.toString());
        ReflectionTestUtils.setField(service, "folderPrefix", "folder");
        ReflectionTestUtils.setField(service, "targetDirectory", tempDir.toString());
        ReflectionTestUtils.setField(service, "preferredIliRepo", "http://example.com/ili");
        ReflectionTestUtils.setField(service, "mailEnabled", false);

        UUID jobId = UUID.randomUUID();
        JobContext jobContext = mock(JobContext.class);
        when(jobContext.getJobId()).thenReturn(jobId);

        Path transferFile = tempDir.resolve("delivery.xtf");
        Files.writeString(transferFile, "content");

        Path logFile = tempDir.resolve(jobId + ".log");
        Files.writeString(logFile, "log");

        when(filesStorageService.load("delivery.xtf", jobId.toString(), "folder", tempDir.toString()))
                .thenReturn(new FileSystemResource(transferFile.toFile()));

        DeliveriesDelivery delivery = mock(DeliveriesDelivery.class);
        when(objectContext.selectOne(any())).thenReturn(delivery);

        org.mockito.ArgumentCaptor<Settings> settingsCaptor = org.mockito.ArgumentCaptor.forClass(Settings.class);
        when(validationService.validate(eq(transferFile.toFile().getAbsolutePath()), settingsCaptor.capture())).thenReturn(false);

        service.deliver(jobContext, "user@example.com", "Theme", "Operat", "delivery.xtf", "config.xml", "meta.xml", "http://localhost");

        Settings settings = settingsCaptor.getValue();
        assertThat(settings.getValue(Validator.SETTING_CONFIGFILE)).isEqualTo("ilidata:config.xml");
        assertThat(settings.getValue(Validator.SETTING_META_CONFIGFILE)).isEqualTo("ilidata:meta.xml");
        assertThat(settings.getValue(Validator.SETTING_ILIDIRS))
                .isEqualTo("http://example.com/ili;" + Validator.SETTING_DEFAULT_ILIDIRS);
    }
}
