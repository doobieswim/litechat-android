package com.litechat.android.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceDailyLimitTest {
    @Test
    fun `free user can use the first exchange`() {
        assertTrue(VoiceDailyLimit.allowed(isPro = false, usedToday = 0))
    }

    @Test
    fun `free user is blocked after one exchange`() {
        assertFalse(VoiceDailyLimit.allowed(isPro = false, usedToday = 1))
    }

    @Test
    fun `pro is never blocked`() {
        assertTrue(VoiceDailyLimit.allowed(isPro = true, usedToday = 99))
    }
}

class BackupCryptoTest {
    @Test
    fun `round trip keeps the bytes`() {
        val plain = "hello chats".toByteArray()
        val blob = BackupCrypto.encrypt(plain, "secret-pass")
        assertArrayEquals(plain, BackupCrypto.decrypt(blob, "secret-pass"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `wrong password fails`() {
        val blob = BackupCrypto.encrypt("x".toByteArray(), "right")
        BackupCrypto.decrypt(blob, "wrong")
    }

    @Test
    fun `stream round trip keeps a bigger payload`() {
        val plain = ByteArray(64 * 1024) { it.toByte() }
        val out = java.io.ByteArrayOutputStream()
        BackupCrypto.encryptTo(plain.inputStream(), "pw", out)
        val restored = java.io.ByteArrayOutputStream()
        BackupCrypto.decryptTo(out.toByteArray().inputStream(), "pw", restored)
        assertArrayEquals(plain, restored.toByteArray())
    }
}
