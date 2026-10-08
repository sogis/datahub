package ch.so.agi.datahub.service;

/** Immutable provenance of the files actually resolved for this validation. */
public record ValidationOutcome(
    boolean valid, String modelMetadata, String configurationSnapshot) {}
