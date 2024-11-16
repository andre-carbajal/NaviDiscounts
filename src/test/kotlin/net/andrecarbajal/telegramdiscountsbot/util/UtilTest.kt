package net.andrecarbajal.telegramdiscountsbot.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.core.env.Environment

class UtilTest {
    @Test
    fun isDevelopment_withDevProfile_returnsTrue() {
        val environment = mock(Environment::class.java)
        `when`(environment.activeProfiles).thenReturn(arrayOf("dev"))

        assertTrue(Util.isDevelopment(environment))
    }

    @Test
    fun isDevelopment_withoutDevProfile_returnsFalse() {
        val environment = mock(Environment::class.java)
        `when`(environment.activeProfiles).thenReturn(arrayOf("prod"))

        assertFalse(Util.isDevelopment(environment))
    }

    @Test
    fun isNotValidUrl_withSpace_returnsTrue() {
        val spaceUrl = " "
        assertTrue(Util.isNotValidUrl(spaceUrl))
    }

    @Test
    fun isNotValidUrl_withInvalidUrl_returnsTrue() {
        val invalidUrl = "htp://invalid-url"
        assertTrue(Util.isNotValidUrl(invalidUrl))
    }

    @Test
    fun isNotValidUrl_withUrlMissingProtocol_returnsTrue() {
        val validUrl = "www.google.com"
        assertTrue(Util.isNotValidUrl(validUrl))
    }

    @Test
    fun isNotValidUrl_withUrlContainingInvalidHost_returnsTrue() {
        val urlContainingInvalidHost = "https://invalid..com"
        assertTrue(Util.isNotValidUrl(urlContainingInvalidHost))
    }

    @Test
    fun isNotValidUrl_withUrlMissingHost_returnsTrue() {
        val urlMissingHost = "https://"
        assertTrue(Util.isNotValidUrl(urlMissingHost))
    }

    @Test
    fun isNotValidUrl_withUrlIsMifarma_returnsFalse() {
        val urlContainingValidHost = "https://www.mifarma.com.pe/producto/test"
        assertFalse(Util.isNotValidUrl(urlContainingValidHost))
    }

    @Test
    fun isNotValidUrl_withUrlIsInkafarma_returnsFalse() {
        val urlContainingValidHost = "https://inkafarma.pe/producto/test"
        assertFalse(Util.isNotValidUrl(urlContainingValidHost))
    }
}