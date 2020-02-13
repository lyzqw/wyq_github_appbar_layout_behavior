package com.qwlyz.behavior

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fragments =
        arrayListOf(HomeFeedFragment(), HomeFeedFragment(), HomeFeedFragment(), HomeFeedFragment())
    private val titles = arrayListOf(
        "关注",
        "推荐",
        "视频",
        "攻略"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
    }

     private fun initData() {
        main_home_tab_layout.setupWithViewPager(view_pager_home)
        view_pager_home.adapter = HomeViewPager(supportFragmentManager)
        val dataList = getHeaderDataList()
        rv_home_header.layoutManager = LinearLayoutManager(this)
        val homeAdapter = HomeFeedFragment.HomeNewlyGameAdapter(dataList)
        rv_home_header.adapter = homeAdapter
        initListener()
        setTabLayoutText()
        handleUnConsumeFling()
    }

    private fun handleUnConsumeFling() {
        val behavior = getAppBarLayoutBehavior()
        behavior.setOnUnConsumeFlingListener { unConsumeVelocity ->
            fragments[view_pager_home.currentItem].getRecyclerView().fling(0, unConsumeVelocity)
        }
    }

    private fun getAppBarLayoutBehavior(): AppBarLayoutBehavior {
        val lp = app_bar.layoutParams as CoordinatorLayout.LayoutParams
        return lp.behavior as AppBarLayoutBehavior
    }

    private fun setTabLayoutText() {
        for ((index, text) in titles.withIndex()) {
            main_home_tab_layout.getTabAt(index)?.text = text
        }
    }


    /**
     * 下面列表滑动未完成就滑动上面的列表, 导致列表回弹的问题
     */
    private fun initListener() {
        rv_home_header.setOnDispatchTouchEventListener(object :
            RecommendRecyclerView.DispatchTouchEventListener {
            override fun onDispatchTouchEvent(): Boolean {
                val childRecyclerView = fragments[view_pager_home.currentItem].getRecyclerView()
                childRecyclerView.stopScroll()
                return false
            }
        })
    }

    inner class HomeViewPager(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int = fragments.size

    }
}
