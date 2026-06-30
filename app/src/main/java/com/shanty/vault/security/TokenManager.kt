package com.shanty.vault.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
    private val keyAlias = "shanty_vault_token_key"
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

    fun saveToken(key: String, token: String) {
        val prefs = context.getSharedPreferences("shanty_vault_tokens", Context.MODE_PRIVATE)
        val encrypted = encrypt(token)
        val storedValue = android.util.Base64.encodeToString(encrypted.ciphertext, android.util.Base64.NO_WRAP) +
                ":" +
                android.util.Base64.encodeToString(encrypted.iv, android.util.Base64.NO_WRAP)
        prefs.edit().putString(key, storedValue).apply()
    }

    fun getToken(key: String): String? {
        val prefs = context.getSharedPreferences("shanty_vault_tokens", Context.MODE_PRIVATE)
        val storedValue = prefs.getString(key, null) ?: return null
        return try {
            val parts = storedValue.split(":")
            if (parts.size != 2) return null
            val data = EncryptedData(
                ciphertext = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP),
                iv = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            )
            decrypt(data)
        } catch (e: Exception) {
            null
        }
    }

    fun clearToken(key: String) {
        val prefs = context.getSharedPreferences("shanty_vault_tokens", Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    fun clearAllTokens() {
        val prefs = context.getSharedPreferences("shanty_vault_tokens", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun encrypt(plaintext: String): EncryptedData {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        return EncryptedData(cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)), cipher.iv)
    }

    private fun decrypt(data: EncryptedData): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, data.iv))
        return String(cipher.doFinal(data.ciphertext), Charsets.UTF_8)
    }

    data class EncryptedData(
        val ciphertext: ByteArray,
        val iv: ByteArray
    )
}
