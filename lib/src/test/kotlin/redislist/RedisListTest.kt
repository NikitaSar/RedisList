package redislist

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.JedisPool
import kotlin.test.*

class RedisListTest {

    private val listKey = "myList"

    private val redisContainer = GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
        .withExposedPorts(6379)

    private lateinit var jedisPool: JedisPool

    init {
        redisContainer.start()
        val host = redisContainer.host
        val port: Int  = redisContainer.getMappedPort(6379)
        jedisPool = JedisPool(host, port)

    }

    @BeforeTest
    fun initTest() {
        jedisPool.resource.use { jedis ->
            jedis.del(listKey)
        }
    }

    @Test
    fun listCount() {
        jedisPool.resource.use { jedis ->
            jedis.lpush(listKey, "one")
            jedis.lpush(listKey, "two")
            jedis.lpush(listKey, "three")

            val list: List<String> = RedisList(listKey, jedis)
            assertEquals(3, list.size)
        }
    }

    @Test
    fun listGet() {
        jedisPool.resource.use { jedis ->
            jedis.rpush(listKey, "one")
            jedis.rpush(listKey, "two")
            jedis.rpush(listKey, "three")

            val list: List<String> = RedisList(listKey, jedis)
            assertEquals("two", list[1])
        }
    }

    @Test
    fun isEmpty() {
        jedisPool.resource.use {
            val list: List<String> = RedisList(listKey, it)
            assertTrue (list.isEmpty())
        }
    }

    @Test
    fun iteratorTest() {
        jedisPool.resource.use {jedis ->
            val expected = listOf("one", "two", "three")
            expected.forEach { jedis.rpush(listKey, it) }
            val list: List<String> = RedisList(listKey, jedis)
            val iterator = expected.iterator();
            list.forEach {
                assertEquals(iterator.next(), it)
            }
        }
    }
}