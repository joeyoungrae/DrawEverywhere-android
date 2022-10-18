package com.draw.free.fragment

import android.os.Bundle
import android.view.View

class NftContainer : BaseContainerFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.fragments.size == 0) {
            action(OrderType.NftMain)
        }
    }


}