package ch.so.agi.datahub.ilidata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.interlis.ili2c.metamodel.TransferDescription;

@Service
public class IlidataService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.targetDirectory}")
    private String targetDirectory;
    
    private final TransferDescription td;
    
    private static final List<String> DIRECTORIES = List.of(
            "DMAV_FixpunkteAVKategorie3_V1_0",
            "DMAV_Grundstuecke_V1_0", 
            "DMAV_HoheitsgrenzenAV_V1_0", 
            "DMAV_Bodenbedeckung_V1_0",
            "DMAV_Einzelobjekte_V1_0",
            "DMAV_Gebaeudeadressen_V1_0");

    public IlidataService(TransferDescription td) {
        this.td = td;
    }
    
    public void createIlidataXml() {
        log.info("*************");
    }
}
