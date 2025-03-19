package hbv.web.servlet;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
public class MyFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("MyFilter: " + ((HttpServletRequest)request).getRequestURI());
        chain.doFilter(request, response);
    }
    public void destroy() { }
}
