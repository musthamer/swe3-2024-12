package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import redis.clients.jedis.Jedis;

public class RedisServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        String redisServer = getServletContext().getInitParameter("redisserver");
        String redisPass = getServletContext().getInitParameter("redispassword");
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        long start = System.nanoTime();
        Jedis jedis = new Jedis(redisServer, 6379);
        if(redisPass != null && !redisPass.isEmpty()){
            jedis.auth(redisPass);
        }
        Long result = jedis.incr("bar");
        long end = System.nanoTime();
        out.format("value:%10d %11.4fms\n", result, (end-start)/1e6);
        jedis.close();
    }
}
