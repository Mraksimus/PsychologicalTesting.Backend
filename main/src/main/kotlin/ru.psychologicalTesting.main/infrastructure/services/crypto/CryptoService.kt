package ru.psychologicalTesting.main.infrastructure.services.crypto

interface CryptoService {
    fun encrypt(plaintext: String): String
    fun decrypt(ciphertext: String): String
    fun tryDecrypt(value: String): String
}
