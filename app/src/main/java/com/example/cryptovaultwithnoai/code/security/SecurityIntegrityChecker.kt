package com.example.cryptovaultwithnoai.code.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.File
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

    private fun isEmulator(): Boolean {
        //Check atribute Build
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        val fingerprint = Build.FINGERPRINT.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val device = Build.DEVICE.lowercase()
        val brand = Build.BRAND.lowercase()
        val board = Build.BOARD.lowercase()

        var isEmu = fingerprint.contains("generic")
                || fingerprint.contains("unknown")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk built for x86")
                || model.contains("sdk_gphone")
                || manufacturer.contains("genymotion")
                || product.contains("sdk_google")
                || product.contains("google_sdk")
                || product.contains("vbox86p")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || brand.contains("generic")
                || device.contains("generic")
                || board.contains("goldfish")

        if (isEmu) return true

        //check qemu driver
        val knownEmuFiles = arrayOf(
            "/dev/qemu_pipe",
            "/dev/socket/qemud",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )
        for (filePath in knownEmuFiles) {
            if (File(filePath).exists()) {
                android.util.Log.d("SecurityIntegrity", "Emulator detected via file: $filePath")
                return true
            }
        }

        //check translation layer
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText()
                if (content.contains("libhoudini") || content.contains("libndk_translation")) {
                    android.util.Log.d("SecurityIntegrity", "Emulator detected via Translation Layer (Houdini/NDK)")
                    return true
                }
            }
        } catch (e: Exception) {

        }


        return false
    }



}