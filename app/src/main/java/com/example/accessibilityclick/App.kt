package com.example.accessibilityclick

import android.app.Application

/**
 * Application
 *
 * @author Jin
 * @since 2021/7/4
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