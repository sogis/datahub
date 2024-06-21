package ch.so.agi.datahub.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader Map<String, String> headers) {
        
        headers.forEach((key, value) -> {
            logger.info(String.format("Header '%s' = %s", key, value));
        });
        
        logger.info("ping");        
        return new ResponseEntity<String>("datahub", HttpStatus.OK);
    }
    
    @GetMapping("/protected/hello")
    public String foo(Authentication authentication) {
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Name: " + authentication.getName());
        return "Hello, this is a secured endpoint!";
    }
    
    @GetMapping("/public/hello")
    public String hello() {
        return "Hello, this is a _un_secured endpoint!";
    }
}
