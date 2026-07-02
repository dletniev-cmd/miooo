package com.letify.app

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.letify.app.ui.icons.SolarIconLoader
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Application entry point.
 *
 * Beyond the usual lifecycle hook, this class installs a process-wide
 * `Thread.UncaughtExceptionHandler` that serialises every fatal stack
 * trace to disk. Because a launch-time crash never gives the UI a chance
 * to show a "copy logs" dialog, the report is written to places the user
 * can actually reach from the phone with no computer and no permissions:
 *
 *   1. Public "Downloads" as `логи.txt` (MediaStore, API 29+).
 *   2. The app-specific external dir `Android/data/<pkg>/files/логи.txt`
 *      (works on every API level, no permission needed).
 *   3. The internal cache `last_crash.txt` (kept for the in-app dialog on
 *      a non-fatal-launch crash).
 *
 * After capture we still call through to the platform-default handler so
 * the OS gets to do its own crash reporting (and the process actually dies).
 *
 * onCreate also kicks off the background icon prewarm so the work starts
 * overlapping with MainActivity's window inflation.
 */
class LetifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        SolarIconLoader.prewarmAll(this)
    }
}

object CrashReporter {

    private const val TAG = "LetifyCrash"
    private const val CRASH_FILE = "last_crash.txt"
    private const val USER_LOG_NAME = "логи.txt"
    private const val MAX_RETAINED_BYTES = 256 * 1024

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeReport(appContext, thread, throwable)
            } catch (t: Throwable) {
                Log.e(TAG, "failed to write crash report", t)
            }
            // Defer to the platform handler so Android still surfaces the
            // crash dialog / kills the process.
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun crashFile(context: Context): File = File(context.cacheDir, CRASH_FILE)

    private fun buildReport(context: Context, thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).use { pw ->
            pw.println("Анкетница — лог вылета")
            pw.println(
                "Time: " +
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date()),
            )
            pw.println("Package: ${context.packageName}")
            val version = runCatching {
                val pi = context.packageManager.getPackageInfo(context.packageName, 0)
                "${pi.versionName} (${pi.longVersionCodeCompat()})"
            }.getOrNull()
            if (version != null) pw.println("Version: $version")
            pw.println("Thread: ${thread.name}")
            pw.println("Android: ${Build.VERSION.RELEASE} (sdk ${Build.VERSION.SDK_INT})")
            pw.println("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            pw.println()
            // The cause chain — printStackTrace already walks Caused-by links.
            throwable.printStackTrace(pw)
        }
        val text = sw.toString()
        return if (text.length > MAX_RETAINED_BYTES) text.take(MAX_RETAINED_BYTES) else text
    }

    private fun writeReport(context: Context, thread: Thread, throwable: Throwable) {
        val text = buildReport(context, thread, throwable)
        // 1) internal cache (for the in-app "copy logs" dialog).
        runCatching { crashFile(context).writeText(text, Charsets.UTF_8) }
        // 2) app-specific external dir — no permission, reachable via a file
        //    manager at Android/data/<pkg>/files/логи.txt.
        runCatching {
            context.getExternalFilesDir(null)?.let { dir ->
                File(dir, USER_LOG_NAME).writeText(text, Charsets.UTF_8)
            }
        }
        // 3) public Downloads via MediaStore (API 29+) — easiest for the user
        //    to find and share. Best-effort.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { writeToDownloads(context, text) }
        }
    }

    private fun writeToDownloads(context: Context, text: String) {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        // Remove any prior лог so a single, current file is left behind.
        runCatching {
            resolver.query(
                collection,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                arrayOf(USER_LOG_NAME),
                null,
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    runCatching { resolver.delete(uri, null, null) }
                }
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, USER_LOG_NAME)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: return
        resolver.openOutputStream(uri)?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
        }
        val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
        runCatching { resolver.update(uri, done, null, null) }
    }

    /**
     * Returns the previously-captured crash report, or `null` if the previous
     * session terminated cleanly.
     */
    fun read(context: Context): String? {
        val f = crashFile(context.applicationContext)
        return if (f.exists() && f.length() > 0L) f.readText() else null
    }

    fun consume(context: Context) {
        crashFile(context.applicationContext).delete()
    }
}

/** versionCode across API levels (longVersionCode added in API 28). */
private fun android.content.pm.PackageInfo.longVersionCodeCompat(): Long =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode
    else @Suppress("DEPRECATION") versionCode.toLong()