package ch.so.agi.datahub;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyTomcatWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${app.targetDirectory}")
    private String targetDirectory;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
//        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
//        mappings.add("ili","text/plain; charset=UTF-8");
//        mappings.add("ini","text/plain; charset=UTF-8");
//        factory.setMimeMappings(mappings);

        TomcatContextCustomizer tomcatContextCustomizer = new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                Path path = Paths.get(targetDirectory);
                
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
