package com.halimjr11.beacon.ui

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

/** Resolves a string resource id (Int) or returns the literal (String), so parameter tables can mix both. */
fun text(ref: Any): String {
    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    return if (ref is Int) context.getString(ref) else ref as String
}
