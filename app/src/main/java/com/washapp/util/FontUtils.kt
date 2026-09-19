package com.washapp.util

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.washapp.R

fun applyPoppinsRecursively(view: View) {
    if (view is TextView) {
        val typeface = ResourcesCompat.getFont(view.context, R.font.poppins_medium)
        view.typeface = typeface
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            applyPoppinsRecursively(view.getChildAt(i))
        }
    }
}
