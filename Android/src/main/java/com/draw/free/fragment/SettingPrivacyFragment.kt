package com.draw.free.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.draw.free.R
import com.draw.free.databinding.ActivitySettingBinding
import com.draw.free.databinding.FragmentSettingPrivacyBinding
import com.draw.free.dialog.DescriptionAccountTypeBottomFragmentDialog
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.viewmodel.SearchFragmentViewModel
import com.draw.free.viewmodel.SettingPrivacyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingPrivacyFragment : BaseInnerFragment<FragmentSettingPrivacyBinding>() {

    private lateinit var viewModel: SettingPrivacyViewModel

    init {
        layoutId = R.layout.fragment_setting_privacy
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingPrivacyViewModel::class.java]
        binding.viewmodel = viewModel

        binding.toggleAccountType.setOnClickListener {
            Timber.e("클릭함")
            val nav = DescriptionAccountTypeBottomFragmentDialog(!viewModel.isPrivateAccount.value!!)
            nav.afterNext = {
                viewModel.toggleAccountType()
            }

            nav.show(childFragmentManager, "description")
        }


        viewModel.isPrivateAccount.observe(viewLifecycleOwner) {
            binding.accountTypeSwitch.isChecked = it
        }
    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(isSet = true)
            showBottomNav.show(true)
        }
    }


}