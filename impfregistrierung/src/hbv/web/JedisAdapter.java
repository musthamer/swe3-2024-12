package hbv.web;

import redis.clients.jedis.Jedis;

public class JedisAdapter {
    private static String host;
    private static int port;
    private static String password;

    public static void init(String h, int p, String pass) {
        host = h;
        port = p;
        password = pass;
    }

    public static Jedis getJedis(){
        Jedis jedis = new Jedis(host, port);
        if(password != null && !password.isEmpty()){
            jedis.auth(password);
        }
        return jedis;
    }

    public static void releaseJedis(Jedis jedis){
        if(jedis != null) {
            jedis.close();
        }
    }

    public static void destroy(){
        // Kein Pool vorhanden – nichts zu tun.
    }
}
