package com.example.ringarea.ui.util

import android.view.HapticFeedbackConstants
import android.view.View

object Haptics {
    fun performLightFeedback(view: View?, enabled: Boolean) {
        if (!enabled || view == null) return
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}
