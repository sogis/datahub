package ch.so.agi.datahub;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.catalina.Context;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyTomcatWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${app.targetDirectory}")
    private String targetDirectory;

    @Value("${app.directoryListing.username}")
    private String username;

    @Value("${app.directoryListing.password}")
    private String password;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
//        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
//        mappings.add("ili","text/plain; charset=UTF-8");
//        mappings.add("ini","text/plain; charset=UTF-8");
//        factory.setMimeMappings(mappings);

        Path path = Paths.get(targetDirectory);
        
        factory.addContextValves(new BasicAuthenticator());
        factory.addContextCustomizers(ctx -> {
//            String AUTH_ROLE = "admin";
//            LoginConfig config = new LoginConfig();
//            config.setAuthMethod("BASIC");
//            ctx.setLoginConfig(config);
//            ctx.addSecurityRole(AUTH_ROLE);
//            SecurityConstraint constraint = new SecurityConstraint();
//            constraint.addAuthRole(AUTH_ROLE);
//            SecurityCollection collection = new SecurityCollection();
//            collection.addPattern("/"+path.getFileName().toString()+"/*");
//            constraint.addCollection(collection);
//            ctx.addConstraint(constraint);
            
            String AUTH_ROLE = "admin";

            // Configure BASIC authentication
            LoginConfig config = new LoginConfig();
            config.setAuthMethod("BASIC");
            ctx.setLoginConfig(config);
            ctx.addSecurityRole(AUTH_ROLE);

            // Set up security constraints
            SecurityConstraint constraint = new SecurityConstraint();
            constraint.addAuthRole(AUTH_ROLE);
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/" + path.getFileName().toString() + "/*");
            constraint.addCollection(collection);
            ctx.addConstraint(constraint);

            // Set up a custom memory realm with users
            ctx.setRealm(new CustomRealm(username, password, AUTH_ROLE));
        });
        
        TomcatContextCustomizer tomcatContextCustomizer = new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                context.setDocBase(path.getParent().toAbsolutePath().toString());   

                Wrapper defServlet = (Wrapper) context.findChild("default");
                defServlet.addInitParameter("listings", "true");
                defServlet.addInitParameter("sortListings", "true");
                defServlet.addInitParameter("sortDirectoriesFirst", "true");
                defServlet.addInitParameter("readOnly", "true");
                defServlet.addMapping("/" + path.getFileName().toString() + "/*");       
            }
        };
        factory.addContextCustomizers(tomcatContextCustomizer);        
        
    }

}
