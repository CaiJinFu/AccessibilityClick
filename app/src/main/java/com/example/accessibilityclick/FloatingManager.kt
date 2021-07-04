package com.example.accessibilityclick

import android.content.Context
import android.view.View
import android.view.WindowManager

/**
 * FloatingManager
 *
 * @author Jin
 * @since 2021/7/4
 */
class FloatingManager private constructor(context: Context) {

  /**
   * 获得WindowManager对象
   */
  private var mWindowManager: WindowManager =
    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

  companion object : SingletonHolder<FloatingManager, Context>(::FloatingManager)

  /**
   * 添加悬浮窗
   *
   * @param view 添加的view
   * @param params WindowManager
   * @return 添加是否成功，true：成功，false：失败
   */
  fun addView(view: View, params: WindowManager.LayoutParams): Boolean {
    try {
      mWindowManager.addView(view, params)
      return true
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  /**
   * 移除悬浮窗
   *
   * @param view 添加的view
   * @return 移除是否成功，true：成功，false：失败
   */
  fun removeView(view: View): Boolean {
    try {
      mWindowManager.removeView(view)
      return true
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  /**
   * 更新悬浮窗参数
   *
   * @param view 添加的view
   * @param params WindowManager
   * @return 添加是否成功，true：成功，false：失败
   */
  fun updateView(view: View, params: WindowManager.LayoutParams): Boolean {
    try {
      mWindowManager.updateViewLayout(view, params)
      return true
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }
}