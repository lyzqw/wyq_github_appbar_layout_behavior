package com.qwlyz.behavior

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_home_list_tab.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 首页列表中间tab切换界面
 * @author lyz
 */
class HomeFeedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_list_tab, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataList = getDataList()
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = HomeAdapter(dataList)
    }

    fun getRecyclerView(): RecyclerView = recycler_view

    class HomeAdapter(dataList: MutableList<String>?) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_home, dataList) {
        override fun convert(helper: BaseViewHolder, item: String?) {
            helper.setText(R.id.tv_text, item.orEmpty()).itemView.setBackgroundColor(getColor())

        }
    }

    class HomeNewlyGameAdapter(dataList: MutableList<String>?) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_newly_game, dataList) {
        override fun convert(helper: BaseViewHolder, item: String?) {}
    }
}

fun getDataList(): MutableList<String>? {
    val dataList = ArrayList<String>()
    for (i in 1..20) {
        dataList.add(" I is item $i")
    }
    return dataList
}

fun getHeaderDataList(): MutableList<String>? {
    val dataList = ArrayList<String>()
    for (i in 1..10) {
        dataList.add(" I is Header item===> $i")
    }
    return dataList
}

fun getColor(): Int {
    val random = Random()
    return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
}
