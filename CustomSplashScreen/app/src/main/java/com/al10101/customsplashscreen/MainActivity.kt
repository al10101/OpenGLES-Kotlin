package com.al10101.customsplashscreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.lang.Exception

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.glFragmentContainer, GLFragment.newInstance())
                .add(R.id.uiFragmentContainer, UIFragment.newInstance())
                .commit()
        }

        Thread {
            Log.d(TAG, "The counter is about to start")
            try {
                Thread.sleep(5000)
            } catch (e: Exception) {
                throw e
            }
            Log.d(TAG, "Now the counter has stopped")
        }.start()

    }

}