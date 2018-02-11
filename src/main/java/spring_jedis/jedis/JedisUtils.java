package spring_jedis.jedis;

import redis.clients.jedis.Jedis;
public class JedisUtils {

    private static final String OK_CODE = "OK";
    private static final String OK_MULTI_CODE = "+OK";


    /**
     * 判断 返回值是否ok.
     */
    public static boolean isStatusOk(String status) {
        return (status != null) && (OK_CODE.equals(status) || OK_MULTI_CODE.equals(status));
    }

    /**
     * Return jedis connection to the pool, call different return methods depends on the conectionBroken status.
     */
	public static void closeResource(Jedis jedis) {
        try {
            jedis.close();
        } catch (Exception e) {
            //logger.error("return back jedis failed, will fore close the jedis.", e);
            destroyJedis(jedis);
        }
    }

    /**
     * 在Pool以外强行销毁Jedis.
     */
    public static void destroyJedis(Jedis jedis) {
        if ((jedis != null) && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                    //logger.error("在Pool以外强行销毁Jedis quit error:{}", e.getMessage());
                }
                jedis.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                //logger.error("在Pool以外强行销毁Jedis disconnect error:{}", e.getMessage());
            }
        }
    }
}
