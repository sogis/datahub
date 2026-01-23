package ch.so.agi.datahub.service;

import ch.ehi.basics.settings.Settings;

public interface DeliveryValidationService {
    boolean validate(String filePath, Settings settings);
}
