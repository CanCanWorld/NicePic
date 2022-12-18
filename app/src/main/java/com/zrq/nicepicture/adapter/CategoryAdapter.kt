package com.zrq.nicepicture.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zrq.nicepicture.ui.CategoryFragment

class CategoryAdapter(fragmentActivity: FragmentActivity, private var list: MutableList<String>) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment(list[position])
    }
}