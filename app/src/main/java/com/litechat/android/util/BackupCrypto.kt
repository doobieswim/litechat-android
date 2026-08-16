package com.litechat.android.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * P-003: encrypt a chat backup with a passphrase.
 *
 * File layout (binary):
 *   BYO1 (4 bytes) + salt (16) + iv (12) + ciphertext
 *
 * Key = PBKDF2-HMAC-SHA256 (120_000 rounds, 256-bit). AES-GCM.
 * The passphrase never goes on disk. Wrong passphrase → decrypt fails.
 */
object BackupCrypto {
    private const val MAGIC = "BYO1"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256

    fun encrypt(plain: ByteArray, passphrase: String): ByteArray {
        require(passphrase.isNotEmpty()) { "Need a password" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        val out = cipher.doFinal(plain)
        return MAGIC.toByteArray(Charsets.US_ASCII) + salt + iv + out
    }

    fun decrypt(blob: ByteArray, passphrase: String): ByteArray {
        require(blob.size > 4 + SALT_LEN + IV_LEN) { "Backup file is too small" }
        val magic = blob.copyOfRange(0, 4).toString(Charsets.US_ASCII)
        require(magic == MAGIC) { "Not an encrypted BYO AI backup" }
        val salt = blob.copyOfRange(4, 4 + SALT_LEN)
        val iv = blob.copyOfRange(4 + SALT_LEN, 4 + SALT_LEN + IV_LEN)
        val data = blob.copyOfRange(4 + SALT_LEN + IV_LEN, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        return try {
            cipher.doFinal(data)
        } catch (_: Exception) {
            throw IllegalArgumentException("Wrong password or damaged file")
        }
    }

    /** Encode/decode helpers for tests that want a string view. */
    fun encryptToBase64(plain: ByteArray, passphrase: String): String =
        Base64.encodeToString(encrypt(plain, passphrase), Base64.NO_WRAP)

    fun decryptFromBase64(b64: String, passphrase: String): ByteArray =
        decrypt(Base64.decode(b64, Base64.NO_WRAP), passphrase)

    private fun key(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_BITS)
        val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(raw, "AES")
    }
}
