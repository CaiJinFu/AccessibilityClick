package com.example.accessibilityclick

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.accessibilityclick.MainActivity.NotificationConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

/**
 * AutoClickService
 *
 * @author Jin
 * @since 2021/7/4
 */
class AutoClickService : AccessibilityService() {

  private val TAG = javaClass.canonicalName

  var mainScope: CoroutineScope? = null

  /**
   * 点击间隔
   */
  private var mInterval = -1L

  /**
   * 点击坐标xy
   */
  private var mPointX = -1f
  private var mPointY = -1f

  /**
   * 悬浮窗视图
   */
  private lateinit var mFloatingView: FloatingClickView

  companion object {
    val FLAG_ACTION = "flag_action"

    /**
     * 打开悬浮窗
     */
    val ACTION_SHOW = "action_show"

    /**
     * 自动点击事件 开启/关闭
     */
    val ACTION_PLAY = "action_play"
    val ACTION_STOP = "action_stop"

    /**
     * 关闭悬浮窗
     */
    val ACTION_CLOSE = "action_close"
  }

  override fun onCreate() {
    super.onCreate()
    startForegroundNotification()
    mFloatingView = FloatingClickView(App.mApplication)
  }

  @RequiresApi(VERSION_CODES.N) override fun onStartCommand(intent: Intent?, flags: Int,
    startId: Int
  ): Int {
    Log.i(TAG, "onStartCommand " + intent?.extras)
    intent?.apply {
      val action = getStringExtra(FLAG_ACTION)
      Log.i(TAG, "action " + action)
      when (action) {
        ACTION_SHOW  -> {
          mInterval = getLongExtra("interval", 5000)
          mFloatingView.show()
        }
        ACTION_PLAY  -> {
          mPointX = getFloatExtra("pointX", 0f)
          mPointY = getFloatExtra("pointY", 0f)
          mainScope = MainScope()
          autoClickView(mPointX, mPointY)
        }
        ACTION_STOP  -> {
          mainScope?.cancel()
        }
        ACTION_CLOSE -> {
          mFloatingView.remove()
          mainScope?.cancel()
        }
        else         -> {
          Log.e(TAG, "action error")
        }
      }
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun startForegroundNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationBuilder = NotificationCompat.Builder(this, NotificationConstants.CHANNEL_ID)
      val notification = notificationBuilder.setOngoing(true).setSmallIcon(R.mipmap.ic_launcher)
        .setCategory(Notification.CATEGORY_SERVICE).build()
      startForeground(-1, notification)
    } else {
      startForeground(-1, Notification())
    }
  }

  @RequiresApi(Build.VERSION_CODES.N) private fun autoClickView(x: Float, y: Float) {
    mainScope?.launch {
      while (true) {
        delay(mInterval)
        Log.i(TAG, "auto click x:$x  y:$y")
        val path = Path()
        path.moveTo(x, y)
        val gestureDescription = GestureDescription.Builder()
          .addStroke(GestureDescription.StrokeDescription(path, 100L, 100L)).build()
        dispatchGesture(
          gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
              super.onCompleted(gestureDescription)
              Log.i(TAG, "自动点击完成")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
              super.onCancelled(gestureDescription)
              Log.i(TAG, "自动点击取消")
            }
          }, null
        )
      }
    }
  }

  override fun onInterrupt() {
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    Log.i("TAG", "onAccessibilityEvent:$event")
  }

  private fun findFirstRecursive(parent: AccessibilityNodeInfo?, @NonNull vararg tfs: AbstractTF<Any>
  ): AccessibilityNodeInfo? {
    if (parent == null) return null
    if (tfs.size == 0) throw InvalidParameterException("AbstractTF不允许传空")
    for (i in 0 until parent.childCount) {
      val child = parent.getChild(i) ?: continue
      var isOk = true
      for (tf in tfs) {
        if (!tf.checkOk(child)) {
          isOk = false
          break
        }
      }
      if (isOk) {
        return child
      } else {
        val childChild = findFirstRecursive(child, *tfs)
        child.recycle()
        if (childChild != null) {
          return childChild
        }
      }
    }
    return null
  }

  @Nullable fun findFirst(@NonNull vararg tfs: AbstractTF<Any>): AccessibilityNodeInfo? {
    if (tfs.isEmpty()) throw InvalidParameterException("AbstractTF不允许传空")
    val rootInfo = rootInActiveWindow ?: return null
    var idTextTFCount = 0
    var idTextIndex = 0
    for (i in tfs.indices) {
      if (tfs[i] is AbstractTF.IdTextTF) {
        idTextTFCount++
        idTextIndex = i
      }
    }
    return when (idTextTFCount) {
      0    -> {
        val returnInfo: AccessibilityNodeInfo? = findFirstRecursive(rootInfo, *tfs)
        rootInfo.recycle()
        returnInfo
      }
      1    -> if (tfs.size == 1) {
        val returnInfo2: AccessibilityNodeInfo? =
          (tfs[idTextIndex] as AbstractTF.IdTextTF).findFirst(rootInfo)
        rootInfo.recycle()
        returnInfo2
      } else {
        val listIdText: MutableList<AccessibilityNodeInfo>? =
          (tfs[idTextIndex] as AbstractTF.IdTextTF).findAll(rootInfo)
        if (listIdText?.isEmpty() == true) {

        }
        var returnInfo3: AccessibilityNodeInfo? = null
        if (listIdText != null) {
          for (info in listIdText) { //遍历找到匹配的
            if (returnInfo3 == null) {
              var isOk = true
              for (tf in tfs) {
                if (!tf.checkOk(info)) {
                  isOk = false
                  break
                }
              }
              if (isOk) {
                returnInfo3 = info
              } else {
                info.recycle()
              }
            } else {
              info.recycle()
            }
          }
        }
        rootInfo.recycle()
        returnInfo3
      }
      else -> throw RuntimeException("由于时间有限，并且多了也没什么用，所以IdTF和TextTF只能有一个")
    }
    rootInfo.recycle()
    return null
  }

  fun toFullBinaryString(num: Int): String? {
    //将整数num转化为32位的二进制数
    val chs = CharArray(Integer.SIZE)
    for (i in 0 until Integer.SIZE) {
      chs[Integer.SIZE - 1 - i] = ((num shr i and 1) + '0'.toInt()).toChar()
      println(chs[Integer.SIZE - 1 - i])
    }
    return String(chs)
  }

  override fun onDestroy() {
    super.onDestroy()
    mainScope?.cancel()
  }
}