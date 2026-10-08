package ch.so.agi.datahub.model;

public record AuthorizedDataset(
    long id,
    String theme,
    String operat,
    String organisation,
    String email,
    String config,
    String metaConfig,
    String expectedModels) {}
