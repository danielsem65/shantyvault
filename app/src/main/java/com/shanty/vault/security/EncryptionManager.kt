package com.shanty.vault.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class EncryptionManager() {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
    private val keyAlias = "shanty_vault_master_key"
    private val transformation = "AES/GCM/NoPadding"

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        keyGenerator.generateKey()
    }

    private fun getKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(plaintext: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedData(ciphertext, iv)
    }

    fun decrypt(data: EncryptedData): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, data.iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(data.ciphertext)
    }

    fun encryptString(plaintext: String): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encrypted.ciphertext, android.util.Base64.NO_WRAP) +
                ":" +
                android.util.Base64.encodeToString(encrypted.iv, android.util.Base64.NO_WRAP)
    }

    fun decryptString(ciphertext: String): String {
        val parts = ciphertext.split(":")
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted string format")
        val data = EncryptedData(
            ciphertext = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP),
            iv = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
        )
        return String(decrypt(data), Charsets.UTF_8)
    }

    fun encryptFile(input: ByteArray): ByteArray {
        val encrypted = encrypt(input)
        return encrypted.iv + encrypted.ciphertext
    }

    fun decryptFile(input: ByteArray): ByteArray {
        val iv = input.copyOfRange(0, 12)
        val ciphertext = input.copyOfRange(12, input.size)
        return decrypt(EncryptedData(ciphertext, iv))
    }

    data class EncryptedData(
        val ciphertext: ByteArray,
        val iv: ByteArray
    )
}
