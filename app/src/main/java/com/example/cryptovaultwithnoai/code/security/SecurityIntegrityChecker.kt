package com.example.cryptovaultwithnoai.code.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

//RASP (Runtime Application Self-Protection) for the CryptoVault
object SecurityIntegrityChecker {

    //use for Anti-tampering
    private fun isSignatureValid(context: Context): Boolean {
        return try {

            // the code give a question that if your sdk android phone(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            //Android 9(P-Pie, API 28)
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                //true(bc > android 9) --> take all the lasteset history key
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else { // no(< android 9) --> take all the packageInfo

                @Suppress("DEPRECATION") //Annotation to shut down the old android version warning

                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            //take the signature
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION") //Annotation to shut down the old android version warning
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return false //if signature is null or empty return false to out the app

            //take the F0 signatures and change to byteArray
            val rawSignature = signatures[0].toByteArray()
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val byteSignature = md.digest(rawSignature)

            // change into HEX and uppercase
            val currentSignatureHex = byteSignature.joinToString("") {
                String.format("%02X", it)
            }

            //original hardcore signature
            val productionSignatureHex = "809EE25B3C32443A460B8E9851DE89512DEDEF8115E7BE0C8AF7FFC825CB564C"

            //comparision
            java.security.MessageDigest.isEqual(
                currentSignatureHex.toByteArray(Charsets.UTF_8),
                productionSignatureHex.toByteArray(Charsets.UTF_8)
            )

        } catch (e: Exception) {
            //if have error, default lock app
            false
        }
    }



}