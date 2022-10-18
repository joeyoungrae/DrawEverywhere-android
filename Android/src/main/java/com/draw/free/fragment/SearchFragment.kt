package com.draw.free.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.draw.free.*
import com.draw.free.databinding.FragmentSearchBinding
import com.draw.free.fragment.viewPagerFragment.*
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.SearchFragmentViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONArray
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : BaseInnerFragment<FragmentSearchBinding>() {
    interface SearchAction {
        fun openPost(postId: String, customPostList: CustomList<Post>)
        fun openTag(hashtag: String);
        fun openProfile(accountId: String)
    }

    val iOpenPost = object : IOpenPost {
        override fun open(postId: String, customPostList: CustomList<Post>) {
            searchAction!!.openPost(postId, customPostList)
        }
    }

    var searchAction: SearchAction? = null
    private lateinit var viewModel: SearchFragmentViewModel

    private fun getKeywordList(): ArrayList<String> {
        val rk = Global.prefs.getKeyword
        val keywords = ArrayList<String>()

        if (rk != null) {
            val jsonArray = JSONArray(rk)

            for (i in 0 until jsonArray.length()) {
                keywords.add(jsonArray.get(i) as String)
            }
        }
        keywords.reverse()

        return keywords
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SearchFragmentViewModel::class.java)
        viewModel.mOpenPost = {
            iOpenPost.open(it, viewModel.postListByTitle.value!!)
        }
        viewModel.mOpenUserProfile = {
            searchAction?.openProfile(it)
        }
        viewModel.mOpenTag = {
            searchAction?.openTag(it)
        }

        binding.viewmodel = viewModel

        //
        val adapter = SearchViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = 0


        // 탭 레이아웃과 뷰페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.search_title)
                }
                1 -> {
                    tab.text = getString(R.string.search_user)
                }
                2 -> {
                    tab.text = getString(R.string.search_tag)
                }
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewModel.switchType(SearchFragmentViewModel.SearchType.Title)
                    }
                    1 -> {
                        viewModel.switchType(SearchFragmentViewModel.SearchType.User)
                    }
                    2 -> {
                        viewModel.switchType(SearchFragmentViewModel.SearchType.Tags)
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        viewModel.typeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                SearchFragmentViewModel.SearchType.Title -> {
                    binding.searchView.hint = context?.getString(R.string.search_title)
                }
                SearchFragmentViewModel.SearchType.User -> {
                    binding.searchView.hint = context?.getString(R.string.search_user)
                }
                SearchFragmentViewModel.SearchType.Tags -> {
                    binding.searchView.hint = context?.getString(R.string.search_tag)
                }
            }
        }

        val touch = { type : RecentlySearchKeywordAdapter.InputType, pos : Int, keyword : String ->
            when (type) {
                RecentlySearchKeywordAdapter.InputType.Search -> {
                    val rk = Global.prefs.getKeyword

                    val jsonArray: JSONArray by lazy {
                        if (rk != null) {
                            JSONArray(rk)
                        } else {
                            val list = ArrayList<String>()
                            JSONArray(list)
                        }
                    }
                    if (jsonArray.length() == 20) {
                        jsonArray.remove(jsonArray.length() - 1)
                    }

                    for (i in 0 until jsonArray.length()) {
                        if ((jsonArray.get(i) as String) == keyword) {
                            Timber.e("같아서 지움")
                            jsonArray.remove(i)
                            break
                        }
                    }

                    jsonArray.put(keyword)

                    Global.prefs.getKeyword = jsonArray.toString()

                    viewModel.search(keyword = keyword)
                    binding.searchView.setText(keyword)


                    binding.searchView.clearFocus()
                    val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
                }

                RecentlySearchKeywordAdapter.InputType.Delete -> {
                    val jsonArray = JSONArray(Global.prefs.getKeyword)
                    jsonArray.remove(pos)

                    Global.prefs.getKeyword = jsonArray.toString()

                    if (binding.rvRecentlySearch.adapter != null) {
                        binding.rvRecentlySearch.adapter?.notifyDataSetChanged()
                    }
                }
            }

            Timber.d("end")
        }

        binding.releaseFocus.setOnClickListener {
            binding.searchView.clearFocus()
            val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }

        binding.searchView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                Timber.e("focus")

                binding.recentlySearchList.visibility = View.VISIBLE
                binding.viewPager.visibility = View.INVISIBLE
                binding.tabLayout.visibility = View.INVISIBLE

                val keywords = getKeywordList()
                val adapter = RecentlySearchKeywordAdapter(keywords)
                adapter.touch = touch
                binding.rvRecentlySearch.adapter = adapter
            } else {
                Timber.e("not focus")

                binding.recentlySearchList.visibility = View.INVISIBLE
                binding.viewPager.visibility = View.VISIBLE
                binding.tabLayout.visibility = View.VISIBLE
            }
        }


        val keywords = getKeywordList()

        binding.rvRecentlySearch.layoutManager = LinearLayoutManager(requireContext())

        val searchAdapter = RecentlySearchKeywordAdapter(keywords)
        searchAdapter.touch = touch
        binding.rvRecentlySearch.adapter = searchAdapter



        binding.searchView.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
                Timber.d("검색")
                if ((v as EditText).text.toString().isEmpty()) {
                    Global.makeToast("검색어를 입력해주세요.")
                    return@setOnKeyListener false
                }

                val rk = Global.prefs.getKeyword

                val jsonArray: JSONArray by lazy {
                    if (rk != null) {
                        JSONArray(rk)
                    } else {
                        val list = ArrayList<String>()
                        JSONArray(list)
                    }
                }
                if (jsonArray.length() == 20) {
                    jsonArray.remove(jsonArray.length() - 1)
                }

                for (i in 0 until jsonArray.length()) {
                    if ((jsonArray.get(i) as String) == v.text.toString()) {
                        Timber.e("같아서 지움")
                        jsonArray.remove(i)
                        break
                    }
                }

                jsonArray.put(v.text.toString())

                Global.prefs.getKeyword = jsonArray.toString()



                viewModel.search(keyword = v.text.toString())

                v.clearFocus()
                v.requestFocus()
                val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
            }
            return@setOnKeyListener false
        }


    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(false)
            showBottomNav.show(true)
        }

    }

    inner class SearchViewPagerAdapter(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> SearchPostListFragment()
                1 -> SearchUserListFragment()
                2 -> SearchTagListFragment()
                else -> SearchPostListFragment()
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun downKeyboardOutOfTouch(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                binding.searchView.clearFocus()
                binding.searchView.requestFocus()
                val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    activity?.currentFocus?.windowToken,
                    0
                )
            }
            true
        }
    }


}