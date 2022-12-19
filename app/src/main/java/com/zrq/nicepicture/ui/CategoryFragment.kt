package com.zrq.nicepicture.ui

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.zrq.nicepicture.R
import com.zrq.nicepicture.adapter.PicAdapter
import com.zrq.nicepicture.bean.Picture
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.databinding.FragmentCategoryBinding
import com.zrq.nicepicture.util.Constants.BASE_URL
import com.zrq.nicepicture.util.Util.httpGet
import com.zrq.nicepicture.util.Util.saveImage
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
    private var downloadBtn: Button? = null

    override fun initData() {
        picAdapter = PicAdapter(requireContext(), list,
            { _, pos ->
                mainModel.list.clear()
                mainModel.list.addAll(list)
                mainModel.pos = pos
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(R.id.action_homeFragment_to_picFragment)
            },
            { view, position ->
                val location = IntArray(2)
                view.getLocationInWindow(location)
                location[0] += view.width / 2
                location[1] += view.height / 2
                if (downloadBtn != null) {
                    mBinding.root.removeView(downloadBtn)
                }
                downloadBtn = newBtn(location[0], location[1])
                downloadBtn?.let { btn ->
                    btn.setOnClickListener {
                        saveImage(requireContext(), list[position].preview) { success, msg ->
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    btn.text = "已下载"
                                    btn.isEnabled = false
                                }
                            }
                        }
                    }
                }
            }
        )
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

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    mBinding.root.removeView(downloadBtn)
                }
            })

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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun newBtn(x: Int, y: Int): Button {
        val btn = MaterialButton(requireContext())
        val width = 240
        val height = 120
        btn.layoutParams = RelativeLayout.LayoutParams(width, height)
        btn.cornerRadius = 20
        btn.background = resources.getDrawable(R.drawable.shape_btn_download)
        btn.text = "下载"
        btn.x = x.toFloat() - width / 2
        btn.y = y.toFloat() - height / 2
        mBinding.root.addView(btn)
        return btn
    }

    companion object {
        const val TAG = "CategoryFragment"
    }
}
