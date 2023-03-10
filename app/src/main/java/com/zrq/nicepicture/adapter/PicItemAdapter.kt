package com.zrq.nicepicture.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.ui.PicItemFragment

class PicItemAdapter(
    fragmentActivity: FragmentActivity,
    private val list: MutableList<Vertical>,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return PicItemFragment(list[position])
    }
}