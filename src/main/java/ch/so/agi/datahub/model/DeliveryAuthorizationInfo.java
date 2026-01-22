package ch.so.agi.datahub.model;

public record DeliveryAuthorizationInfo(
        String organisationName,
        String email,
        String config,
        String metaConfig
) {
}
