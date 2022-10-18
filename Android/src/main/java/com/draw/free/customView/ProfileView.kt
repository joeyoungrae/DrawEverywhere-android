package com.draw.free.customView

import android.content.Context
import android.util.AttributeSet
import android.widget.*
import androidx.annotation.Nullable
import com.draw.free.R

class ProfileView : FrameLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private lateinit var content: ImageView

    private fun init() {
        inflate(context, R.layout.view_profile, this)
        content = findViewById(R.id.imageView_profile)
    }

    fun getContent() : ImageView {
        return content
    }

}