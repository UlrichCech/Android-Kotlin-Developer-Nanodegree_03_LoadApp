package com.udacity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val downloadResult = intent.getParcelableExtra<DownloadResult>("downloadresult")

        findViewById<TextView>(R.id.filenameTV).text = downloadResult?.filename
        val resultTextView = findViewById<TextView>(R.id.statusTV)
        resultTextView.text = downloadResult?.result
        if (downloadResult?.result == resources.getString(R.string.downloadResultTextError)) {
            resultTextView.setTextColor(Color.RED)
        }
        val okBtn = findViewById<Button>(R.id.backToMainBtn)
        okBtn.setOnClickListener {
            finish()
        }
    }

}
