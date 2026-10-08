package ch.so.agi.datahub.service;

import ch.ehi.basics.settings.Settings;

public interface DeliveryValidationService {
  ValidationOutcome validate(String filePath, Settings settings) throws Exception;
}
