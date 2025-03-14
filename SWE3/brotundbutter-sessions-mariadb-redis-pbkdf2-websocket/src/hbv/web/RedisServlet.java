package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import redis.clients.jedis.*;

public class RedisServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    ServletContext ctx = getServletContext();
    String redispassword = ctx.getInitParameter("redispassword");
    String redisserver = ctx.getInitParameter("redisserver");

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();

    long start = System.nanoTime();
    Jedis jedis = new Jedis(redisserver, 6379);
    jedis.auth(redispassword);

    Long result = jedis.incr("bar");
    long ende = System.nanoTime();
    out.format("value:%10d%11.4fms\n", result, (double) ((ende - start) / 1000000d));
    jedis.close();
  }
}
