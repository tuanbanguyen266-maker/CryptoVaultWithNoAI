package com.example.cryptovaultwithnoai.code.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager() {

    //open the keyStore to take key
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)    // .apply == init {keyStore.load(null)}
    }

    //take and create the key for roomDatabase
    //what is roomDB? roomDB is the official data storage library developed by GG. This is an
    //"assistant" lied down between Kotlin code and SQLLite
    private fun getDatabaseSecretKey(): SecretKey{
        //check if in the AndroidKeyStore have the key for roomDB
        val existingKey = keyStore.getEntry(DB_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry

        //if yes --> take that. no --> createDatabaseKey
        return existingKey?.secretKey?: createDatabaseKey()
    }

    private fun createDatabaseKey(): SecretKey {
        //Create the key use AES Algorithm in the "AndroidKeyStore"
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        //Setup the blueprint for key
        val spec = KeyGenParameterSpec.Builder(
            DB_KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
            //purpose: use for decrypt and encrypt
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM) //use for check integrity
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // no padding
            .setUserAuthenticationRequired(false) //no required authen to take the key
            .setRandomizedEncryptionRequired(false) // no random encrypted
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    //Check and Create key for the Notes
    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true) // Self-destructs when add a new biometric to the device
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    //use for Encrypted and Decrypted the Notes
    fun getEncryptCipher(): Cipher {
        //create the crypto template accoridng to the"AES/GCM/NoPadding"
        val cipher = Cipher.getInstance(TRANSFORMATION)

        //check the secretkey by "getSecretKey()" and turn the encrypt mode on
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        return cipher //the cipher variable has 3 things:
        //first: mode: Encrypt
        //second: the key get from getSecretKey
        //third: the random IV(Intialization vector) code

    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        //create the crypto template accoridng to the"AES/GCM/NoPadding"
        val cipher = Cipher.getInstance(TRANSFORMATION)

        //setup the gcm use the old iv code and 128bit Authentication Tag
        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher
    }

    fun encryptWithPinKey(data: ByteArray, pinDerivedKey: ByteArray): Pair<ByteArray, ByteArray> {
        //use the ByteArray of PIN to make a AES key
        val secretKey = SecretKeySpec(pinDerivedKey, "AES")
        //create the crypto template accoridng to the"AES/GCM/NoPadding"
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedData = cipher.doFinal(data) // hash the raw data to trash data

        return Pair(encryptedData, cipher.iv) //return a pair about encrypted data and randomcode IV
    }

    fun decryptWithPinKey(encryptedData: ByteArray, pinDerivedKey: ByteArray, iv: ByteArray): ByteArray {

        val secretKey = SecretKeySpec(pinDerivedKey, "AES")

        val cipher = Cipher.getInstance(TRANSFORMATION)

        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(encryptedData) //decrypt
    }


    companion object {
        private const val DB_KEY_ALIAS ="cryptovault_db_hardware_key"
        private const val KEY_ALIAS = "vault_master_key_secure"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

}
