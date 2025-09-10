package ch.so.agi.datahub.ilidata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxWriter;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;

@Service
public class IlidataService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.targetDirectory}")
    private String targetDirectory;
    
    private final TransferDescription td;
    
    // Alle Verzeichnisse (=Themen=Datenmodelle), die vom NF-Geometer
    // beliefert werden.
    private static final Map<String, String> DIRECTORIES = Map.of(
            "DMAV_FixpunkteAVKategorie3_V1_0", "ch.so.agi.dmav.relational.fixpunkteavkategorie3",
            "DMAV_Dienstbarkeitsgrenzen_V1_0", "ch.so.agi.dmav.relational.dienstbarkeitsgrenzen", // for testing
            "DMAV_Grundstuecke_V1_0", "ch.so.agi.dmav.relational.grundstuecke",
            "DMAV_HoheitsgrenzenAV_V1_0", "ch.so.agi.dmav.relational.hoheitsgrenzenav",
            "DMAV_Bodenbedeckung_V1_0", "ch.so.agi.dmav.relational.bodenbedeckung",
            "DMAV_Einzelobjekte_V1_0", "ch.so.agi.dmav.relational.einzelobjekte",
            "DMAV_Gebaeudeadressen_V1_0", "ch.so.agi.dmav.relational.gebaeudeadressen");

    public IlidataService(TransferDescription td) {
        this.td = td;
    }
    
    public Path createXml()  {        
        File dataFile = null;
        try {
            Path tempDir = Files.createTempDirectory("datahub-ilidata-");
            dataFile = Paths.get(tempDir.toString(), "ilidata.xml").toFile();
            IoxWriter ioxWriter = new XtfWriter(dataFile, td);
            ioxWriter.write(new StartTransferEvent("SOGIS-DATAHUB", "", null));
            ioxWriter.write(new StartBasketEvent("IliRepository09.RepositoryIndex", "DatasetIdx16.DataIndex"));

            int tid=1;
            for (String directory : DIRECTORIES.keySet()) {
                Path modelDirectory = Paths.get(targetDirectory, directory);
                if (Files.exists(modelDirectory)) { 
                    List<Path> xtfFiles = null;
                    try {
                        xtfFiles = Files.list(modelDirectory)
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().toLowerCase().endsWith(".xtf"))
                                .collect(Collectors.toList());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    for (Path xtfFile : xtfFiles) {                        
                        int lastDotIndex = xtfFile.getFileName().toString().lastIndexOf('.');
                        String fileName = xtfFile.getFileName().toString().substring(0, lastDotIndex);
                        
                        String datasetId = fileName + "." + DIRECTORIES.get(directory);
                        
                        Iom_jObject iomObj = new Iom_jObject("DatasetIdx16.DataIndex.DatasetMetadata", String.valueOf(tid));
                        iomObj.setattrvalue("id", datasetId);
                        FileTime creationTime = (FileTime) Files.getAttribute(xtfFile, "creationTime");
                        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                            creationTime.toInstant(), 
                            ZoneId.systemDefault()
                        );
                        iomObj.setattrvalue("version", localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                        iomObj.setattrvalue("owner", "https://agi.so.ch");
                        iomObj.setattrvalue("technicalContact", "https://agi.so.ch");
                        
                        Iom_jObject model = new Iom_jObject("DatasetIdx16.ModelLink", null);
                        model.setattrvalue("name", directory);
                        model.setattrvalue("locationHint", "https://models.geo.admin.ch");
                        iomObj.addattrobj("model", model);

                        iomObj.setattrvalue("epsgCode", "2056");
                        iomObj.setattrvalue("publishingDate", localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));

                        Iom_jObject files = new Iom_jObject("DatasetIdx16.DataFile", null); 
                        files.setattrvalue("fileFormat", "application/interlis+xml;version=2.4");
                        Iom_jObject file = new Iom_jObject("DatasetIdx16.File", null);
                        file.setattrvalue("path", "files/"+directory+"/"+xtfFile.getFileName().toString());
                        files.addattrobj("file", file);
                        iomObj.addattrobj("files", files);
                        
                        ioxWriter.write(new ObjectEvent(iomObj));                            

                        tid++;
                    }                    
                }
            }
            ioxWriter.write(new EndBasketEvent());
            ioxWriter.write(new EndTransferEvent());
            ioxWriter.flush();
            ioxWriter.close();
        } catch (IOException | IoxException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        
        return dataFile.toPath();
    }    
}
