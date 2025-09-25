package com.timo.timoterminal.components

import com.google.android.material.button.MaterialButton

class ResIdMaterialButton : MaterialButton {
    private var resId: Int? = null

    constructor(context: android.content.Context) : super(context)
    constructor(
        context: android.content.Context,
        attrs: android.util.AttributeSet?
    ) : super(context, attrs)

    constructor(
        context: android.content.Context,
        attrs: android.util.AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    fun setResId(id: Int?) {
        resId = id
    }

    fun getResId(): Int? {
        return resId
    }
}