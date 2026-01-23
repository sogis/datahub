package ch.so.agi.datahub.service;

import org.interlis2.validator.Validator;
import org.springframework.stereotype.Service;

import ch.ehi.basics.settings.Settings;

@Service
public class IliValidatorService implements DeliveryValidationService {

    @Override
    public boolean validate(String filePath, Settings settings) {
        return Validator.runValidation(filePath, settings);
    }
}
