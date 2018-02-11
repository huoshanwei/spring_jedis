package spring_jedis.jedis;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * =====================================================================================
 *
 * @Filename    :   JedisTemplate.java
 * @Description :   JedisTemplate
 * @Version     :   3.0
 * @Created     :   2017年11月15日 12:14:06
 * @Compiler    :   jdk 1.8
 * @Author      :   冯兵兵
 * @Email       :   cnrainbing@163.com
 * @Copyright   :   简媒(http://www.ejianmedia.com/)
 * =====================================================================================
 */
public class JedisTemplate {

    private JedisPool jedisPool;

    public JedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * T 输入
     * R 返回
     * Callback interface for template.
     */
    @FunctionalInterface
    public interface JedisAction<T, R> {
        R action(T jedis);
    }

    /**
     * Callback interface for template.
     */
    @FunctionalInterface
    public interface PipelineAction<T, R> {
        R action(Pipeline Pipeline);
    }

    /**
     * Callback interface for template without result.
     */
    /*public interface PipelineActionNoResult {
        void action(Pipeline Pipeline);
    }*/


    /**
     * Execute with a call back action with result.
     */
    public <R> R execute(JedisAction<Jedis, R> jedisAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedisAction.action(jedis);
        } catch (JedisException e) {
            //logger.error("JedisAction error :{}", e.getMessage());
            throw e;
        } finally {
            //closeResource(jedis);
        }
    }

    /**
     * Execute with a call back action without result.
     */
    public void execute(Consumer<Jedis> jedisAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedisAction.accept(jedis);
        } catch (JedisException e) {
            //logger.error("JedisActionNoResult error :{}", e.getMessage());
            throw e;
        } finally {
            //closeResource(jedis);
        }
    }


    /**
     * Execute with a call back action with result in pipeline.
     */
    public <R> R execute(PipelineAction<Pipeline, R> pipelineAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            return pipelineAction.action(pipeline);
        } catch (JedisException e) {
            //logger.error("PipelineAction error :{}", e.getMessage());
            throw e;
        } finally {
            //closeResource(jedis);
        }
    }

    /**
     * Execute with a call back action without result in pipeline.
     */
    public void executePipeline(Consumer<Pipeline> pipelineAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipelineAction.accept(pipeline);
            pipeline.sync();
        } catch (JedisException e) {
            //logger.error("PipelineActionNoResult error :{}", e.getMessage());
            throw e;
        } finally {
            //closeResource(jedis);
        }
    }

    /**
     * Return the internal JedisPool.
     */
    public JedisPool getJedisPool() {
        return jedisPool;
    }


    // Common Actions S

	public boolean Exists(final String key){
		//logger.debug("redis Exists key:{}",key);

		JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
			return jedis.exists(key);
		};

		return execute(vJedisAction);
	}

    /**
     * 清空
     */
    public void flushDB() {
        Consumer<Jedis> vJedisActionNoResult = (jedis) -> {
            jedis.flushDB();
        };

        execute(vJedisActionNoResult);
    }

	/**
	 * dbSize 当前库key的数量
	 */
	public Long dbSize() {
		JedisAction<Jedis, Long> vJedisAction = (jedis) -> jedis.dbSize();

		return execute(vJedisAction);
	}

    /***
     * @param keys
     * @return false if one of the key is not exist.
     */
    public Boolean del(final String... keys) {
        JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
            return jedis.del(keys) == keys.length ? true : false;
        };

        return execute(vJedisAction);
    }

    //================================String Action S ============================================

    /**
     * Get the value of the specified key. If the key does not exist null is
     * returned. If the value stored at key is not a string an error is returned
     * because GET can only handle string values.
     */
    public String get(final String key) {
        JedisAction<Jedis, String> vJedisAction = (jedis) -> jedis.get(key);

        return execute(vJedisAction);
    }

    /**
     * Get the value of the specified key as Long.If the key does not exist null is returned.
     */
    public Long getAsLong(final String key) {
        String result = get(key);

        return result != null ? Long.valueOf(result) : null;
    }

    /**
     * Get the value of the specified key as Integer.If the key does not exist null is returned.
     */
    public Integer getAsInt(final String key) {
        String result = get(key);

        return result != null ? Integer.valueOf(result) : null;
    }

    public List<String> mget(final String... keys) {
        JedisAction<Jedis, List<String>> vJedisAction = (jedis) -> jedis.mget(keys);

        return execute(vJedisAction);
    }

    /**
     * Set the string value as value of the key.
     * The string can't be longer than 1073741824 bytes (1 GB).
     */
    public void set(final String key, final String value) {
        Consumer<Jedis> vJedisActionNoResult = (jedis) -> {
            jedis.set(key, value);
        };

        execute(vJedisActionNoResult);
    }

    /**
     * The command is exactly equivalent to the following group of commands: {@link #set(String, String) SET} +
     * {@link # expire(String, int) EXPIRE}.
     * The operation is atomic.
     */
    public void Setex(final String key, final String value, final int seconds) {
        Consumer<Jedis> vJedisActionNoResult = (jedis) -> {
            jedis.setex(key, seconds, value);
        };

        execute(vJedisActionNoResult);
    }

    /**
     * 可以实现分布式锁
     * redis> SETNX mykey “hello”
     * (integer) 1
     * redis> SETNX mykey “hello”
     * (integer) 0
     * redis> GET mykey
     * “hello”
     * <p>
     * SETNX works exactly like {@link # setNX(String, String) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * return true if the key was set.
     */
    public Boolean setnx(final String key, final String value) {
        JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> jedis.setnx(key, value) == 1 ? true : false;

        return execute(vJedisAction);
    }

    /**
     * 当使用jedis的set时，同一个key在被set两次后，通过get得到的还是第一次set的值，所以要先del掉key再进行set。
     * set使用api，原因是参数nxxx的设置问题
     * The command is exactly equivalent to the following group of commands: {@link # setex(String, String, int) SETEX} +
     * {@link # sexnx(String, String) SETNX}.
     * The operation is atomic.
	 * EX seconds − 设置指定的到期时间(以秒为单位)。
	 * PX milliseconds - 设置指定的到期时间(以毫秒为单位)。
	 * NX - 仅在键不存在时设置键。
	 * XX - 只有在键已存在时才设置
     */
    public Boolean setIfNotnxex(final String key, final String value, final int seconds) {
        JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
            String result = jedis.set(key, value, "NX", "EX", seconds);
            return JedisUtils.isStatusOk(result);
        };

        return execute(vJedisAction);
    }

	public Boolean setIfnxex(final String key, final String value, final int seconds) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
			String result = jedis.set(key, value, "XX", "EX", seconds);
			return JedisUtils.isStatusOk(result);
		};

		return execute(vJedisAction);
	}

	/**
	 * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
	 * GB).
	 * @param key
	 * @param value
	 * @param milliseconds expire time in the units of milliseconds
	 * @return Status code reply
	 */
	public Boolean setIfNotnxpx(final String key, final String value, final long milliseconds) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
			String result = jedis.set(key, value, "NX", "PX", milliseconds);
			return JedisUtils.isStatusOk(result);
		};
		return execute(vJedisAction);
	}

	/**
	 * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
	 * GB).
	 * @param key
	 * @param value
	 * @param milliseconds expire time in the units of milliseconds
	 * @return Status code reply
	 */
	public Boolean setIfnxpx(final String key, final String value, final long milliseconds) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) -> {
			String result = jedis.set(key, value, "XX", "PX", milliseconds);
			return JedisUtils.isStatusOk(result);
		};
		return execute(vJedisAction);
	}

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     */
    public String getSet(final String key, final String value) {
        JedisAction<Jedis, String> vJedisAction = (jedis) -> jedis.getSet(key, value);

        return execute(vJedisAction);
    }
    
    /**
	 * Increment the number stored at key by one. If the key does not exist or
	 * contains a value of a wrong type, set the key to the value of "0" before
	 * to perform the increment operation.
	 * <p>
	 * INCR commands are limited to 64 bit signed integers.
	 * <p>
	 * Note: this is actually a string operation, that is, in Redis there are not "integer" types. Simply the string
	 * stored at the key is parsed as a base 10 64 bit signed integer, incremented, and then converted back as a string.
	 * 
	 * @return Integer reply, this commands will reply with the new value of key
	 *         after the increment.
	 */
	public Long incr(final String key) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) -> jedis.incr(key);
		
		return execute(vJedisAction);
	}
	
	public Long incrBy(final String key, final long increment) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) -> jedis.incrBy(key,increment);
		
		return execute(vJedisAction);
	}

	/**
	 * Decrement the number stored at key by one. If the key does not exist or
	 * contains a value of a wrong type, set the key to the value of "0" before
	 * to perform the decrement operation.
	 */
	public Long decr(final String key) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) -> jedis.decr(key);
		
		return execute(vJedisAction);
	}
	
	public Long decrBy(final String key, final long decrement) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) -> jedis.decrBy(key, decrement);
		
		return execute(vJedisAction);
	}
    //================================String Action E ============================================

    
    
	//================================List Action S ============================================
	/**
	 * Redis Lpush 命令将一个或多个值插入到列表头部。 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。 当 key 存在但不是列表类型时，返回一个错误。
	 * @param key
	 * @param values
	 * @return
	 */
    public Long lpush(final String key, final String... values) {
    	JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
    		return jedis.lpush(key, values);
    	};
    	
    	return execute(vJedisAction);
    }
    
    /**
     * Redis Rpop 命令用于移除并返回列表的最后一个元素。
     * @param key
     * @return
     */
    public String rpop(final String key) {
    	JedisAction<Jedis, String> vJedisAction = (jedis) ->{
    		return jedis.rpop(key);
    	};
    	
    	return execute(vJedisAction);
    }
    
    public String brpop(final String key) {
    	JedisAction<Jedis, String> vJedisAction = (jedis) ->{
    		List<String> nameValuePair = jedis.brpop(new String[] { key });
			if (nameValuePair != null) {
				return nameValuePair.get(1);
			} else {
				return null;
			}
    	};
    	
		return execute(vJedisAction);
	}
    
    /**
     * Redis Brpop 命令移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @param timeout
     * @param key
     * @return
     */
    public String brpop(final int timeout, final String key) {
    	JedisAction<Jedis, String> vJedisAction = (jedis) ->{
    		List<String> nameValuePair = jedis.brpop(timeout, key);
			if (nameValuePair != null) {
				return nameValuePair.get(1);
			} else {
				return null;
			}
    	};
    	
		return execute(vJedisAction);
	}
    
    /**
     * Redis Rpoplpush 命令用于移除列表的最后一个元素，并将该元素添加到另一个列表并返回。
     * @param sourceKey
     * @param destinationKey
     * 
     * Not support for sharding.
     * @return
     */
    public String rpoplpush(final String sourceKey, final String destinationKey) {
    	JedisAction<Jedis, String> vJedisAction = (jedis) ->{
    		return jedis.rpoplpush(sourceKey, destinationKey);
    	};
    	
		return execute(vJedisAction);
	}
    
    /**
     * Redis Brpoplpush 命令从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
	 * Not support for sharding.
	 */
	public String brpoplpush(final String source, final String destination, final int timeout) {
		JedisAction<Jedis, String> vJedisAction = (jedis) ->{
			return jedis.brpoplpush(source, destination, timeout);
    	};
    	
		return execute(vJedisAction);
	}
	
	/***
	 * Redis Llen 命令用于返回列表的长度。 如果列表 key 不存在，则 key 被解释为一个空列表，返回 0 。 如果 key 不是列表类型，返回一个错误。
	 * @param key
	 * @return
	 */
	public Long llen(final String key) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.llen(key);
    	};
    	
		return execute(vJedisAction);
	}
	
	/***
	 * Redis Lindex 命令用于通过索引获取列表中的元素。你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推
	 * @param key
	 * @param index
	 * @return
	 */
	public String lindex(final String key, final long index) {
		JedisAction<Jedis, String> vJedisAction = (jedis) ->{
			return jedis.lindex(key, index);
    	};
    	
		return execute(vJedisAction);
	}
	
	/**
	 * Redis Lrange 返回列表中指定区间内的元素，区间以偏移量 START 和 END 指定。 其中 0 表示列表的第一个元素，  1 表示列表的第二个元素，以此类推。 
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> lrange(final String key, final int start, final int end) {
		JedisAction<Jedis, List<String>> vJedisAction = (jedis) ->{
			return jedis.lrange(key, start, end);
    	};
    	
		return execute(vJedisAction);
	}
	
	/**
	 * Redis Ltrim 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
	 * 下标 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * @param key
	 * @param start
	 * @param end
	 */
	public void ltrim(final String key, final int start, final int end) {
		Consumer<Jedis> vJedisActionNoResult = (jedis) -> {
			jedis.ltrim(key, start, end);
        };
        
		execute(vJedisActionNoResult);
	}
	
	public void ltrimFromLeft(final String key, final int size) {
		Consumer<Jedis> vJedisActionNoResult = (jedis) -> {
			jedis.ltrim(key, 0, size - 1);
        };
        
		execute(vJedisActionNoResult);
	}
	
	/***
	 *  Redis Lrem 根据参数 COUNT 的值，移除列表中与参数 VALUE 相等的元素。
	 *  COUNT 的值可以是以下几种：
	 *  count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为 COUNT 。
	 *  count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值。
	 *  count = 0 : 移除表中所有与 VALUE 相等的值。
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean lremFirst(final String key, final String value) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) ->{
			Long count = jedis.lrem(key, 1, value);
			return (count == 1);
    	};
    	
		return execute(vJedisAction);
	}
	
	public Boolean lremAll(final String key, final String value) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) ->{
			Long count = jedis.lrem(key, 0, value);
			return (count > 0);
    	};
    	
		return execute(vJedisAction);
	}
    //================================List Action E ============================================
    
    
	
    //================================Set Actions  S ============================================
	/***
	 * Redis Sadd 命令将一个或多个成员元素加入到集合中，已经存在于集合的成员元素将被忽略。
	 * 假如集合 key 不存在，则创建一个只包含添加的元素作成员的集合。
	 * 当集合 key 不是集合类型时，返回一个错误。
	 * @param key
	 * @param member
	 * @return
	 */
	public Boolean sadd(final String key, final String member) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) ->{
			return jedis.sadd(key, member) == 1 ? true : false;
    	};
    	
		return execute(vJedisAction);
	}

	public String spop(final String key) {
		JedisAction<Jedis, String> vJedisAction = (jedis) ->{
			return jedis.spop(key);
		};

		return execute(vJedisAction);
	}

	public Set<String> smembers(final String key) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.smembers(key);
    	};
    	
		return execute(vJedisAction);
	}
	
	/**
	 * Redis Scard 命令返回集合中元素的数量。
	 * 当集合 key 不存在时，返回 0 。
	 * @param key
	 * @return
	 */
	public Long scard(final String key) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.scard(key);
    	};
    	
		return execute(vJedisAction);
	}
	
	/***
	 * Redis Sdiff 命令返回给定集合之间的差集。不存在的集合 key 将视为空集。
	 * @param key1
	 * @param key2
	 * @return
	 */
	public Set<String> sdiff(final String key1,final String key2) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.sdiff(key1,key2);
    	};
    	
    	return execute(vJedisAction);
	}
    //================================Set Actions  E ============================================


	//================================Ordered Set Actions  S ============================================

	/***
	 * Redis Zadd 命令用于将一个或多个成员元素及其分数值加入到有序集当中。
	 * 如果某个成员已经是有序集的成员，那么更新这个成员的分数值，并通过重新插入这个成员元素，来保证该成员在正确的位置上。
	 * 分数值可以是整数值或双精度浮点数。
	 * 如果有序集合 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
	 * 当 key 存在但不是有序集类型时，返回一个错误。
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public Boolean zadd(final String key, final double score, final String member) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) ->{
			return jedis.zadd(key, score, member) == 1 ? true : false;
		};

		return execute(vJedisAction);
	}

	public Double zscore(final String key, final String member) {
		JedisAction<Jedis, Double> vJedisAction = (jedis) ->{
			return jedis.zscore(key, member);
		};

		return execute(vJedisAction);
	}

	public Long zrank(final String key, final String member) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zrank(key, member);
		};

		return execute(vJedisAction);
	}

	public Long zrevrank(final String key, final String member) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zrevrank(key, member);
		};

		return execute(vJedisAction);
	}

	public Long zcount(final String key, final double min, final double max) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zcount(key, min, max);
		};

		return execute(vJedisAction);
	}

	public Set<String> zrange(final String key, final int start, final int end) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.zrange(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Set<Tuple> zrangeWithScores(final String key, final int start, final int end) {
		JedisAction<Jedis, Set<Tuple>> vJedisAction = (jedis) ->{
			return jedis.zrangeWithScores(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Set<String> zrevrange(final String key, final int start, final int end) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.zrevrange(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Set<Tuple> zrevrangeWithScores(final String key, final int start, final int end) {
		JedisAction<Jedis, Set<Tuple>> vJedisAction = (jedis) ->{
			return jedis.zrevrangeWithScores(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Set<String> zrangeByScore(final String key, final double min, final double max) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.zrangeByScore(key, min, max);
		};

		return execute(vJedisAction);
	}

	public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
		JedisAction<Jedis, Set<Tuple>> vJedisAction = (jedis) ->{
			return jedis.zrangeByScoreWithScores(key, min, max);
		};

		return execute(vJedisAction);
	}

	public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
		JedisAction<Jedis, Set<String>> vJedisAction = (jedis) ->{
			return jedis.zrevrangeByScore(key, max, min);
		};

		return execute(vJedisAction);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
		JedisAction<Jedis, Set<Tuple>> vJedisAction = (jedis) ->{
			return jedis.zrevrangeByScoreWithScores(key, max, min);
		};

		return execute(vJedisAction);
	}

	public Boolean zrem(final String key, final String member) {
		JedisAction<Jedis, Boolean> vJedisAction = (jedis) ->{
			return jedis.zrem(key, member) == 1 ? true : false;
		};

		return execute(vJedisAction);
	}

	public Long zremByScore(final String key, final double start, final double end) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zremrangeByScore(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Long zremByRank(final String key, final long start, final long end) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zremrangeByRank(key, start, end);
		};

		return execute(vJedisAction);
	}

	public Long zcard(final String key) {
		JedisAction<Jedis, Long> vJedisAction = (jedis) ->{
			return jedis.zcard(key);
		};

		return execute(vJedisAction);
	}
	//================================Ordered Set Actions  E ============================================

    // Common Actions E
}

