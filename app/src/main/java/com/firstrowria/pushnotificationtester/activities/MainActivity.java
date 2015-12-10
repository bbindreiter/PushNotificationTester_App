package com.firstrowria.pushnotificationtester.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.firstrowria.pushnotificationtester.R;
import com.firstrowria.pushnotificationtester.broadcast.GCMReceiver;
import com.firstrowria.pushnotificationtester.threads.ConnectThread;
import com.firstrowria.pushnotificationtester.threads.TriggerNotificationThread;
import com.firstrowria.pushnotificationtester.util.PlayServicesUtil;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "PushNotificationTester";

    public static final String BROADCAST_ACTION_PUSH_REGISTER = "BROADCAST_ACTION_PUSH_REGISTER";
    public static final String BROADCAST_ACTION_SERVER_CONNECTION = "BROADCAST_ACTION_SERVER_CONNECTION";
    public static final String BROADCAST_ACTION_NOTIFICATION_REQUESTED = "BROADCAST_ACTION_NOTIFICATION_REQUESTED";
    public static final String BROADCAST_ACTION_NOTIFICATION_ARRIVED = "BROADCAST_ACTION_NOTIFICATION_ARRIVED";
    public static final String BROADCAST_ACTION_NOTIFICATION_SHOWN = "BROADCAST_ACTION_NOTIFICATION_SHOWN";
    public static final String BROADCAST_ACTION_NOTIFICATION_SERVICE_STOP = "BROADCAST_ACTION_NOTIFICATION_SERVICE_STOP";
    public static final String BROADCAST_ACTION_PUSH_UNREGISTER = "BROADCAST_ACTION_PUSH_UNREGISTER";

    public static final String BROADCAST_SUCCESS = "BROADCAST_SUCCESS";
    public static final String BROADCAST_PUSH_ID = "BROADCAST_PUSH_ID";
    private static final int RESULT_FLAG_PLAY_SERVICES = 0;
    private static final int RESULT_FLAG_INTERNET_CONNECTION = 1;
    private static final int RESULT_FLAG_PUSH_REGISTERED = 2;
    private static final int RESULT_FLAG_SERVER_CONNECTION = 3;
    private static final int RESULT_FLAG_NOTIFICATION_READY = 4;
    private static final int RESULT_FLAG_NOTIFICATION_REQUESTED = 5;
    private static final int RESULT_FLAG_NOTIFICATION_ARRIVED = 6;
    private static final int RESULT_FLAG_NOTIFICATION_SHOWN = 7;
    private static final int RESULT_FLAG_NOTIFICATION_SERVICE_STOP = 8;
    private static final int RESULT_FLAG_NOTIFICATION_UNREGISTER = 9;
    private static final int RESULT_STEP1_SUCCESSFUL = 15; //1111
    private static final int RESULT_STEP2_SUCCESSFUL = 31; //11111
    private static final int RESULT_STEP3_SUCCESSFUL = 1023; //1111111111
    private int step = 0;
    private String pushId = "";

    private ViewFlipper viewFlipper = null;
    private FrameLayout step1Item1FrameLayout = null;
    private FrameLayout step1Item2FrameLayout = null;
    private FrameLayout step1Item3FrameLayout = null;
    private FrameLayout step1Item4FrameLayout = null;
    private FrameLayout step3Item1FrameLayout = null;
    private FrameLayout step3Item2FrameLayout = null;
    private FrameLayout step3Item3FrameLayout = null;
    private FrameLayout step3Item4FrameLayout = null;
    private FrameLayout step3Item5FrameLayout = null;

    private Button continueButton = null;
    private TextView toolBarTextView = null;
    private Toolbar toolBar = null;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean success = intent.getBooleanExtra(BROADCAST_SUCCESS, false);

            if (action.equals(BROADCAST_ACTION_PUSH_REGISTER)) {
                if (success) {
                    step1Item3FrameLayout.findViewById(R.id.step1Item3SuccessImageView).setVisibility(View.VISIBLE);
                    pushId = intent.getStringExtra(BROADCAST_PUSH_ID);
                    step = step | (1 << RESULT_FLAG_PUSH_REGISTERED);
                } else {
                    step1Item3FrameLayout.findViewById(R.id.step1Item3ErrorImageView).setVisibility(View.VISIBLE);
                }

                step1Item3FrameLayout.findViewById(R.id.step1Item3ProgressBar).setVisibility(View.INVISIBLE);

            } else if (action.equals(BROADCAST_ACTION_SERVER_CONNECTION)) {
                if (success) {
                    step1Item4FrameLayout.findViewById(R.id.step1Item4SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_SERVER_CONNECTION);
                } else {
                    step1Item4FrameLayout.findViewById(R.id.step1Item4ErrorImageView).setVisibility(View.VISIBLE);
                }

                step1Item4FrameLayout.findViewById(R.id.step1Item4ProgressBar).setVisibility(View.INVISIBLE);
            } else if (action.equals(BROADCAST_ACTION_NOTIFICATION_REQUESTED)) {
                if (success) {
                    step3Item1FrameLayout.findViewById(R.id.step3Item1SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_NOTIFICATION_REQUESTED);
                } else {
                    step3Item1FrameLayout.findViewById(R.id.step3Item1ErrorImageView).setVisibility(View.VISIBLE);
                }

                step3Item1FrameLayout.findViewById(R.id.step3Item1ProgressBar).setVisibility(View.INVISIBLE);
                step3Item2FrameLayout.findViewById(R.id.step3Item2ProgressBar).setVisibility(View.VISIBLE);
            } else if (action.equals(BROADCAST_ACTION_NOTIFICATION_ARRIVED)) {
                if (success) {
                    step3Item2FrameLayout.findViewById(R.id.step3Item2SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_NOTIFICATION_ARRIVED);
                } else {
                    step3Item2FrameLayout.findViewById(R.id.step3Item2ErrorImageView).setVisibility(View.VISIBLE);
                }

                step3Item2FrameLayout.findViewById(R.id.step3Item2ProgressBar).setVisibility(View.INVISIBLE);
                step3Item3FrameLayout.findViewById(R.id.step3Item3ProgressBar).setVisibility(View.VISIBLE);
            } else if (action.equals(BROADCAST_ACTION_NOTIFICATION_SHOWN)) {
                if (success) {
                    step3Item3FrameLayout.findViewById(R.id.step3Item3SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_NOTIFICATION_SHOWN);
                } else {
                    step3Item3FrameLayout.findViewById(R.id.step3Item3ErrorImageView).setVisibility(View.VISIBLE);
                }

                step3Item3FrameLayout.findViewById(R.id.step3Item3ProgressBar).setVisibility(View.INVISIBLE);
                step3Item4FrameLayout.findViewById(R.id.step3Item4ProgressBar).setVisibility(View.VISIBLE);
            } else if (action.equals(BROADCAST_ACTION_NOTIFICATION_SERVICE_STOP)) {
                if (success) {
                    step3Item4FrameLayout.findViewById(R.id.step3Item4SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_NOTIFICATION_SERVICE_STOP);
                } else {
                    step3Item4FrameLayout.findViewById(R.id.step3Item4ErrorImageView).setVisibility(View.VISIBLE);
                }

                step3Item4FrameLayout.findViewById(R.id.step3Item4ProgressBar).setVisibility(View.INVISIBLE);
                step3Item5FrameLayout.findViewById(R.id.step3Item5ProgressBar).setVisibility(View.VISIBLE);

                GCMReceiver.unregisterAsync(getApplicationContext());
            } else if (action.equals(BROADCAST_ACTION_PUSH_UNREGISTER)) {
                if (success) {
                    step3Item5FrameLayout.findViewById(R.id.step3Item5SuccessImageView).setVisibility(View.VISIBLE);
                    step = step | (1 << RESULT_FLAG_NOTIFICATION_UNREGISTER);
                } else {
                    step3Item5FrameLayout.findViewById(R.id.step3Item5ErrorImageView).setVisibility(View.VISIBLE);
                }

                step3Item5FrameLayout.findViewById(R.id.step3Item5ProgressBar).setVisibility(View.INVISIBLE);
            }


            if (step == RESULT_STEP1_SUCCESSFUL) {
                continueButton.setVisibility(View.VISIBLE);
                continueButton.setText(getString(R.string.next));
            } else if (step == RESULT_STEP3_SUCCESSFUL) {
                viewFlipper.showNext();

                toolBar.setBackgroundColor(getResources().getColor(R.color.success));
                getWindow().setStatusBarColor(getResources().getColor(R.color.success_dark));

                toolBarTextView.setText(getString(R.string.success));
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");
        setSupportActionBar(toolBar);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        toolBarTextView = (TextView) findViewById(R.id.toolbarTextView);

        step1Item1FrameLayout = (FrameLayout) findViewById(R.id.step1Item1FrameLayout);
        step1Item2FrameLayout = (FrameLayout) findViewById(R.id.step1Item2FrameLayout);
        step1Item3FrameLayout = (FrameLayout) findViewById(R.id.step1Item3FrameLayout);
        step1Item4FrameLayout = (FrameLayout) findViewById(R.id.step1Item4FrameLayout);

        step3Item1FrameLayout = (FrameLayout) findViewById(R.id.step3Item1FrameLayout);
        step3Item2FrameLayout = (FrameLayout) findViewById(R.id.step3Item2FrameLayout);
        step3Item3FrameLayout = (FrameLayout) findViewById(R.id.step3Item3FrameLayout);
        step3Item4FrameLayout = (FrameLayout) findViewById(R.id.step3Item4FrameLayout);
        step3Item5FrameLayout = (FrameLayout) findViewById(R.id.step3Item5FrameLayout);

        final TextView delayTextView = (TextView) findViewById(R.id.delayTextView);
        final SeekBar delaySeekBar = (SeekBar) findViewById(R.id.delaySeekBar);
        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                delayTextView.setText(getString(R.string.delay) + ": " + progress + " " + getString(R.string.seconds));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (step == 0) {

                    continueButton.setVisibility(View.INVISIBLE);

                    if (PlayServicesUtil.checkPlayServices(getApplicationContext())) {
                        step1Item1FrameLayout.findViewById(R.id.step1Item1SuccessImageView).setVisibility(View.VISIBLE);
                        step = step | (1 << RESULT_FLAG_PLAY_SERVICES);
                    } else {
                        step1Item1FrameLayout.findViewById(R.id.step1Item1ErrorImageView).setVisibility(View.VISIBLE);
                    }

                    if (PlayServicesUtil.checkInternetConnection(getApplicationContext())) {
                        step1Item2FrameLayout.findViewById(R.id.step1Item2SuccessImageView).setVisibility(View.VISIBLE);
                        step = step | (1 << RESULT_FLAG_INTERNET_CONNECTION);
                    } else {
                        step1Item2FrameLayout.findViewById(R.id.step1Item2ErrorImageView).setVisibility(View.VISIBLE);
                    }

                    viewFlipper.showNext();
                    toolBarTextView.setText(getString(R.string.step1));

                    step1Item3FrameLayout.findViewById(R.id.step1Item3ProgressBar).setVisibility(View.VISIBLE);
                    step1Item4FrameLayout.findViewById(R.id.step1Item4ProgressBar).setVisibility(View.VISIBLE);

                    //try to register for GCM and check Server connection as well
                    (new ConnectThread(getApplicationContext())).start();
                    GCMReceiver.registerAsync(getApplicationContext());
                } else if (step == RESULT_STEP1_SUCCESSFUL) {
                    viewFlipper.showNext();
                    toolBarTextView.setText(getString(R.string.step2));
                    continueButton.setText(getString(R.string.request_notification));

                    step = step | (1 << RESULT_FLAG_NOTIFICATION_READY);
                } else if (step == RESULT_STEP2_SUCCESSFUL) {

                    continueButton.setVisibility(View.INVISIBLE);
                    viewFlipper.showNext();
                    toolBarTextView.setText(getString(R.string.step3));

                    step3Item1FrameLayout.findViewById(R.id.step3Item1ProgressBar).setVisibility(View.VISIBLE);

                    (new TriggerNotificationThread(getApplicationContext(), pushId, delaySeekBar.getProgress())).start();
                }
            }
        });


        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_PUSH_REGISTER);
        filter.addAction(BROADCAST_ACTION_PUSH_UNREGISTER);
        filter.addAction(BROADCAST_ACTION_SERVER_CONNECTION);
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_REQUESTED);
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_ARRIVED);
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_SHOWN);
        filter.addAction(BROADCAST_ACTION_NOTIFICATION_SERVICE_STOP);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (step == RESULT_STEP3_SUCCESSFUL)
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getString(R.string.action_about));
            alertDialog.setMessage(getString(R.string.about));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
