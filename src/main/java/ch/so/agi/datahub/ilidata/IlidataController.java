package ch.so.agi.datahub.ilidata;

import java.io.InputStream;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IlidataController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IlidataService ilidataService;
    
    public IlidataController(IlidataService ilidataService) {
        this.ilidataService = ilidataService;
    }
    
    @GetMapping(value="/ilisite.xml")
    public ResponseEntity<?> ilisite() {
        InputStream is = getClass()
                .getResourceAsStream("/ili/ilisite.xml");
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML).body(new InputStreamResource(is));
    }
    
    @GetMapping(value="/ilimodels.xml")
    public ResponseEntity<?> ilimodels() {
        InputStream is = getClass()
                .getResourceAsStream("/ili/ilimodels.xml");
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML).body(new InputStreamResource(is));            
    }
    
    @GetMapping(value="/ilidata.xml")
    public ResponseEntity<Resource> ilidata() {
        Path ilidataXml = ilidataService.createXml();
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML).body(ilidataXml);            
    }



}
