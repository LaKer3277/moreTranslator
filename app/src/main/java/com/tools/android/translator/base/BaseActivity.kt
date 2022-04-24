package com.tools.android.translator.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Created on 2022/4/20
 * Describe:
 */
open class BaseActivity: AppCompatActivity() {

    private var _isPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenAdapt()
        _isPause = false
    }

    private fun screenAdapt() {
        resources.displayMetrics.apply {
            val finalHeight = heightPixels / 750f
            density = finalHeight
            scaledDensity = finalHeight
            densityDpi = (160 * finalHeight).toInt()
        }
    }

    override fun onStart() {
        super.onStart()
        _isPause = false
    }

    override fun onResume() {
        super.onResume()
        _isPause = false
    }

    override fun onPause() {
        super.onPause()
        _isPause = true
    }

    override fun onStop() {
        super.onStop()
        _isPause = true
    }

    open fun isPaused(): Boolean {
        return _isPause
    }

    fun showKeyboard(view: View): Boolean {
        val imm = view.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            view.requestFocus()
            return imm.showSoftInput(view, 0)
        }
        return false
    }

    fun hideKeyboard(view: View): Boolean {
        val imm = view.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        return imm?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
    }

    fun toggleSoftInput(view: View) {
        val imm = view.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(0, 0)
    }
}