package com.zrq.nicepicture.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.zrq.nicepicture.R
import com.zrq.nicepicture.adapter.PicAdapter
import com.zrq.nicepicture.bean.Picture
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.databinding.FragmentCategoryBinding
import com.zrq.nicepicture.util.Constants.BASE_URL
import com.zrq.nicepicture.util.Util.httpGet
import java.util.*
import kotlin.random.Random

class CategoryFragment(private val id: String) : BaseFragment<FragmentCategoryBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCategoryBinding {
        return FragmentCategoryBinding.inflate(inflater, container, false)
    }

    private lateinit var picAdapter: PicAdapter
    private val list = mutableListOf<Vertical>()
    private val limit = 30  //每次加载限制的个数
    private var page = 0    //当前页数

    override fun initData() {
        picAdapter = PicAdapter(requireContext(), list) { _, pos ->
            mainModel.list.clear()
            mainModel.list.addAll(list)
            mainModel.pos = pos
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_homeFragment_to_picFragment)
        }
        mBinding.apply {
            recyclerView.adapter = picAdapter
        }
        loadPic()
    }

    override fun initEvent() {

        mBinding.apply {

            refreshLayout.setOnRefreshListener {
                page = 0
                loadPic()
            }

            refreshLayout.setOnLoadMoreListener {
                page++
                loadPic()
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPic() {
        val random = Random(Date().time).nextInt(200)
        Log.d(TAG, "loadPic: $random")
        val url = "$BASE_URL/$id/vertical?limit=$limit&skip=${random * limit}&adult=false&first=1&order=new"
        Log.d(TAG, "loadPic: $url")
        httpGet(url) { success, msg ->
            if (success) {
                val picture = Gson().fromJson(msg, Picture::class.java)
                val size = list.size
                picture?.res?.vertical?.let {
                    if (page == 0) {
                        list.clear()
                        picAdapter.notifyDataSetChanged()
                    }
                    list.addAll(it)
                }
                picAdapter.notifyItemRangeInserted(size, limit)
            } else {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            mBinding.refreshLayout.finishRefresh()
            mBinding.refreshLayout.finishLoadMore()
        }
    }

    companion object {
        const val TAG = "CategoryFragment"
    }
}
