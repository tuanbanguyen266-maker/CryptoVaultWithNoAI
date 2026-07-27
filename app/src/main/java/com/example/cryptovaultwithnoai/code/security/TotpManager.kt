package com.example.cryptovault.core.security

import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.util.concurrent.TimeUnit

object TotpManager {

    fun generateSecret(): String {
        //create a secret String for the first time setup 2FA
        val rawSecret = Base32().encodeAsString(GoogleAuthenticator.createRandomSecretAsByteArray())
        //replace "=" to empty string to avoid issues with some Authenticator apps
        return rawSecret.replace("=", "").lowercase()
    }
    //validate the OTP code from users
    fun validateCode(secret: String, code: String): Boolean {
        val cleanCode = code.replace("\\s".toRegex(), "") // delete the space
        if (cleanCode.length != 6) return false //bc the OTP code from 2FA authenticator have 6 num

        return try {
            val decodedSecret = Base32().decode(secret.trim().uppercase())
            // setup the config according to RFC6238 standard
            val config = TimeBasedOneTimePasswordConfig(
                codeDigits = 6,
                timeStep = 30,
                timeStepUnit = TimeUnit.SECONDS,
                hmacAlgorithm = HmacAlgorithm.SHA1
            )
            val generator = TimeBasedOneTimePasswordGenerator(decodedSecret, config)

            //check the valid from code
            generator.isValid(cleanCode)
        } catch (e: Exception) {
            false
        }
    }

    //use for create the qr code to fastly set up
    fun getOtpAuthUri(accountName: String, issuer: String, secret: String): String {

        return "otpauth://totp/$issuer:$accountName?secret=${secret.uppercase()}&issuer=$issuer&algorithm=SHA1&digits=6&period=30"
    }
}