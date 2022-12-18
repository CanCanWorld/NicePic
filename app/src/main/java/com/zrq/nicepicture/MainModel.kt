package com.zrq.nicepicture

import androidx.lifecycle.ViewModel
import com.zrq.nicepicture.bean.Vertical

class MainModel : ViewModel() {
    val list = mutableListOf<Vertical>()
    var pos = 0
}