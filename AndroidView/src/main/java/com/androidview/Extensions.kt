package com.androidview

import android.content.res.Resources
import android.util.TypedValue

/**
 *  author : 破天荒
 *  date : 4/19/21
 *  description :
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Int.dp: Float
    get() = this.toFloat().dp

val Float.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

val Int.sp: Float
    get() = this.toFloat().sp