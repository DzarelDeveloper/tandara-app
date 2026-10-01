package id.tandara.parent

import id.tandara.parent.core.network.ServerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerConfigTest {

    @Test
    fun `validateHost returns true for valid IPv4 addresses`() {
        assertTrue(ServerConfig.validateHost("192.168.110.101"))
        assertTrue(ServerConfig.validateHost("192.168.1.20"))
        assertTrue(ServerConfig.validateHost("10.0.0.5"))
        assertTrue(ServerConfig.validateHost("172.16.0.1"))
        assertTrue(ServerConfig.validateHost("172.31.255.254"))
        assertTrue(ServerConfig.validateHost("0.0.0.0"))
        assertTrue(ServerConfig.validateHost("255.255.255.255"))
        assertTrue(ServerConfig.validateHost("localhost"))
        assertTrue(ServerConfig.validateHost("LocalHost"))
    }

    @Test
    fun `validateHost returns false for invalid IPv4 addresses`() {
        assertFalse(ServerConfig.validateHost("999.999.1.1"))
        assertFalse(ServerConfig.validateHost("192.168"))
        assertFalse(ServerConfig.validateHost("abc"))
        assertFalse(ServerConfig.validateHost(""))
        assertFalse(ServerConfig.validateHost("   "))
        assertFalse(ServerConfig.validateHost("256.0.0.1"))
        assertFalse(ServerConfig.validateHost("192.168.1.1.1"))
        assertFalse(ServerConfig.validateHost("192.168.1"))
        assertFalse(ServerConfig.validateHost(".192.168.1.1"))
        assertFalse(ServerConfig.validateHost("192.168.1.1."))
        assertFalse(ServerConfig.validateHost("-1.1.1.1"))
    }

    @Test
    fun `validatePort returns parsed int for valid port strings`() {
        assertEquals(8000, ServerConfig.validatePort("8000"))
        assertEquals(1, ServerConfig.validatePort("1"))
        assertEquals(65535, ServerConfig.validatePort("65535"))
        assertEquals(8080, ServerConfig.validatePort(" 8080 "))
        assertEquals(ServerConfig.DEFAULT_PORT, ServerConfig.validatePort(""))
    }

    @Test
    fun `validatePort returns null for invalid port strings`() {
        assertNull(ServerConfig.validatePort("0"))
        assertNull(ServerConfig.validatePort("65536"))
        assertNull(ServerConfig.validatePort("abc"))
        assertNull(ServerConfig.validatePort("-1"))
        assertNull(ServerConfig.validatePort("80 00"))
        assertNull(ServerConfig.validatePort("80a00"))
    }

    @Test
    fun `whitespace and case normalization`() {
        val host = "  192.168.110.101  "
        assertTrue(ServerConfig.validateHost(host.trim()))
        val port = "  8000  "
        assertEquals(8000, ServerConfig.validatePort(port))
        val config = ServerConfig(host.trim(), ServerConfig.validatePort(port)!!)
        assertEquals("192.168.110.101", config.host)
        assertEquals(8000, config.port)
    }

    @Test
    fun `resolvedHttpBaseUrl constructs correct URL`() {
        val config = ServerConfig("192.168.1.20", 8000)
        assertEquals("http://192.168.1.20:8000/", config.resolvedHttpBaseUrl)
    }

    @Test
    fun `resolvedWebSocketBaseUrl constructs correct URL`() {
        val config = ServerConfig("192.168.1.20", 8000)
        assertEquals("ws://192.168.1.20:8000/", config.resolvedWebSocketBaseUrl)
    }

    @Test
    fun `normalizedKey produces stable host-port identity`() {
        val a = ServerConfig("192.168.110.101", 8000)
        val b = ServerConfig("192.168.110.101", 8000)
        val c = ServerConfig("192.168.110.102", 8000)
        val d = ServerConfig("192.168.110.101", 9000)
        assertEquals(a.normalizedKey(), b.normalizedKey())
        assertFalse(a.normalizedKey() == c.normalizedKey())
        assertFalse(a.normalizedKey() == d.normalizedKey())
    }

    @Test
    fun `default port constant is 8000`() {
        assertEquals(8000, ServerConfig.DEFAULT_PORT)
    }

    @Test
    fun `fromBuildConfigFallback yields a usable config`() {
        val fallback = ServerConfig.fromBuildConfigFallback()
        assertNotNull(fallback)
        assertTrue(fallback.host.isNotBlank())
        assertTrue(fallback.port in 1..65535)
        assertTrue(fallback.resolvedHttpBaseUrl.startsWith("http"))
        assertTrue(fallback.resolvedWebSocketBaseUrl.startsWith("ws"))
    }
}
