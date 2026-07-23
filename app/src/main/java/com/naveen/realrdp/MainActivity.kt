package com.naveen.realrdp

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java)
            serviceIntent.putExtra("resultCode", result.resultCode)
            serviceIntent.putExtra("data", result.data)
            startForegroundService(serviceIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val startBtn = Button(this)
        startBtn.text = "Start Screen Share"
        startBtn.setOnClickListener {
            screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
        }

        layout.addView(startBtn)
        setContentView(layout)
    }
}
