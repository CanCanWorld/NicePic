package com.zrq.nicepicture.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.zrq.nicepicture.adapter.CategoryAdapter
import com.zrq.nicepicture.bean.Category
import com.zrq.nicepicture.bean.CategoryX
import com.zrq.nicepicture.databinding.FragmentHomeBinding
import com.zrq.nicepicture.util.Constants.BASE_URL
import com.zrq.nicepicture.util.Util.httpGet

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    private val list = mutableListOf<CategoryX>()
    private val ids = mutableListOf<String>()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun initData() {

        loadCategory()

        mBinding.apply {
            categoryAdapter = CategoryAdapter(requireActivity(), ids)
            viewPager.adapter = categoryAdapter
        }
    }

    override fun initEvent() {
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCategory() {
        val url = "$BASE_URL?adult=true&first=1"
        httpGet(url) { success, msg ->
            if (success) {
                val result = Gson().fromJson(msg, Category::class.java)
                list.clear()
                ids.clear()
                if (result?.res?.category != null) {
                    result.res.category.forEach {
                        list.add(it)
                        ids.add(it.id)
                    }
                    categoryAdapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}
