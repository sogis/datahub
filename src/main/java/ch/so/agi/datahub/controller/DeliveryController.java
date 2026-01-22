package ch.so.agi.datahub.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.cayenne.ObjectContext;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.cayenne.DeliveriesDelivery;
import ch.so.agi.datahub.model.ApiError;
import ch.so.agi.datahub.model.DeliveryAuthorizationInfo;
import ch.so.agi.datahub.service.DeliveryService;
import ch.so.agi.datahub.service.FilesStorageService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class DeliveryController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.folderPrefix}")
    private String folderPrefix;
    
    private PasswordEncoder encoder;
    
    private JobScheduler jobScheduler;
    
    private DeliveryService deliveryService;
        
    private ObjectContext objectContext;
    
    private FilesStorageService filesStorageService;

    public DeliveryController(PasswordEncoder encoder, JobScheduler jobScheduler, DeliveryService deliveryService,
            ObjectContext objectContext, FilesStorageService filesStorageService) {
        this.encoder = encoder;
        this.jobScheduler = jobScheduler;
        this.deliveryService = deliveryService;
        this.objectContext = objectContext;
        this.filesStorageService = filesStorageService;
    }

    //@Transactional(rollbackFor={InvalidDataAccessResourceUsageException.class})
    @PostMapping(value="/api/deliveries", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFile(Authentication authentication, 
            @RequestPart(name = "theme", required = true) String theme,
            @RequestPart(name = "operat", required = true) String operat,
            @RequestPart(name = "file", required = true) MultipartFile file, 
            HttpServletRequest request) throws Exception {
        
        // JobId für Jobrunr
        UUID jobIdUuid = UUID.randomUUID();
        String jobId = jobIdUuid.toString();
  
        // Get the operat/theme information we gathered in the authorization filter
        DeliveryAuthorizationInfo operatDeliveryInfo = (DeliveryAuthorizationInfo) request.getAttribute(AppConstants.ATTRIBUTE_OPERAT_DELIVERY_INFO);

        // Normalize file name
        String originalFileName = file.getOriginalFilename();
        String sanitizedFileName = operat + ".xtf";

        // Daten speichern
        filesStorageService.save(file.getInputStream(), sanitizedFileName, jobId, folderPrefix, workDirectory);
                
        // Die Delivery-Tabellen nachführen.
        String orgName = operatDeliveryInfo.organisationName();
                
        DeliveriesDelivery deliveriesDelivery = objectContext.newObject(DeliveriesDelivery.class);
        deliveriesDelivery.setJobid(jobId);
        deliveriesDelivery.setDeliverydate(LocalDateTime.now());
        deliveriesDelivery.setOrganisation(orgName);
        deliveriesDelivery.setTheme(theme);
        deliveriesDelivery.setOperat(operat);

        // Validierungsjob in Jobrunr queuen.
        // Jobrunr kann nicht mit null Strings umgehen.
        String validatorConfig = operatDeliveryInfo.config() != null ? operatDeliveryInfo.config() : "";
        String validatorMetaConfig = operatDeliveryInfo.metaConfig() != null ? operatDeliveryInfo.metaConfig() : "";
        
        String email = operatDeliveryInfo.email();
        String host = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString().toString();
                        
        jobScheduler.enqueue(jobIdUuid, () -> deliveryService.deliver(JobContext.Null, email, theme, operat, sanitizedFileName,
                validatorConfig, validatorMetaConfig, host));
        logger.info("<{}> Job is being queued for validation.", jobId);
       
        // DB-Nachführung committen.
        objectContext.commitChanges();

        return ResponseEntity
                .accepted()
                .header("Operation-Location", getHost()+"/api/jobs/"+jobId)
                .body(null);
    }
    
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<?> error(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        logger.error("<{}>", e.getMessage());
        ApiError error = new ApiError(
                e.getClass().getCanonicalName(),
                "Please contact service provider. Delivery is not queued.",
                Instant.now(),
                request.getRequestURI(),
                null);
        return ResponseEntity
                .internalServerError()
                .body(error);
    }
    
    private String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
