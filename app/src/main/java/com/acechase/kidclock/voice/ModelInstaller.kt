package com.acechase.kidclock.voice

import android.content.Context
import java.io.File

/** Copies assets/vosk-model into filesDir once per model version. Vosk needs real files. */
object ModelInstaller {
    private const val ASSET_DIR = "vosk-model"

    fun ensureInstalled(context: Context): File {
        val target = File(context.filesDir, ASSET_DIR)
        val wantVersion = context.assets.open("$ASSET_DIR/version").bufferedReader().use { it.readText().trim() }
        val haveVersion = File(target, "version").takeIf { it.exists() }?.readText()?.trim()
        if (haveVersion == wantVersion && File(target, "am/final.mdl").exists()) return target
        target.deleteRecursively()
        copyDir(context, ASSET_DIR, target)
        return target
    }

    private fun copyDir(context: Context, assetPath: String, dest: File) {
        val children = context.assets.list(assetPath) ?: emptyArray()
        if (children.isEmpty()) {
            dest.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input -> dest.outputStream().use { input.copyTo(it) } }
            return
        }
        dest.mkdirs()
        for (c in children) copyDir(context, "$assetPath/$c", File(dest, c))
    }
}
