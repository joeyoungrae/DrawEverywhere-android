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

class SinglePostBottomFragmentDialog() : BottomSheetDialogFragment() {

    lateinit var editPostActivity : () -> Unit
    lateinit var removePost : () -> Unit
    var isMine = false
    var declarationPost : (() -> Unit)? = null
    var declarationUser : (() -> Unit)? = null

    private var _binding: DialogBottomsheetPostMenuBinding? = null
    private val binding
        get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogBottomsheetPostMenuBinding.inflate(inflater, container, false)
        return _binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false

        if (isMine) {
            binding.itemEdit.visibility = View.VISIBLE
            binding.itemDelete.visibility = View.VISIBLE
        } else {
            binding.itemEdit.visibility = View.INVISIBLE
            binding.itemDelete.visibility = View.INVISIBLE
        }

        binding.itemDelete.setOnClickListener {
            removePost()
        }

        binding.itemEdit.setOnClickListener {
            editPostActivity()
        }

        binding.itemDeclarationPost.setOnClickListener {
            if (Global.userProfile != null) {
                declarationPost!!()
            } else {
                Global.makeToast("로그인을 해주세요.")
            }
        }

        binding.itemDeclarationUser.setOnClickListener {
            if (Global.userProfile != null) {
                declarationUser!!()
            } else {
                Global.makeToast("로그인을 해주세요.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        declarationPost = null
        _binding = null
    }
}