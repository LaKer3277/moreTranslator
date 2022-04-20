package com.tools.android.translator.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

/**
 * Created on 2022/4/20
 * Describe:
 */
abstract class BaseBindingActivity<T: ViewBinding>: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = obtainBinding()
        setContentView(binding.root)
    }

    protected lateinit var binding: T
    abstract fun obtainBinding(): T
}