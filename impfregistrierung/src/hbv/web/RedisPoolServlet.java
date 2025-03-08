package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import redis.clients.jedis.Jedis;

public class RedisPoolServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        long start = System.nanoTime();
        Jedis jedis = JedisAdapter.getJedis();
        Long result = jedis.incr("bar");
        JedisAdapter.releaseJedis(jedis);
        long end = System.nanoTime();
        out.format("value:%10d %11.4fms\n", result, (end-start)/1e6);
    }
}
