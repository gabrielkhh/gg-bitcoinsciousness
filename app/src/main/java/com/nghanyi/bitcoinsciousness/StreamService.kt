package com.nghanyi.bitcoinsciousness

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class StreamService : Service() {

    private val file = "eth.csv"
    private val binder = StreamServiceBinder()
    val priceList = ArrayList<Price>()

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")
        loadPriceFromFile()
        return binder
    }

    /**
     * Load price from file
     */
    private fun loadPriceFromFile() {
        val inputStream = assets.open(file)
        val reader = inputStream.bufferedReader()
        var line = reader.readLine() // discard header
        while (reader.readLine().also { line = it } != null) {
            val tokens = line.split(",")
            priceList.add(Price(
                tokens[0],
                tokens[1].toDouble(),
                tokens[2].toDouble(),
                tokens[3].toDouble(),
                tokens[4].toDouble(),
                tokens[5].toLong())
            )
        }
        Log.d(TAG, "Loaded file $file with ${priceList.size} records...")
    }

    companion object {
        private val TAG = StreamService::class.java.simpleName
    }

    inner class StreamServiceBinder: Binder() {
        fun getService(): StreamService {
            return this@StreamService
        }
    }
}