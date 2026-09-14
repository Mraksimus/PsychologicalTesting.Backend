package ru.psychologicalTesting.main.infrastructure.services.crypto

import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.config.crypto.CryptoConfig
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_LENGTH = 12
private const val TAG_LENGTH_BITS = 128
private const val KEY_LENGTH_BYTES = 32
private const val ENCRYPTED_PREFIX = "enc:v1:"

@Single
class AesGcmCryptoService(
    config: CryptoConfig,
) : CryptoService {

    private val keySpec: SecretKeySpec
    private val random = SecureRandom()

    init {
        val decoded = Base64.getDecoder().decode(config.key)
        require(decoded.size == KEY_LENGTH_BYTES) {
            "crypto.key must decode to $KEY_LENGTH_BYTES bytes, got ${decoded.size}"
        }
        keySpec = SecretKeySpec(decoded, ALGORITHM)
    }

    override fun encrypt(plaintext: String): String {
        val iv = ByteArray(IV_LENGTH).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + ct
        return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined)
    }

    override fun decrypt(ciphertext: String): String {
        require(ciphertext.startsWith(ENCRYPTED_PREFIX)) { "not an encrypted payload" }
        val payload = Base64.getDecoder().decode(ciphertext.removePrefix(ENCRYPTED_PREFIX))
        val iv = payload.copyOfRange(0, IV_LENGTH)
        val ct = payload.copyOfRange(IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ct).toString(Charsets.UTF_8)
    }

    override fun tryDecrypt(value: String): String {
        if (!value.startsWith(ENCRYPTED_PREFIX)) return value
        return runCatching { decrypt(value) }.getOrDefault(value)
    }
}
