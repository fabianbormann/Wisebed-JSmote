/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Fabian
 */
public class SecurityFilter implements Filter {

    private final String[] forbiddenSites = {"home.xhtml","share.xhtml",
        "experiments.xhtml", "settings.xhtml"};
    
    private final String[] forbiddenSites_debug = {"nothing.xhtml"};
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse serveltResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession(true);
        
        if (session.getAttribute("authenticated") != null || isNotForbidden(request.getRequestURI())) {
            chain.doFilter(servletRequest, serveltResponse);
            Logger.getLogger(SecurityFilter.class.getName()).log(Level.INFO, "ok...");
        } else {
            HttpServletResponse response = (HttpServletResponse) serveltResponse;
            response.sendRedirect(request.getContextPath() + "/faces/index.xhtml");
            Logger.getLogger(SecurityFilter.class.getName()).log(Level.INFO, "nicht ok");
        }
    }

    private boolean isNotForbidden(String URI){
        
        for(String site : forbiddenSites_debug){
            if(URI.contains(site))
                return false;
        }
        
        return true;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void destroy() {}
}
