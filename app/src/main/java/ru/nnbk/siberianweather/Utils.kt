package ru.nnbk.siberianweather

import android.app.AlertDialog
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun Context.showConfirmDialog(message: String): Boolean =
    suspendCancellableCoroutine { cont ->
        val dialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Да") { _, _ -> cont.resume(true) }
            .setNegativeButton("Нет") { _, _ -> cont.resume(false) }
            .setOnCancelListener { cont.resume(false) }
            .create()

        dialog.show()

        cont.invokeOnCancellation { dialog.dismiss() }
    }


fun getPortStatus(str: String, mask: String): Char {
    val index = str.indexOf(mask)
    if (index == -1) return ' '
    var pos = index - 1
    while (pos >= 0 && str[pos] == ' ') {
        pos--
    }
    return if (pos >= 0) str[pos] else ' '
}