package spring_jedis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import spring_jedis.jedis.JedisConfig;
import spring_jedis.jedis.JedisTemplate;
import io.netty.util.concurrent.FastThreadLocal;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfiguration {
	private final JedisPoolConfig mJedisPoolConfig = new JedisPoolConfig();

	@Bean
	public JedisConfig getJedisConfig() {
		return new JedisConfig();
	}

	public final FastThreadLocal<JedisTemplate> FTL = new FastThreadLocal<JedisTemplate>() {
		@Override
		protected JedisTemplate initialValue() throws Exception {
			JedisConfig jedisConfig = getJedisConfig();
			//logger.debug("mFastThreadLocal :{} json:{}", "RedisProvider 连接池初始化",binder.toJson(getJedisConfig()));
			JedisPool vJedisPool = new JedisPool(mJedisPoolConfig, jedisConfig.getHost(), Integer.parseInt(jedisConfig.getPort()), jedisConfig.getTimeout(), jedisConfig.getPassword(),jedisConfig.getDatabase());

			JedisTemplate vTemplate = new JedisTemplate(vJedisPool);
			
			return vTemplate;
		}
	};
	
	@Bean
	public JedisTemplate getJedisTemplate() {
		return FTL.get();
	}
}

