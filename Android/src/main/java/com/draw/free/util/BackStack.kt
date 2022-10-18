package com.draw.free.util

import java.util.*
import kotlin.collections.ArrayList

// isDuplicated ->
class BackStack(private val isDuplicated : Boolean = true) {
    private val mCurrentStack = ArrayList<Int>()

    fun pushStack(value : Int) {
        if (isDuplicated) {
            mCurrentStack.add(value);
        } else {
            val stack = Stack<Int>()
            for (i in 0 until mCurrentStack.size) {
                if (i != value) {
                    stack.push(mCurrentStack[i])
                }
            }
            mCurrentStack.clear()
            while (!stack.empty()) {
                mCurrentStack.add(stack.peek())
                stack.pop()
            }

            mCurrentStack.add(value);
        }
    }

    fun pop() {
        if (mCurrentStack.isNotEmpty()) {
            mCurrentStack.remove(mCurrentStack.size - 1);
        }
    }

    fun peek() : Int {
        if (mCurrentStack.isNotEmpty()) {
            return mCurrentStack.last()
        }
        return -1
    }


    fun getSize() : Int {
        return mCurrentStack.size
    }
}