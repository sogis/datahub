package ch.so.agi.datahub.ftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@ConditionalOnProperty(
        value="app.ftp.enabled", 
        havingValue = "true", 
        matchIfMissing = false)
@Component
public class MyFtpServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.ftp.usersFile}")
    private String usersFile;

    @Value("${app.ftp.port}")
    private int port;

    @Value("${app.ftp.username}")
    private String username;

    @Value("${app.ftp.password}")
    private String password;
    
    @Value("${app.targetDirectory}")
    private String targetDirectory;

    private static final int MAX_IDLE_TIME = 300;
    
    private static final String passivePorts = "2121-2199";

    private FtpServer ftpServer;
    
    @PostConstruct
    private void start() throws FtpException, IOException {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);

        DataConnectionConfigurationFactory dataConnectionConfFactory = new DataConnectionConfigurationFactory();
        dataConnectionConfFactory.setPassivePorts(passivePorts);
        factory.setDataConnectionConfiguration(dataConnectionConfFactory.createDataConnectionConfiguration());

        serverFactory.addListener("default", factory.createListener());

        // Wir erstellen eine leere Datei. Diese wird benötigt, um
        // anschliessend (programmtisch) einen Benutzer erstellen
        // zu können.
        // Die Datei wird beim Hochfahren immer wieder neu angelegt
        // und ggf überschrieben.
        Path usersFilePath = Paths.get(usersFile);
        if (Files.exists(usersFilePath)) {
            Files.delete(usersFilePath);
        }
        Files.createFile(usersFilePath);

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(usersFilePath.toFile());
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        UserManager um = userManagerFactory.createUserManager();

        List<Authority> authorities = new ArrayList<>();
//      authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(0, 0));
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        user.setHomeDirectory(targetDirectory);
        user.setMaxIdleTime(MAX_IDLE_TIME);
        user.setAuthorities(authorities);
        um.save(user);
        
        serverFactory.setUserManager(um);

        ftpServer = serverFactory.createServer();
        ftpServer.start();
    }

    @PreDestroy
    private void stop() {
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }
}
