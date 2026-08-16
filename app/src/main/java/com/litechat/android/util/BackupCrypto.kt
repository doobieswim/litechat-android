package com.litechat.android.util

import java.io.InputStream
import java.io.OutputStream
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
 *
 * Stream helpers never hold the whole file in heap (REVIEW #5).
 * Uses Cipher.update/doFinal in 8 KB chunks — CipherInputStream + GCM is flaky.
 */
object BackupCrypto {
    const val MAGIC = "BYO1"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val CHUNK = 8 * 1024

    fun encryptTo(input: InputStream, passphrase: String, output: OutputStream) {
        require(passphrase.isNotEmpty()) { "Need a password" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        output.write(MAGIC.toByteArray(Charsets.US_ASCII))
        output.write(salt)
        output.write(iv)
        pump(input, output, cipher)
    }

    fun decryptTo(input: InputStream, passphrase: String, output: OutputStream) {
        val magicBuf = ByteArray(4)
        require(readFully(input, magicBuf) == 4) { "Backup file is too small" }
        require(magicBuf.toString(Charsets.US_ASCII) == MAGIC) { "Not an encrypted BYO AI backup" }
        val salt = ByteArray(SALT_LEN)
        val iv = ByteArray(IV_LEN)
        require(readFully(input, salt) == SALT_LEN && readFully(input, iv) == IV_LEN) {
            "Backup file is too small"
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        try {
            pump(input, output, cipher)
        } catch (_: Exception) {
            throw IllegalArgumentException("Wrong password or damaged file")
        }
    }

    /** Small-payload helpers for tests. Not used on the backup path. */
    fun encrypt(plain: ByteArray, passphrase: String): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        encryptTo(plain.inputStream(), passphrase, out)
        return out.toByteArray()
    }

    fun decrypt(blob: ByteArray, passphrase: String): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        decryptTo(blob.inputStream(), passphrase, out)
        return out.toByteArray()
    }

    fun looksEncrypted(header: ByteArray): Boolean =
        header.size >= 4 && header.copyOfRange(0, 4).toString(Charsets.US_ASCII) == MAGIC

    private fun pump(input: InputStream, output: OutputStream, cipher: Cipher) {
        val buf = ByteArray(CHUNK)
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            val chunk = cipher.update(buf, 0, n)
            if (chunk != null && chunk.isNotEmpty()) output.write(chunk)
        }
        val last = cipher.doFinal()
        if (last.isNotEmpty()) output.write(last)
    }

    private fun readFully(input: InputStream, dest: ByteArray): Int {
        var off = 0
        while (off < dest.size) {
            val n = input.read(dest, off, dest.size - off)
            if (n < 0) return off
            off += n
        }
        return off
    }

    private fun key(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_BITS)
        val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(raw, "AES")
    }
}
