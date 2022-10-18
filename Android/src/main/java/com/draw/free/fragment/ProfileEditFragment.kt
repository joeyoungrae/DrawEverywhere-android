package com.draw.free.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.draw.free.R
import com.draw.free.databinding.FragmentProfileEditBinding
import com.draw.free.viewmodel.ProfileEditFragmentViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import timber.log.Timber

class ProfileEditFragment : BaseInnerFragment<FragmentProfileEditBinding>() {
    interface ProfileEditAction {
        fun close()
    }

    lateinit var profileEditAction: ProfileEditAction
    private lateinit var viewModel: ProfileEditFragmentViewModel

    companion object {
        private var INSTANCE: ProfileEditFragment? = null;

        fun getInstance(): ProfileEditFragment {
            if (INSTANCE == null) {
                INSTANCE = ProfileEditFragment();
            }

            return INSTANCE!!;
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileEditFragmentViewModel::class.java]
        viewModel.closeFragment = profileEditAction
        binding.viewmodel = viewModel

        // 글라이드 이미지 프로필 사진 대충
        if (viewModel.targetUserProfile.pfPicture.isNullOrEmpty()) {
            Glide.with(binding.imageView)
                .load(R.drawable.pf_picture_default)
                .circleCrop()
                .into(binding.imageView.getContent())
        } else {
            Glide.with(binding.imageView)
                .load(viewModel.targetUserProfile.pfPicture)
                .placeholder(R.drawable.pf_picture_default)
                .circleCrop()
                .into(binding.imageView.getContent())
        }



        binding.imageView.setOnLongClickListener {
            viewModel.isRemoveImage = true
            Glide.with(binding.imageView)
                .load(R.drawable.pf_picture_default)
                .circleCrop()
                .into(binding.imageView.getContent())

            return@setOnLongClickListener true
        }

        binding.imageView.setOnClickListener {
            val intent = CropImage.activity()
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setMinCropResultSize(600, 600)
                .setMaxCropResultSize(1000, 1000)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .getIntent(requireActivity())

            resultLauncher.launch(intent)
        }

        binding.tvChangeProfile.setOnClickListener {
            val intent = CropImage.activity()
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setMinCropResultSize(600, 600)
                .setMaxCropResultSize(1000, 1000)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .getIntent(requireActivity())

            resultLauncher.launch(intent)
        }

    }


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            Timber.d("Something happened")

            viewModel.resultUri = CropImage.getActivityResult(data).uri

            Glide.with(binding.imageView)
                .load(viewModel.resultUri)
                .circleCrop()
                .into(binding.imageView.getContent())
        } else {
            Timber.d("실패함")
        }
    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(false)
            showBottomNav.show(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }

}