package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import redis.clients.jedis.*;

public class RedisPoolServlet extends HttpServlet {
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    long start = System.nanoTime();
    Jedis jedis = JedisAdapter.getJedis();
    long result = jedis.incr("bar");
    JedisAdapter.releaseJedis(jedis);
    long ende = System.nanoTime();
    out.println(result);
    out.format("value:%10d%11.4fms\n", result, (ende - start) / 1000000d);
  }
}
