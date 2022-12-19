package com.zrq.nicepicture.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zrq.nicepicture.R
import com.zrq.nicepicture.bean.Vertical
import com.zrq.nicepicture.databinding.ItemPictureBinding

class PicAdapter(
    private val context: Context,
    private val list: MutableList<Vertical>,
    private val onClickListener: (View, Int) -> Unit,
    private val onLongClickListener: (View, Int) -> Unit
) : RecyclerView.Adapter<VH<ItemPictureBinding>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ItemPictureBinding> {
        val mBinding = ItemPictureBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(mBinding)
    }

    override fun onBindViewHolder(holder: VH<ItemPictureBinding>, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(list[holder.adapterPosition].thumb)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        ivPic.setImageResource(R.drawable.ic_baseline_broken_image_24)
                        return true
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        ivPic.startAnim()
                        ivPic.setImageDrawable(resource)
                        return true
                    }
                })
                .into(ivPic)
            ivPic.setOnClickListener { onClickListener(it, holder.adapterPosition) }
            ivPic.setOnLongClickListener {
                onLongClickListener(it, holder.adapterPosition)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}