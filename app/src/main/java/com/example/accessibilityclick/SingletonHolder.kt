package com.example.accessibilityclick

/**
 * @name AccessibilityClick
 * @class name：com.example.accessibilityclick
 * @class describe
 * @anthor 猿小蔡
 * @time 2021/5/29 19:43
 * @change
 * @chang time
 */

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
  private var creator: ((A) -> T)? = creator
  @Volatile private var instance: T? = null

  fun getInstance(arg: A): T {
    val i = instance
    if (i != null) {
      return i
    }

    return synchronized(this) {
      val i2 = instance
      if (i2 != null) {
        i2
      } else {
        val created = creator!!(arg)
        instance = created
        creator = null
        created
      }
    }
  }
}