package dev.ragnarok.fenrir.player

import java.util.*

class Shuffler(private val mMaxHistorySize: Int) {
    private val mHistoryOfNumbers = LinkedList<Int>()
    private val mPreviousNumbers = TreeSet<Int>()
    private val mRandom = Random()
    private var mPrevious = 0
    fun nextInt(interval: Int): Int {
        var next: Int
        do {
            next = mRandom.nextInt(interval)
        } while (next == mPrevious && interval > 1 && !mPreviousNumbers.contains(next))
        mPrevious = next
        mHistoryOfNumbers.add(mPrevious)
        mPreviousNumbers.add(mPrevious)
        cleanUpHistory()
        return next
    }

    private fun cleanUpHistory() {
        if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size >= mMaxHistorySize) {
            for (i in 0 until 1.coerceAtLeast(mMaxHistorySize / 2)) {
                mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst())
            }
        }
    }

}