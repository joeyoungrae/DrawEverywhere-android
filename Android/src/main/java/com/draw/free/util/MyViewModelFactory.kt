package com.draw.free.util

import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import com.draw.free.viewmodel.UserRelationListViewModel
import com.draw.free.viewmodel.CommentFragmentViewModel


class MyViewModelFactory(val stringValue : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommentFragmentViewModel(stringValue) as T
    }

    fun <T : ViewModel> createUserRelationListModel(modelClass: Class<T>): T {
        return UserRelationListViewModel(stringValue) as T
    }
}