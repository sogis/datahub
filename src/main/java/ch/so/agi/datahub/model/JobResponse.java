package ch.so.agi.datahub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResponse(
    String jobId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String status,
    Long queuePosition,
    String operat,
    String theme,
    String organisation,
    String message,
    String validationStatus,
    String logFileLocation,
    String deliveryStatus,
    String notificationStatus) {}
