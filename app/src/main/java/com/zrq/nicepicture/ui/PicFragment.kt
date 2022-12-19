package com.zrq.nicepicture.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zrq.nicepicture.adapter.PicItemAdapter
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.databinding.FragmentPicBinding

class PicFragment : BaseFragment<FragmentPicBinding>() {

    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicBinding {
        return FragmentPicBinding.inflate(inflater, container, false)
    }

    private lateinit var adapter: PicItemAdapter
    private val list = mutableListOf<Vertical>()

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        adapter = PicItemAdapter(requireActivity(), list)
        mBinding.apply {
            viewPager.adapter = adapter
        }
        list.clear()
        mainModel.list.forEach {
            list.add(it)
        }
        mBinding.viewPager.setCurrentItem(mainModel.pos, false)
        adapter.notifyDataSetChanged()
    }

    override fun initEvent() {
    }

}
