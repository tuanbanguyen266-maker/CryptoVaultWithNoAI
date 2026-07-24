package com.example.cryptovaultwithnoai.code.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object PasswordHasher {

    private const val ITERATIONS = 10000 // the loop of hashing

    private const val KEY_LENGTH = 256 //the length of result: 256 bits = 32 bytes

    private const val ALGORITHM= "PBKDF2WithHmacSHA256"

    //function to hash password
    fun hashPassword(password: String, salt: ByteArray): ByteArray {

        val passwordChars = password.toCharArray()
        // create the blueprint have parameters: password, salt , iteration, length
        val spec = PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH)
        //start the factory to hash =)))
        val factory = SecretKeyFactory.getInstance(ALGORITHM)

        try {
            //Let's hash 10000 times
            return factory.generateSecret(spec).encoded
        } finally {
            //that's for  memory security. Fill '0' to the byteArray
            passwordChars.fill('0')
            spec.clearPassword()
        }
    }

    fun generateSalt(size: Int = 16): ByteArray {

        val random = SecureRandom() // use SecureRandom is more secure than Random =))
        val salt = ByteArray(size)
        random.nextBytes(salt) //Write random number from SecureRandom() to salt
        return salt
    }

    //change byte to string (base64)
    //Purpose: help easily to save the hash to database
    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

}