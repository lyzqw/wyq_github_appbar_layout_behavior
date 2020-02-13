package com.qwlyz.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Field;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * 处理滑动回弹的一些问题
 *
 * @author liuyuzhe
 */
public class AppBarLayoutBehavior extends AppBarLayout.Behavior {
    private static final int                                  TYPE_FLING = 1;
    private final        FlingHelper                          mFlingHelper;
    private final        GestureDetector                      gestureDetector;
    private final        int                                  mMinimumFlingVelocity;
    private              boolean                              isFlinging;
    private              boolean                              shouldBlockNestedScroll;
    private              float                                mVelocityY;
    private              AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
    private              int                                  moveOffset;
    private              OnUnConsumeFlingListener             mConsumeFlingListener;
    private              int                                  mTotalScrollRange;
    private              OnAppBarExpandedListener             mOnAppBarExpandedListener;
    private              boolean                              mIsExpand;

    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mFlingHelper = new FlingHelper(context);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                mVelocityY = velocityY;
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, final AppBarLayout child, MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        if (mOnOffsetChangedListener == null && child != null) {
            handleUnConsumeFlingEvent(child);
        }
        return super.onTouchEvent(parent, child, ev);
    }

    private void handleUnConsumeFlingEvent(final AppBarLayout child) {
        mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (moveOffset == 0) {
                    moveOffset = verticalOffset;
                }
                if (mTotalScrollRange == 0) {
                    mTotalScrollRange = child.getTotalScrollRange();
                }
                boolean isExpand = -mTotalScrollRange == verticalOffset;
                if (mOnAppBarExpandedListener != null && isExpand != mIsExpand) {
                    mIsExpand = isExpand;
                    mOnAppBarExpandedListener.appBarExpanded(mIsExpand);
                }
                //滑动到底部了并且是惯性滑动, 把未消费的惯性滑动交给下面列表继续滑动
                if (-mTotalScrollRange == verticalOffset && Math.abs(mVelocityY) > 0 && Math.abs(mVelocityY) > mMinimumFlingVelocity) {
                    float consumeOffset = Math.abs(verticalOffset) - Math.abs(moveOffset);
                    double flingDistance = mFlingHelper.getSplineFlingDistance((int) mVelocityY);
                    mVelocityY = 0;
                    double unConsumeOffset = flingDistance - consumeOffset;
                    if (mConsumeFlingListener != null && unConsumeOffset > 0) {
                        mConsumeFlingListener.unConsumeFling(mFlingHelper.getVelocityByDistance(unConsumeOffset));
                    }
                }
            }
        };
        child.addOnOffsetChangedListener(mOnOffsetChangedListener);
    }


    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        shouldBlockNestedScroll = isFlinging;
        //手指触摸屏幕的时候停止fling事件
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            stopAppbarLayoutFling(child);
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            moveOffset = 0;
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    /**
     * 反射获取私有的flingRunnable 属性，考虑support 28以后变量名修改的问题
     *
     * @return Field
     */
    private Field getFlingRunnableField() throws NoSuchFieldException {
        try {
            // support design 27及以下版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("mFlingRunnable");
        } catch (NoSuchFieldException e) {
            // 可能是28及以上版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("flingRunnable");
        }
    }

    /**
     * 反射获取私有的scroller 属性，考虑support 28以后变量名修改的问题
     *
     * @return Field
     */
    private Field getScrollerField() throws NoSuchFieldException {
        try {
            // support design 27及以下版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("mScroller");
        } catch (NoSuchFieldException e) {
            // 可能是28及以上版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("scroller");
        }
    }

    /**
     * 停止appbarLayout的fling事件
     *
     * @param appBarLayout
     */
    private void stopAppbarLayoutFling(AppBarLayout appBarLayout) {
        //通过反射拿到HeaderBehavior中的flingRunnable变量
        try {
            Field flingRunnableField = getFlingRunnableField();
            Field scrollerField = getScrollerField();
            flingRunnableField.setAccessible(true);
            scrollerField.setAccessible(true);

            Runnable flingRunnable = (Runnable) flingRunnableField.get(this);
            OverScroller overScroller = (OverScroller) scrollerField.get(this);
            if (flingRunnable != null) {
                appBarLayout.removeCallbacks(flingRunnable);
                flingRunnableField.set(this, null);
            }
            if (overScroller != null && !overScroller.isFinished()) {
                overScroller.abortAnimation();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        stopAppbarLayoutFling(child);
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        //type返回1时，表示当前target处于非touch的滑动，
        //该bug的引起是因为appbar在滑动时，CoordinatorLayout内的实现NestedScrollingChild2接口的滑动子类还未结束其自身的fling
        //所以这里监听子类的非touch时的滑动，然后block掉滑动事件传递给AppBarLayout
        if (type == TYPE_FLING) {
            isFlinging = true;
        }
        if (!shouldBlockNestedScroll) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (!shouldBlockNestedScroll) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        }
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        isFlinging = false;
        shouldBlockNestedScroll = false;
    }

    public void setOnUnConsumeFlingListener(OnUnConsumeFlingListener listener) {
        mConsumeFlingListener = listener;
    }

    public interface OnUnConsumeFlingListener {
        void unConsumeFling(int unConsumeVelocity);
    }

    public interface OnAppBarExpandedListener {
        void appBarExpanded(boolean expand);
    }

    public void setOnAppBarExpandedListener(OnAppBarExpandedListener listener) {
        mOnAppBarExpandedListener = listener;
    }
}