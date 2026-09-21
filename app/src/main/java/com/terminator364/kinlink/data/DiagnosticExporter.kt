package com.terminator364.kinlink.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class DiagnosticExporter(private val context: Context) {
    /**
     * Creates a local, user-initiated share sheet. It performs no network request,
     * background upload or automatic recipient selection.
     */
    fun share(summary: DiagnosticSummary) {
        val directory = File(context.cacheDir, "diagnostics").apply { mkdirs() }
        val bundleFile = File(directory, "KINLINK_DIAGNOSTIC.zip")
        DiagnosticBundleBuilder.write(bundleFile, summary)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.diagnostics",
            bundleFile
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Partager le diagnostic KINLINK"))
    }
}
