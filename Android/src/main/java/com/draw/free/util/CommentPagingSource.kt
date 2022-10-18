package com.draw.free.util

import androidx.paging.*
import com.draw.free.model.Comment
import com.draw.free.network.RetrofitClient
import timber.log.Timber

class CommentPagingSource(val postId: String, var pageIndex: LinkedHashMap<String?, String?>) :
    PagingSource<String, Comment>() {

    init {
        staticPageIndex = pageIndex
    }

    companion object {
        const val PAGE = 10
        private var staticPageIndex = LinkedHashMap<String?, String?>()
        var refreshMode = -1
    }

    private val commentDao = RetrofitClient.getCommentService()

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Comment> {
        try {

            // 정의되지 않은 경우 최초 페이지는 0

            val curPage = params.key ?: "0";
            val response = commentDao.getComments(curPage, PAGE, postId)

            Timber.d("size : ${response.data.size}")

            var nextPageValue: String? = null

            if (response.data.isEmpty() || response.data.size != PAGE) {
                nextPageValue = null
            }


            return LoadResult.Page(
                data = response.data, prevKey = null, //
                nextKey = nextPageValue
            )


        } catch (e: Exception) {
            e.printStackTrace();

            return LoadResult.Page(
                data = emptyList(), prevKey = null, //
                nextKey = null
            )
        }
    }

    override fun getRefreshKey(state: PagingState<String, Comment>): String {
        return "0"

        /* if (refreshMode == 0) {
                refreshMode = -1;
                return "0"
            }

            return state.firstItemOrNull()?.id?:"0" */

        /*val previousKey = staticPageIndex.get(state.firstItemOrNull()?.id) ?: "0"

        val it = staticPageIndex.entries.iterator()
        var bRemoved = previousKey == "0"

        Timber.d("source : ${state.lastItemOrNull()?.id} refresh Key : $previousKey, Removed : $bRemoved")


        while (it.hasNext()) {
            if (it.next().key.equals(state.firstItemOrNull()?.id?:"")) {
                Timber.d("키 발견하여 이후 항목 삭제함")
                bRemoved = true
            }
            if (bRemoved) {
                it.remove()
            }
        }

        //return state.firstItemOrNull()?.id ?: "0"
        return previousKey*/
    }

    fun refresh() {
        invalidate()
    }


}