package com.zrq.nicepicture.ui

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zrq.nicepicture.R
import com.zrq.nicepicture.databinding.FragmentPicItemBinding

class PicItemFragment(private val url: String) : BaseFragment<FragmentPicItemBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicItemBinding {
        return FragmentPicItemBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        mBinding.apply {
            Glide.with(requireActivity())
                .load(url)
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
        }
    }

    companion object {
        const val TAG = "PicItemFragment"
    }

}
