package com.arbazmateen.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.Serializable

fun Activity.toastLong(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Activity.toastShort(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Activity.requestFocus(view: View) {
    if (view.requestFocus()) {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}

fun Activity.hideKeyboard(view: View) {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.getIntValue(key: String): Int {
    if(intent != null && intent.extras != null) {
        return intent.extras?.getInt(key) ?: 0
    }
    return 0
}

fun Activity.getLongValue(key: String): Long {
    if(intent != null && intent.extras != null) {
        return intent.extras?.getLong(key) ?: 0L
    }
    return 0L
}

fun Activity.getStringValue(key: String): String {
    if(intent != null && intent.extras != null) {
        return intent.extras?.getString(key) ?: ""
    }
    return ""
}

inline fun <reified T : Serializable> Activity.getSerializable(key: String): T? {
    if(intent != null && intent.extras != null) {
        try {
            return intent.extras?.getSerializable(key) as T
        } catch (e: Exception) {
            Log.e("ERROR", e.message)
        }
    }
    return null
}

fun Activity.copyToClipboard(text: String, label: String = "Copy", msg: String = "Copy to clipboard") {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip = ClipData.newPlainText(label, text)
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
