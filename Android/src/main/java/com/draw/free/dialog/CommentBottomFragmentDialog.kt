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
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.util.CommentPagingSource
import com.draw.free.util.MyViewModelFactory
import com.draw.free.viewmodel.CommentFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import okhttp3.internal.wait

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class CommentBottomFragmentDialog : BottomSheetDialogFragment() {

    var postId: String? = null
    private var _binding: DialogBottomsheetCommentsBinding? = null
    private val binding
        get() = _binding!!

    var passValue : ((Int) -> Unit)? = null

    private lateinit var viewModel: CommentFragmentViewModel
    private lateinit var adapter: CommentAdapter
    private var isTop = AtomicBoolean(false);

    private fun downKeyboardOutOfTouch(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    binding.etComment.clearFocus()
                    binding.etComment.requestFocus()
                    val inputMethodManager: InputMethodManager = activity!!.getSystemService(
                        Activity.INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(activity?.getCurrentFocus()?.getWindowToken(), 0)
                }
                return true
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogBottomsheetCommentsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    val clear = object : ToNextWork {
        override fun next() {
            binding.etComment.setText("");
            handler.postDelayed({
                binding.recyclerView.smoothScrollToPosition(0)
            }, 300)
        }
    }


    private val handler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false


        viewModel = ViewModelProvider(this, MyViewModelFactory(postId!!))[CommentFragmentViewModel::class.java]


        binding.etComment.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                Timber.d("실행 테스트")
                viewModel.addComment(clear)
                (v as EditText).clearFocus()
                v.requestFocus()
                val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

            }

            return@setOnKeyListener false
        }
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.viewmodel = viewModel

        val rv = binding.recyclerView
        adapter = CommentAdapter(viewModel)
        rv.adapter = adapter


        adapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
                binding.isEmpty.visibility = View.VISIBLE
            } else {
                binding.isEmpty.visibility = View.GONE
            }
        }

        viewModel.changedData.observe(viewLifecycleOwner) {
            if (!it) {
                return@observe
            }

            if (adapter.itemCount < 1) {
                binding.isEmpty.visibility = View.VISIBLE
            } else {
                binding.isEmpty.visibility = View.GONE
            }

            viewModel.changedData.postValue(true)
        }


        binding.swipeRefreshLayout.setOnRefreshListener {
            Timber.d("refresh")
            viewModel.clearEventsByRefresh()
            adapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.pagingDataViewStates.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
        }


        if (Global.userProfile == null) {
            binding.etComment.hint = "로그인이 필요한 서비스 입니다."
            binding.etComment.isEnabled = false
        }


        downKeyboardOutOfTouch(binding.root);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun dismiss() {
        if (passValue != null) {
            passValue!!(adapter.itemCount)
        }
        super.dismiss()
    }
}