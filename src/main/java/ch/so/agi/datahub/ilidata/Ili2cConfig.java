package ch.so.agi.datahub.ilidata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.TransferDescription;

@Configuration
public class Ili2cConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public TransferDescription transferDescription() throws IOException {
        Ili2cSettings set = new Ili2cSettings();
        ch.interlis.ili2c.Main.setDefaultIli2cPathMap(set);
        set.setIlidirs(Ili2cSettings.DEFAULT_ILIDIRS);
        
        InputStream is = getClass().getResourceAsStream("/ili/DatasetIdx16.ili");
        Path targetPath = Paths.get(System.getProperty("java.io.tmpdir"), "DatasetIdx16.ili");

        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        is.close();

        ch.interlis.ili2c.config.Configuration cfg = new ch.interlis.ili2c.config.Configuration();
        cfg.addFileEntry(new FileEntry(targetPath.toString(), FileEntryKind.ILIMODELFILE));
        cfg.setAutoCompleteModelList(true);
        cfg.setGenerateWarnings(true);

        TransferDescription td = ch.interlis.ili2c.Main.runCompiler(cfg, set, null);

        if (td == null) {
            throw new IllegalStateException("ili2c failed to compile models");
        }
        return td;
    }    
}
