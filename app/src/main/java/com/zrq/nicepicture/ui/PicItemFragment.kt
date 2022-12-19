package com.zrq.nicepicture.ui

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zrq.nicepicture.R
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.databinding.FragmentPicItemBinding
import com.zrq.nicepicture.util.Util.saveImage

class PicItemFragment(private val pic: Vertical) : BaseFragment<FragmentPicItemBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicItemBinding {
        return FragmentPicItemBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        mBinding.apply {
            Glide.with(requireActivity())
                .load(pic.img)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        image.setImageResource(R.drawable.ic_baseline_broken_image_24)
                        return true
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        mBinding.image.startAnim()
                        image.setImageDrawable(resource)
                        return true
                    }
                })
                .into(image)
        }
    }


    override fun initEvent() {
        mBinding.apply {
            image.setOnClickListener {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .popBackStack()
            }

            image.setOnLongClickListener {
                relativeLayout.visibility = View.VISIBLE
                true
            }

            relativeLayout.setOnClickListener {
                relativeLayout.visibility = View.GONE
            }

            btnDownload.setOnClickListener {
                btnDownload.text = "正在下载"
                saveImage(requireContext(), pic.preview) { success, msg ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            btnDownload.text = "已下载"
                            btnDownload.isEnabled = false
                            relativeLayout.visibility = View.GONE
                        } else {
                            btnDownload.text = "下载失败"
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "PicItemFragment"
    }

}
