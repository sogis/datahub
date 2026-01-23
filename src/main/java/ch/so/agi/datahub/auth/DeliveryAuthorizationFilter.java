package ch.so.agi.datahub.auth;

import java.io.IOException;
import java.time.Instant;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.cayenne.CoreOperat;
import ch.so.agi.datahub.cayenne.CoreOrganisation;
import ch.so.agi.datahub.cayenne.CoreTheme;
import ch.so.agi.datahub.model.DeliveryAuthorizationInfo;
import ch.so.agi.datahub.model.ApiError;
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
        
    private ObjectContext objectContext;
     
    private ObjectMapper mapper;
    
    public DeliveryAuthorizationFilter(ObjectContext objectContext, ObjectMapper mapper) {
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
            mapper.writeValue(responseStream, new ApiError(this.getClass().getCanonicalName(), "Missing parameter", Instant.now(),
                    request.getRequestURI(), null));
            responseStream.flush();
            return;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String orgName = null;
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN))) {
            orgName = null;
        } else {
            orgName = authentication.getName();
        }
                 
        
        var expression = CoreOperat.ANAME.eq(operatName)
                .andExp(CoreOperat.CORE_THEME.dot(CoreTheme.ANAME).eq(themeName));

        if (orgName != null) {
            expression = expression.andExp(CoreOperat.CORE_ORGANISATION.dot(CoreOrganisation.ANAME).eq(orgName));
        }

        CoreOperat coreOperat = ObjectSelect
                .query(CoreOperat.class)
                .where(expression)
                .selectOne(objectContext);

        logger.trace("CoreOperat: {}", coreOperat);
        
        if (coreOperat == null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ServletOutputStream responseStream = response.getOutputStream();
            mapper.writeValue(responseStream, new ApiError(this.getClass().getCanonicalName(), "User is not authorized", Instant.now(),
                    request.getRequestURI(), null));
            responseStream.flush();
        } else {
            DeliveryAuthorizationInfo authorizationInfo = new DeliveryAuthorizationInfo(
                    coreOperat.getCoreOrganisation().getAname(),
                    coreOperat.getCoreOrganisation().getEmail(),
                    coreOperat.getCoreTheme().getConfig(),
                    coreOperat.getCoreTheme().getMetaconfig());
            request.setAttribute(AppConstants.ATTRIBUTE_OPERAT_DELIVERY_INFO, authorizationInfo);
            filterChain.doFilter(request, response);            
        }
    }
}
