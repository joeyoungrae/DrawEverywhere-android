package com.draw.free.dialog

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.paging.filter
import com.draw.free.CommentAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.BottomdialogDescriptionAccountTypeBinding
import com.draw.free.databinding.DialogBottomsheetCommentsBinding
import com.draw.free.databinding.DialogBottomsheetPostMenuBinding
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.Post
import com.draw.free.util.CommentPagingSource
import com.draw.free.util.MyViewModelFactory
import com.draw.free.viewmodel.CommentFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import okhttp3.internal.wait

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class DescriptionAccountTypeBottomFragmentDialog(private val isPrivate : Boolean) : BottomSheetDialogFragment() {

    lateinit var afterNext : () -> Unit

    private var _binding: BottomdialogDescriptionAccountTypeBinding? = null
    private val binding
        get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BottomdialogDescriptionAccountTypeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false

        if (isPrivate) {
            binding.title.text = getString(R.string.change_private_account_type)
            binding.content.text = getString(R.string.description_private_account_type)
        } else {
            binding.title.text = getString(R.string.change_public_account_type)
            binding.content.text = getString(R.string.description_public_account_type)
        }
        binding.description.text = getString(R.string.element_account_type)

        binding.apply.setOnClickListener {
            afterNext()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}