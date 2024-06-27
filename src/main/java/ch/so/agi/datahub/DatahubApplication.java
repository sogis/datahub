package ch.so.agi.datahub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.so.agi.datahub.cayenne.CoreApikey;
import ch.so.agi.datahub.cayenne.CoreOrganisation;
import jakarta.annotation.PreDestroy;

@Configuration
@EnableScheduling
@SpringBootApplication
public class DatahubApplication {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.dbSchemaConfig}")
    private String dbSchemaConfig;
    
    @Value("${app.adminAccountInit}")
    private boolean adminAccountInit;

    @Value("${app.adminAccountName}")
    private String adminAccountName;

    @Value("${app.adminAccountMail}")
    private String adminAccountMail;

    @Value("${app.workDirectory}")
    private String workDirectory;

    @Value("${app.targetDirectory}")
    private String targetDirectory;

    @Value("${app.createDirectories}")
    private boolean createDirectories;

    @Autowired 
    private DataSource dataSource;
    
    private ServerRuntime cayenneRuntime;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(DatahubApplication.class, args);
        
//        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
//        for(String beanName : allBeanNames) {
//            System.out.println(beanName);
//        }
    }
    
    @Bean
    ObjectContext objectContext() {        
        cayenneRuntime = ServerRuntime.builder()
                .dataSource(dataSource)
                .addConfig("cayenne/cayenne-project.xml")
                .build();

        ObjectContext context = cayenneRuntime.newContext();
        
        return context;
    }
    
    @PreDestroy
    public void shutdownCayenne() {
        cayenneRuntime.shutdown();
    }

    @Bean
    CommandLineRunner init(ObjectContext objectContext) {
        return args -> {
            // Add admin account to database.
            // Show admin key once in the console.
            if (adminAccountInit) {
                CoreOrganisation existingOrg = ObjectSelect.query(CoreOrganisation.class)
                        .where(CoreOrganisation.ANAME.eq(adminAccountName))
                        .selectFirst(objectContext);

                if (existingOrg != null) {
                    logger.warn("Account name '{}' already exists.", adminAccountName);
                } else {
                    CoreOrganisation coreOrganisation = objectContext.newObject(CoreOrganisation.class);
                    coreOrganisation.setAname(adminAccountName);
                    coreOrganisation.setArole(AppConstants.ROLE_NAME_ADMIN);
                    coreOrganisation.setEmail(adminAccountMail);
                    
                    String apiKey = UUID.randomUUID().toString();
                    String encodedApiKey = encoder().encode(apiKey);
                    
                    CoreApikey coreApiKey = objectContext.newObject(CoreApikey.class);
                    coreApiKey.setApikey(encodedApiKey);
                    coreApiKey.setCreatedat(LocalDateTime.now());
                    coreApiKey.setCoreOrganisation(coreOrganisation);
                    
                    objectContext.commitChanges();
                    
                    logger.warn("************************************************************");
                    logger.warn(apiKey);
                    logger.warn("************************************************************");
                }
            }
            
            // Create work and target directory.
            // Application user must have permissions to do so.
            if (createDirectories) {
                try {
                    Files.createDirectories(Paths.get(workDirectory));
                    Files.createDirectories(Paths.get(targetDirectory));
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                    throw new Exception(); // Shuts down application.
                }                
            }
        };
    }
    
    @Bean
    PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    ResourceBundle resourceBundle() {
        Locale locale = Locale.of("de", "CH");
        ResourceBundle rsrc = ResourceBundle.getBundle("DatahubMessages", locale);
        return rsrc;
    }
}
