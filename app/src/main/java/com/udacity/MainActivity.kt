package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private val REQUEST_CODE = 0

    private lateinit var notificationManager: NotificationManager

    private lateinit var downloadSelection: DownloadOption
    private val downloadOptions = ArrayList<DownloadOption>()

    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadOptions.add(DownloadOption(DownloadOption.Identifier.NONE, "", ""))
        downloadOptions.add(DownloadOption(DownloadOption.Identifier.GLIDE, resources.getString(R.string.downloadOptionGlide), "http://")) // MISSING LINK TO FORCE AN ERROR
        downloadOptions.add(DownloadOption(DownloadOption.Identifier.PROJECT_STARTERCODE, resources.getString(R.string.downloadOptionProject), "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"))
        downloadOptions.add(DownloadOption(DownloadOption.Identifier.RETROFIT, resources.getString(R.string.downloadOptionRetrofit), "https://search.maven.org/remote_content?g=com.squareup.retrofit2&a=retrofit&v=LATEST"))
        downloadSelection = getDownloadOption(DownloadOption.Identifier.NONE)

        notificationManager = this.getSystemService(NotificationManager::class.java)

        createChannel(CHANNEL_ID, resources.getString(R.string.notification_channelname))

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        radioGroup = this.findViewById<RadioGroup>(R.id.selectionGroup)

        radioGroup.setOnCheckedChangeListener(
                RadioGroup.OnCheckedChangeListener { group, checkedId ->
                    downloadSelection = when (checkedId) {
                        R.id.glideOptionRB -> getDownloadOption(DownloadOption.Identifier.GLIDE)
                        R.id.projectOptionRB -> getDownloadOption(DownloadOption.Identifier.PROJECT_STARTERCODE)
                        R.id.projectOptionRetrofit -> getDownloadOption(DownloadOption.Identifier.RETROFIT)
                        else -> getDownloadOption(DownloadOption.Identifier.NONE)
                    }
                }
        )

        custom_button.setOnClickListener {
            if (downloadSelection.id == DownloadOption.Identifier.NONE) {
                Toast.makeText(this, getString(R.string.select_option_error_message), Toast.LENGTH_SHORT).show()
            } else {
                custom_button.setState(ButtonState.Loading)
                // check Android Build version for permission handling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        // permission is denied, so request the permission from the user
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE_STORAGE)
                    } else {
                        // permission already granted by user
                        download()
                    }
                } else {
                    // Android-Version greater than Marshmallow is used, so Internet-permission is already granted
                    download()
                }
            }
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download()
                } else {
                    Toast.makeText(this, getString(R.string.permissionGrantedInfotext), Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(id!!)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        custom_button.setState(ButtonState.Completed)
                        val downloadResult = DownloadResult(downloadSelection.label, getString(R.string.downloadResultTextOK))
                        notificationManager.sendNotification(downloadResult, R.drawable.ic_baseline_check_circle_24, applicationContext)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        custom_button.setState(ButtonState.Completed)
                        val downloadResult = DownloadResult(downloadSelection.label, getString(R.string.downloadResultTextError))
                        downloadManager.remove(id)
                        notificationManager.sendNotification(downloadResult, R.drawable.ic_baseline_error_24, applicationContext)
                    }
                }
            }
        }
    }


    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(downloadSelection.url))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }


    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download status"
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    fun NotificationManager.sendNotification(result: DownloadResult, icon: Int, applicationContext: Context) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val showDetailsIntent = Intent(applicationContext, ShowDetailsReceiver::class.java)
        val bundle = Bundle()
        bundle.putParcelable("downloadresult", result)
        showDetailsIntent.putExtras(bundle)

        val showDetailsPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                REQUEST_CODE,
                showDetailsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val large = ContextCompat.getDrawable(applicationContext, icon)?.let {
            drawableToBitmap(it)
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                .setLargeIcon(large)
                .setContentTitle(applicationContext.getString(R.string.notification_title))
                .setContentText(result.filename)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .addAction(0, applicationContext.getString(R.string.notification_button), showDetailsPendingIntent)
        notify(NOTIFICATION_ID, builder.build())
    }


    private fun getDownloadOption(id: DownloadOption.Identifier): DownloadOption {
        return downloadOptions.single { o -> o.id == id }
    }


    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
        private const val PERMISSION_REQUEST_CODE_STORAGE = 10
    }


    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
