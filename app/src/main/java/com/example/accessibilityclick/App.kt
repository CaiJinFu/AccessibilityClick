package com.example.accessibilityclick

import android.app.Application

/**
 * @name AccessibilityClick
 * @class name：com.example.accessibilityclick
 * @class describe
 * @anthor 猿小蔡
 * @time 2021/5/29 20:07
 * @change
 * @chang time
 */
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    mApplication = this
  }

  companion object {
    lateinit var mApplication: Application
  }
}