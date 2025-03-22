package com.insecureshop.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "secure_prefs", Context.MODE_PRIVATE
    )
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "InsecureShopAuthKey"

    init {
        // Create the key if it doesn't exist
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun putString(key: String, value: String) {
        val encryptedData = encrypt(value)
        preferences.edit().putString(key, encryptedData).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        val encryptedData = preferences.getString(key, null) ?: return defaultValue
        return try {
            decrypt(encryptedData)
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun encrypt(input: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        
        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decrypt(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // Extract IV (first 12 bytes for GCM)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes)
    }
}