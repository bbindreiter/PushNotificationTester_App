package com.firstrowria.pushnotificationtester.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import com.firstrowria.pushnotificationtester.R
import com.firstrowria.pushnotificationtester.manager.TextNotificationManager
import com.firstrowria.pushnotificationtester.network.connect
import com.firstrowria.pushnotificationtester.network.triggerNotification
import com.firstrowria.pushnotificationtester.services.FCMMessagingService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_step1.*
import kotlinx.android.synthetic.main.content_step3.*

class MainActivity : AppCompatActivity() {

    private var step = 0
    private var pushId = ""

    private var aboutDialog: AlertDialog? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val success = intent.getBooleanExtra(BROADCAST_SUCCESS, false)

            if (action == BROADCAST_ACTION_PUSH_REGISTER) {
                if (success) {
                    step1Item3SuccessImageView.isVisible = true
                    pushId = intent.getStringExtra(BROADCAST_PUSH_ID)
                    step = step or (1 shl RESULT_FLAG_PUSH_REGISTERED)
                } else {
                    step1Item3ErrorImageView.isVisible = true
                }

                step1Item3ProgressBar.isVisible = false

            } else if (action == BROADCAST_ACTION_SERVER_CONNECTION) {
                if (success) {
                    step1Item4SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_SERVER_CONNECTION)
                } else {
                    step1Item4ErrorImageView.isVisible = true
                }

                step1Item4ProgressBar.isVisible = false
            } else if (action == BROADCAST_ACTION_NOTIFICATION_REQUESTED) {
                if (success) {
                    step3Item1SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_NOTIFICATION_REQUESTED)
                } else {
                    step3Item1ErrorImageView.isVisible = true
                }

                step3Item1ProgressBar.isVisible = false
                step3Item2ProgressBar.isVisible = true
            } else if (action == BROADCAST_ACTION_NOTIFICATION_ARRIVED) {
                if (success) {
                    step3Item2SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_NOTIFICATION_ARRIVED)
                } else {
                    step3Item2ErrorImageView.isVisible = true
                }

                step3Item2ProgressBar.isVisible = false
                step3Item3ProgressBar.isVisible = true
            } else if (action == BROADCAST_ACTION_NOTIFICATION_SHOWN) {
                if (success) {
                    step3Item3SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_NOTIFICATION_SHOWN)
                } else {
                    step3Item3ErrorImageView.isVisible = true
                }

                step3Item3ProgressBar.isVisible = false
                step3Item4ProgressBar.isVisible = true

                FCMMessagingService.unregister(applicationContext)
            } else if (action == BROADCAST_ACTION_PUSH_UNREGISTER) {
                if (success) {
                    step3Item4SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_NOTIFICATION_UNREGISTER)
                } else {
                    step3Item4ErrorImageView.isVisible = true
                }

                step3Item4ProgressBar.isVisible = false
            }


            if (step == RESULT_STEP1_SUCCESSFUL) {
                continueButton.visibility = View.VISIBLE
                continueButton.text = getString(R.string.next)
            } else if (step == RESULT_STEP3_SUCCESSFUL) {
                viewFlipper.showNext()

                toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.success))
                window.statusBarColor = ContextCompat.getColor(context, R.color.success_dark)

                toolbarTextView.text = getString(R.string.success)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val deliveryPrioritySpinner = findViewById<View>(R.id.deliveryPrioritySpinner) as Spinner
        deliveryPrioritySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                arrayOf(getString(R.string.normal), getString(R.string.high)))

        val notificationPrioritySpinner = findViewById<View>(R.id.notificationPrioritySpinner) as Spinner
        notificationPrioritySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                arrayOf(getString(R.string.min), getString(R.string.low), getString(R.string.standard), getString(R.string.high), getString(R.string.max)))

        TextNotificationManager.getDefaultNotificationChannel(this)?.let {
            notificationPrioritySpinner.isEnabled = false
            //importance is 1-5, items in list are 0-4
            notificationPrioritySpinner.setSelection(it.importance - 1)
            findViewById<View>(R.id.notificationPriorityChannelHint).isVisible = true
            findViewById<View>(R.id.notificationPriorityDescription).isVisible = false
        }

        val delayNumberPicker = findViewById<View>(R.id.delayNumberPicker) as NumberPicker
        delayNumberPicker.minValue = 0
        delayNumberPicker.maxValue = MAX_PUSH_NOTIFICATION_DELAY_IN_SEC
        delayNumberPicker.wrapSelectorWheel = false

        continueButton.setOnClickListener {
            if (step == 0) {
                continueButton.isVisible = false

                //play services check
                val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
                if (resultCode != ConnectionResult.SUCCESS) {
                    step1Item1ErrorImageView.isVisible = true
                    Log.e(TAG, "Cannot find proper Play Services: $resultCode")

                    if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                        GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show()
                } else {
                    step1Item1SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_PLAY_SERVICES)
                }

                //internet connectivity check
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (connectivityManager.activeNetworkInfo != null) {
                    Log.d(TAG, "Connected to Internet")

                    step1Item2SuccessImageView.isVisible = true
                    step = step or (1 shl RESULT_FLAG_INTERNET_CONNECTION)
                } else {
                    Log.e(TAG, "Not connected to Internet")

                    step1Item2ErrorImageView.isVisible = true
                }

                viewFlipper.showNext()
                toolbarTextView.text = getString(R.string.step1)

                step1Item3ProgressBar.isVisible = true
                step1Item4ProgressBar.isVisible = true

                //try to register for GCM and check Server connection as well
                connect(applicationContext)
                FCMMessagingService.register(applicationContext)
            } else if (step == RESULT_STEP1_SUCCESSFUL) {
                viewFlipper.showNext()
                toolbarTextView.text = getString(R.string.step2)
                continueButton.text = getString(R.string.request_notification)

                step = step or (1 shl RESULT_FLAG_NOTIFICATION_READY)
            } else if (step == RESULT_STEP2_SUCCESSFUL) {

                continueButton.isVisible = false
                viewFlipper.showNext()
                toolbarTextView.text = getString(R.string.step3)

                step3Item1ProgressBar.isVisible = true

                triggerNotification(applicationContext,
                        pushId,
                        delayNumberPicker.value,
                        deliveryPrioritySpinner.selectedItemPosition,
                        notificationPrioritySpinner.selectedItemPosition
                )
            }
        }

        val filter = IntentFilter()
        filter.addAction(BROADCAST_ACTION_PUSH_REGISTER)
        filter.addAction(BROADCAST_ACTION_PUSH_UNREGISTER)
        filter.addAction(BROADCAST_ACTION_SERVER_CONNECTION)
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_REQUESTED)
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_ARRIVED)
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_SHOWN)

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()

        if (aboutDialog?.isShowing == true)
            aboutDialog!!.dismiss()

        if (step == RESULT_STEP3_SUCCESSFUL)
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            aboutDialog = AlertDialog.Builder(this@MainActivity).create()
            aboutDialog!!.setTitle(getString(R.string.action_about))
            aboutDialog!!.setMessage(getString(R.string.about))
            aboutDialog!!.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok)) { d, _ -> d.dismiss() }
            aboutDialog!!.show()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val TAG = "PushNotificationTester"

        const val BROADCAST_ACTION_PUSH_REGISTER = "BROADCAST_ACTION_PUSH_REGISTER"
        const val BROADCAST_ACTION_SERVER_CONNECTION = "BROADCAST_ACTION_SERVER_CONNECTION"
        const val BROADCAST_ACTION_NOTIFICATION_REQUESTED = "BROADCAST_ACTION_NOTIFICATION_REQUESTED"
        const val BROADCAST_ACTION_NOTIFICATION_ARRIVED = "BROADCAST_ACTION_NOTIFICATION_ARRIVED"
        const val BROADCAST_ACTION_NOTIFICATION_SHOWN = "BROADCAST_ACTION_NOTIFICATION_SHOWN"
        const val BROADCAST_ACTION_PUSH_UNREGISTER = "BROADCAST_ACTION_PUSH_UNREGISTER"

        const val BROADCAST_SUCCESS = "BROADCAST_SUCCESS"
        const val BROADCAST_ERROR_CODE = "BROADCAST_ERROR_CODE"
        const val BROADCAST_PUSH_ID = "BROADCAST_PUSH_ID"

        private const val RESULT_FLAG_PLAY_SERVICES = 0
        private const val RESULT_FLAG_INTERNET_CONNECTION = 1
        private const val RESULT_FLAG_PUSH_REGISTERED = 2
        private const val RESULT_FLAG_SERVER_CONNECTION = 3
        private const val RESULT_FLAG_NOTIFICATION_READY = 4
        private const val RESULT_FLAG_NOTIFICATION_REQUESTED = 5
        private const val RESULT_FLAG_NOTIFICATION_ARRIVED = 6
        private const val RESULT_FLAG_NOTIFICATION_SHOWN = 7
        private const val RESULT_FLAG_NOTIFICATION_UNREGISTER = 8
        private const val RESULT_STEP1_SUCCESSFUL = 15 //1111
        private const val RESULT_STEP2_SUCCESSFUL = 31 //11111
        private const val RESULT_STEP3_SUCCESSFUL = 511 //111111111

        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 0
        private const val MAX_PUSH_NOTIFICATION_DELAY_IN_SEC = 60 * 60
    }
}

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if(value) View.VISIBLE else View.INVISIBLE }

