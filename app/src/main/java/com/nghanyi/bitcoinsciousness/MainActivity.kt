package com.nghanyi.bitcoinsciousness

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nghanyi.bitcoinsciousness.databinding.ActivityMainBinding
import kotlinx.coroutines.*

/**
 * PLEASE DON'T CODE LIKE THIS DURING THE TEST.
 * BAD PROGRAMMING PRACTICES.
 *
 * This is just to make all the functionalities work for now :)
 */

private const val MIN = 50
private const val MAX = 5000
private const val STEP = 50
private const val INITIAL_VALUE = 1000
class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private lateinit var binding: ActivityMainBinding
    private lateinit var streamService: StreamService
    private lateinit var streamServiceIntent: Intent
    private lateinit var notiServiceIntent: Intent
    private lateinit var priceListData: ArrayList<Price>

    private var isBounded = false
    private var startStream = true
    private var current10MA: Double = 0.0
    private var previous10MA: Double = 0.0
    private var delay: Int = INITIAL_VALUE

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            streamService = (service as StreamService.StreamServiceBinder).getService()
            isBounded = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBounded = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindStreamService()
        startNotiService()
        setupPicker()
        setupRecyclerView()
        loadPrice()

        binding.shutdownButton.setOnClickListener {
            stopService(streamServiceIntent)
            stopService(notiServiceIntent)
            isBounded = false
        }

        binding.toggleButton.setOnClickListener {
            startStream = !startStream
            if (startStream) {
                binding.toggleButton.text = getString(R.string.stop)
            } else {
                binding.toggleButton.text = getString(R.string.start)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(notiServiceIntent)
        unbindService(serviceConnection)
    }

    private fun loadPrice() {
        var index = 0
        var counter = 1
        var totalClosePrices = 0.0

        launch {
            while (true) {
                if (isBounded && startStream) {
                    if (index == streamService.priceList.size) break
                    delay(delay.toLong())

                    val currentPrice = streamService.priceList[index++]
                    priceListData.add(currentPrice)
                    updateAdapter(priceListData)

                    totalClosePrices += currentPrice.close

                    if (counter == 10) {
                        previous10MA = current10MA
                        current10MA = totalClosePrices / 10
                        totalClosePrices = 0.0
                        counter = 1
                        updateNoti(current10MA, previous10MA)
                    } else {
                        counter++
                    }

                    Log.d(TAG, "Price: $currentPrice")
                }
            }
        }
    }

    /**
     * Pass data to the notification service intent
     * and start the service.
     *
     * @param current10MA current 10MA value
     * @param previous10MA previous 10MA value
     */
    private fun updateNoti(current10MA: Double, previous10MA: Double) {
        notiServiceIntent.putExtra("current10MA", current10MA)
        notiServiceIntent.putExtra("previous10MA", previous10MA)
        startService(notiServiceIntent)
    }

    /**
     * Update the adapter with the new data.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(priceListData: ArrayList<Price>) {
        MainScope().launch {
            binding.priceRecyclerView.adapter?.notifyDataSetChanged()
            binding.priceRecyclerView.smoothScrollToPosition(priceListData.size - 1)
        }
    }

    /**
     * Bind the stream service.
     */
    private fun bindStreamService() {
        streamServiceIntent = Intent(this, StreamService::class.java)
        bindService(streamServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    /**
     * Start the notification service.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startNotiService() {
        notiServiceIntent = Intent(this, NotiService::class.java)
        startForegroundService(notiServiceIntent)
    }

    /**
     * Setup the picker.
     */
    private fun setupPicker() {
        val values = getDisplayedValues()
        binding.freqPicker.minValue = 0
        binding.freqPicker.maxValue = values.size - 1
        binding.freqPicker.value = values.indexOf(INITIAL_VALUE.toString())
        binding.freqPicker.displayedValues = values
        binding.freqPicker.wrapSelectorWheel = false
        binding.freqPicker.setOnValueChangedListener { _, _, newVal ->
            delay = values[newVal].toInt()
            Log.d(TAG, "new interval: ${values[newVal]}")
        }
    }

    /**
     *
     */
    private fun setupRecyclerView() {
        priceListData = ArrayList()
        binding.priceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.priceRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.priceRecyclerView.adapter = PriceAdapter(priceListData)
    }

    /**
     * Get frequencies in increments of 50
     */
    private fun getDisplayedValues(): Array<String> {
        val values = ArrayList<String>()
        for (i in MIN..MAX step STEP) {
            values.add(i.toString())
        }
        return values.toTypedArray()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}