package hbv.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter({"/booking.html", "/appointments.html"})
public class PatientAuthFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        boolean isLoggedIn = session != null && session.getAttribute("loggedin") != null 
                && (Boolean) session.getAttribute("loggedin");
        boolean isAdmin = session != null && session.getAttribute("userRole") != null 
                && "ADMIN".equals(session.getAttribute("userRole"));
        
        if (isLoggedIn && !isAdmin) {
            chain.doFilter(request, response);
        } else if (isAdmin) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin");
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        }
    }
    
    @Override
    public void destroy() {
    }
} 