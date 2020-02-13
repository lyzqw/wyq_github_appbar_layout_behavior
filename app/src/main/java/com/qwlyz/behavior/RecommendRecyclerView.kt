package com.qwlyz.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * 扩展RecyclerView的一些生命周期
 * @author lyz
 */
class RecommendRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val ret = dispatchTouchEventListener?.onDispatchTouchEvent() ?: false
        if (ret) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    interface DispatchTouchEventListener {
        fun onDispatchTouchEvent(): Boolean
    }

    private var dispatchTouchEventListener: DispatchTouchEventListener? = null


    fun setOnDispatchTouchEventListener(dispatchTouchEventListener: DispatchTouchEventListener) {
        this.dispatchTouchEventListener = dispatchTouchEventListener
    }

}