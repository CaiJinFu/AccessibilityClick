package com.example.accessibilityclick

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

/**
 * 主界面
 *
 * @author Jin
 * @since 2021/7/4
 */
class MainActivity : AppCompatActivity() {

  val btn_accessibility: MaterialButton by lazy {
    findViewById(R.id.btn_accessibility)
  }
  val btn_floating_window: MaterialButton by lazy {
    findViewById(R.id.btn_floating_window)
  }
  val btn_show_window: MaterialButton by lazy {
    findViewById(R.id.btn_show_window)
  }
  val btn_close_window: MaterialButton by lazy {
    findViewById(R.id.btn_close_window)
  }
  val btn_test: MaterialButton by lazy {
    findViewById(R.id.btn_test)
  }
  val et_interval: AppCompatEditText by lazy {
    findViewById(R.id.et_interval)
  }

  private val TAG = javaClass::class.java.canonicalName

  object NotificationConstants {
    val CHANNEL_ID = "auto_channel_id"

    val CHANNEl_NAME = "Auto Click"

    val CHANNEL_DES = "Auto Click Service"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initNotification()
    initListener()
  }

  private fun initListener() {
    btn_accessibility.setOnLongClickListener {
      Log.i("TAG", "setOnLongClickListener: ")
      true
    }
    btn_accessibility.setOnClickListener {
      // 判断服务是否开启
      if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this, AutoClickService::class.java.name)) {
        OpenAccessibilitySettingHelper.jumpToSettingPage(this)
        return@setOnClickListener
      }
      Log.i("TAG", "开启了无障碍权限")
    }

    btn_floating_window.setOnClickListener {
      checkFloatingWindow()
    }

    btn_show_window.setOnClickListener {
      hideKeyboard()
      if (TextUtils.isEmpty(et_interval.text.toString())) {
        Snackbar.make(et_interval, "请输入间隔", Snackbar.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      showFloatingWindow()
    }

    btn_close_window.setOnClickListener {
      closeFloatWindow()
    }

    btn_test.setOnClickListener {
      Log.i(TAG, "btn_test on click")
    }

  }

  /**
   * 跳转设置顶层悬浮窗
   */
  private fun checkFloatingWindow() {
    if (Build.VERSION.SDK_INT >= 23) {
      if (Settings.canDrawOverlays(this)) {
        Toast.makeText(this, "已开启", Toast.LENGTH_SHORT).show()
      } else {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivity(intent)
      }
    }
  }

  private fun showFloatingWindow() {
    val intent = Intent(this, AutoClickService::class.java)
    intent.apply {
      putExtra(AutoClickService.FLAG_ACTION, AutoClickService.ACTION_SHOW)
      putExtra("interval", et_interval.text.toString().toLong())
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(intent)
    } else {
      startService(intent)
    }
  }

  private fun closeFloatWindow() {
    val intent = Intent(this, AutoClickService::class.java)
    intent.putExtra(AutoClickService.FLAG_ACTION, AutoClickService.ACTION_CLOSE)
    startService(intent)
  }

  private fun initNotification() {
    // 注册渠道id
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = NotificationConstants.CHANNEl_NAME
      val descriptionText = NotificationConstants.CHANNEL_DES
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(NotificationConstants.CHANNEL_ID, name, importance).apply {
        description = descriptionText
      }
      channel.enableLights(true)
      channel.lightColor = Color.GREEN // Register the channel with the system
      val notificationManager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    val intent = Intent(this, AutoClickService::class.java)
    stopService(intent)
    super.onDestroy()
  }

  /**
   * 收起输入法
   */
  private fun hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive && currentFocus != null) {
      imm.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
  }

}


