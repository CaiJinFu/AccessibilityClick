package com.example.accessibilityclick

/**
 * @name AccessibilityClick
 * @class name：com.example.accessibilityclick
 * @class describe
 * @anthor 猿小蔡
 * @time 2021/5/29 19:42
 * @change
 * @chang time
 */

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.accessibilityclick.MainActivity.NotificationConstants
import kotlinx.coroutines.*

class AutoClickService : AccessibilityService() {

  private val TAG = javaClass.canonicalName

  var mainScope: CoroutineScope? = null

  //点击间隔
  private var mInterval = -1L

  //点击坐标xy
  private var mPointX = -1f
  private var mPointY = -1f

  //悬浮窗视图
  private lateinit var mFloatingView: FloatingClickView

  companion object {
    val FLAG_ACTION = "flag_action"

    //打开悬浮窗
    val ACTION_SHOW = "action_show"

    //自动点击事件 开启/关闭
    val ACTION_PLAY = "action_play"
    val ACTION_STOP = "action_stop"

    //关闭悬浮窗
    val ACTION_CLOSE = "action_close"

  }

  override fun onCreate() {
    super.onCreate()
    startForegroundNotification()
    mFloatingView = FloatingClickView(App.mApplication)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "onStartCommand " + intent?.extras)
    intent?.apply {
      val action = getStringExtra(FLAG_ACTION)
      Log.d(TAG, "action " + action)
      when (action) {

        ACTION_SHOW -> {
          mInterval = getLongExtra("interval", 5000)
          mFloatingView.show()
        }
        ACTION_PLAY -> {
          mPointX = getFloatExtra("pointX", 0f)
          mPointY = getFloatExtra("pointY", 0f)

          mainScope = MainScope()
          autoClickView(mPointX, mPointY)
        }
        ACTION_STOP -> {
          mainScope?.cancel()
        }
        ACTION_CLOSE -> {
          mFloatingView.remove()
          mainScope?.cancel()
        }
        else -> {
          Log.e(TAG, "action error")
        }
      }
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun startForegroundNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationBuilder =
        NotificationCompat.Builder(this, NotificationConstants.CHANNEL_ID)
      val notification = notificationBuilder.setOngoing(true)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setCategory(Notification.CATEGORY_SERVICE)
        .build()
      startForeground(-1, notification)

    } else {
      startForeground(-1, Notification())
    }
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun autoClickView(x: Float, y: Float) {

    mainScope?.launch {
      while (true) {
        delay(mInterval)
        Log.d(TAG, "auto click x:$x  y:$y")
        val path = Path()
        path.moveTo(x, y)
        val gestureDescription = GestureDescription.Builder()
          .addStroke(GestureDescription.StrokeDescription(path, 100L, 100L))
          .build()
        dispatchGesture(
          gestureDescription,
          object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
              super.onCompleted(gestureDescription)
              Log.d(TAG, "自动点击完成")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
              super.onCancelled(gestureDescription)
              Log.d(TAG, "自动点击取消")
            }
          },
          null
        )
      }
    }
  }

  override fun onInterrupt() {
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
  }

  override fun onDestroy() {
    super.onDestroy()
    mainScope?.cancel()
  }
}