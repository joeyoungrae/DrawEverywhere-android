package com.draw.free.util

import androidx.lifecycle.MutableLiveData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean



class CustomList<T>(pageSize: Int, api: (suspend (pageSize : Int, offset : String) -> List<T>)?, getKey : (data : T) -> String) {
    private val mData = ArrayList<T>()
    private val _mLiveData = MutableLiveData<List<T>>();
    val mLiveData
        get() = _mLiveData


    val findKey = getKey
    val mApi : (suspend (pageSize : Int, offset : String) -> List<T>)? = api;


    var mOffset: String = "0"
    var mIsEnd: Boolean = (api == null)
    var mPageSize = pageSize
    var page = 0;

    var doWorking = AtomicBoolean(false)

    fun findPositionByKey(key : String) : Int {
        for (i in 0 until mData.size) {
            if (findKey(mData[i]) == key) {
                return i;
            }
        }
        return -1
    }

    fun getPostByKey(key : String) : T? {
        for (i in 0 until mData.size) {
            if (findKey(mData[i]) == key) {
                return mData[i];
            }
        }
        return null
    }

    fun deleteItem(data : T) {
        for (i in 0 until mData.size) {
            if (findKey(mData[i]) == findKey(data)) {
                mData.remove(data)
                _mLiveData.postValue(mData);
                return
            }
        }

        Timber.d("삭제 할 아이템이 없음.")
    }

    fun deleteByKey(key : String) {
        for (i in 0 until mData.size) {
            if (findKey(mData[i]) == key) {
                mData.remove(mData[i])
                _mLiveData.postValue(mData);
                return
            }
        }

        Timber.d("삭제 할 아이템이 없음.")
    }




    fun getData() : List<T> {
        return mData;
    }

    fun updateLiveData() {
        _mLiveData.postValue(mData);
    }

    fun addList(dataList: List<T>) {
        for (p in dataList) {
            if (mData.contains(p)) {
                mData[mData.indexOf(p)] = p
            } else {
                mData.add(p);
            }
        }
    }

    fun updateData(data : T) {
        if (mData.contains(data)) {
            mData[mData.indexOf(data)] = data
        } else {
            mData.add(data);
        }
        updateLiveData()
    }

    fun setList(postList : List<T>) {
        //assert (mApi == null) { "고정된 길이를 가진 데이터여서, 새로운 데이터를 불러오는 api 를 쓴 경우 잘못 사용하는 것." }
        mData.addAll(postList);
        _mLiveData.postValue(mData);
        mIsEnd = true
    }


    fun getDataByPosition(position : Int) : T {
        return mData[position]
    }

    fun getItemCount() : Int {
        return mData.size
    }


    // addData
    fun getNextData(refresh : (() -> Unit)? = null) {
        if (doWorking.get()) {
            Timber.d("이미 데이터를 가져오는 중임")
            return
        }
        doWorking.set(true)


        if (mIsEnd || mApi == null) {
            Timber.d("더이상 가져올 수 있는 데이터가 없습니다.")
            doWorking.set(false)
            return;
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val getPosts: List<T> = mApi!!(mPageSize, mOffset)
                ++page;
                if (refresh != null) {
                    refresh()
                }

                if (getPosts.size < mPageSize) {
                    mIsEnd = true;
                }

                for (p in getPosts) {
                    if (mData.contains(p)) {
                        mData[mData.indexOf(p)] = p
                    } else {
                        mData.add(p);
                    }
                }

                if (mData.isNotEmpty()) {
                    mOffset = findKey(mData.last())
                    Timber.d("offset__ :  $mOffset")
                }

                //sort();


                launch(Dispatchers.Main) {
                    _mLiveData.postValue(mData);
                    doWorking.set(false)
                }
            } catch (e : Exception) {
                _mLiveData.postValue(mData);
                Timber.e("가져오기 실패")
                doWorking.set(false)
            }
        }
    }

    fun refreshData() {
        page = 0;
        mOffset = "0";

        mIsEnd = mApi == null

        getNextData() {
            mData.clear();
        }
    }
}