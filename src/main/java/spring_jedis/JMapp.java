package spring_jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import spring_jedis.jedis.JedisTemplate;


@SpringBootApplication
@ServletComponentScan
public class JMapp implements CommandLineRunner {
	
	@Autowired
	JedisTemplate jedisTemplate;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(JMapp.class, args);
	}

	public void run(String... args) throws Exception {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		jedisTemplate.set("test", "tutu");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		String key = jedisTemplate.get("test");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+key);
	}


}

