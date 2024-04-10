package ch.so.agi.datahub.auth;

import java.io.IOException;
import java.time.Instant;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.SQLSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.model.GenericResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


// Wenn hier Exceptions auftreten, wird es weitergereicht und es folgt
// ein 403er. 
@Component
public class DeliveryAuthorizationFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.dbSchemaConfig}")
    private String dbSchema;
        
    private ObjectContext objectContext;
     
    private ObjectMapper mapper;
    
    public DeliveryAuthorizationFilter(ObjectContext objectContext, PasswordEncoder encoder, ObjectMapper mapper) {
        this.objectContext = objectContext;
        this.mapper = mapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {        
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String themeName = servletRequest.getParameter("theme");
        String operatName = servletRequest.getParameter("operat");
        
        if (themeName == null || operatName == null) {
            logger.error("theme or operat parameter is missing");
            
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ServletOutputStream responseStream = response.getOutputStream();
            mapper.writeValue(responseStream, new GenericResponse(this.getClass().getCanonicalName(), "Missing parameter", Instant.now()));
            responseStream.flush();
            return;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String orgName = null;
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN))) {
            orgName = "%";
        } else {
            orgName = authentication.getName();
        }
                 
        
        String stmt = """
SELECT 
    th.t_id AS theme_tid,
    th.aname AS theme_name,
    org.aname AS org_name, 
    org.email,
    th.config,
    th.metaconfig,
    op.t_id AS operat_tid,
    op.aname AS operat_name
FROM 
    $core_organisation_table AS org 
    LEFT JOIN $core_operat_table AS op 
    ON org.t_id = op.organisation_r 
    LEFT JOIN $core_theme_table AS th 
    ON th.t_id = op.theme_r 
WHERE 
    org.aname LIKE '$organisation_name'
    AND 
    op.aname = '$operat_name'
    AND 
    th.aname = '$theme_name'
                """;
        
        DataRow result = SQLSelect
                .dataRowQuery(stmt)
                .param("core_organisation_table", dbSchema + ".core_organisation")
                .param("core_operat_table", dbSchema + ".core_operat")
                .param("core_theme_table", dbSchema + ".core_theme")
                .param("organisation_name", orgName)
                .param("operat_name", operatName)
                .param("theme_name", themeName)
                .selectOne(objectContext);

        logger.trace("DataRow: {}", result);
        
        if (result == null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ServletOutputStream responseStream = response.getOutputStream();
            mapper.writeValue(responseStream, new GenericResponse(this.getClass().getCanonicalName(), "User is not authorized", Instant.now()));
            responseStream.flush();
        } else {
            request.setAttribute(AppConstants.ATTRIBUTE_OPERAT_DELIVERY_INFO, result);
            filterChain.doFilter(request, response);            
        }
    }
}
