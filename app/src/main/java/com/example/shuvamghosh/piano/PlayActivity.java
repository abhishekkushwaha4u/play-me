package com.example.shuvamghosh.piano;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

public class PlayActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {


    private static int BASIC = 20;
    private static int SHARP = 21;
    private static int count = 0;

    /*b,b,cs,b,e,ds,b,b,cs,b,fs,e,b,b,bh,gs,e,ds,cs,a,a,gs,e,f,fs,e
    basic: 0a,1b,2c,3d,4e,5f,6g
    sharp: 0as,1bhs,2cs,3ds,4e,5fs,6gs
     */
    int myToneArrayBasic[] = {1, 1, -1, 1, 4, -1, 1, 1, -1, 1, -1, 4, 1, 1, -1, -1, 4, -1, -1, 0, 0, -1, 4, 5, -1, 4};
    int myToneArraySharp[] = {-1, -1, 2, -1, -1, 3, -1, -1, 2, -1, 5, -1, -1, -1, 1, 6, -1, 3, 2, -1, -1, 6, -1, -1, 5, -1};


    int myToneArrayBasic_sw[] = {6, 6, 6, -1, -1, 6, -1, -1, 6, 3, 3, 3, -1, 6, -1, -1, -1, 6, 6, 6, 6, -1, -1, 6, -1, -1, 6};
    int myToneArraySharp_sw[] = {-1, -1, -1, 3, 0, -1, 3, 0, -1, -1, -1, -1, 3, -1, 5, 3, 0, -1, -1, -1, -1, 3, 0, -1, 3, 0, -1};

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SoundPool soundPool;
    int arrBasic[];
    int arrSharp[];
    int arrBasicSad[];
    int arrSharpSad[];

    boolean loaded = false;
    int k = 0;

    private LinearLayout keyLayout;
    private String myTone;

    private EditText toneEditText;
    private ImageView imageView;
    private Button button, playButton;
    private Button flashImage;
    private int toneInt = 0;
    private TextView tv;
    private RadioButton radioButton;
    private int toneMode = BASIC;
    private String mood, time;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        AppController.getInstance().setTones();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                tv.setText("Started Playing");
                try {
                    JSONObject object = new JSONObject(intent.getStringExtra("message"));
                    mood = object.getString("mood");
                    time = object.getString("time");
                    play();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        toneEditText = (EditText) findViewById(R.id.tone_set);
        button = (Button) findViewById(R.id.set_button);
        button.setOnClickListener(this);
        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        flashImage = (Button) findViewById(R.id.dispButton);
        radioButton = (RadioButton) findViewById(R.id.sadButton);
        tv = (TextView) findViewById(R.id.textView);
        keyLayout = (LinearLayout) findViewById(R.id.key);

        checkDeviceTone();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // ImageView iv=(ImageView)findViewById(R.id.imageView);
        //View view = findViewById(R.id.textView1);
        // view.setOnTouchListener(this);
// Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
// Load the sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });


        TypedArray typedArrayBasic = getResources().obtainTypedArray(R.array.sound_tones_basic_sw);
        TypedArray typedArraySharp = getResources().obtainTypedArray(R.array.sound_tones_sharp_sw);

       /* TypedArray typedArrayBasicSad = getResources().obtainTypedArray(R.array.sound_tones_basic_sad);
        TypedArray typedArraySharpSad = getResources().obtainTypedArray(R.array.sound_tones_sharp_sad);*/

        int[] SOUND_TONES_BASIC_T = new int[7];
        int[] SOUND_TONES_SHARP_T = new int[7];

        int[] SOUND_TONES_BASIC_T_SAD = new int[7];
        int[] SOUND_TONES_SHARP_T_SAD = new int[7];


        for (int i = 0; i < typedArrayBasic.length(); i++) {
            SOUND_TONES_BASIC_T[i] = soundPool.load(this, typedArrayBasic.getResourceId(i, -1), 1);
        }

        for (int i = 0; i < typedArraySharp.length(); i++) {
            SOUND_TONES_SHARP_T[i] = soundPool.load(this, typedArraySharp.getResourceId(i, -1), 1);
        }



        /*for (int i = 0; i < typedArrayBasicSad.length(); i++) {
            SOUND_TONES_BASIC_T_SAD[i] = soundPool.load(this, typedArrayBasic.getResourceId(i, -1), 1);
        }

        for (int i = 0; i < typedArraySharpSad.length(); i++) {
            SOUND_TONES_SHARP_T_SAD[i] = soundPool.load(this, typedArraySharp.getResourceId(i, -1), 1);
        }*/



        /*arrBasicSad = new int[myToneArrayBasic.length];

        for (int i = 0; i < arrBasic.length; i++) {
            if (myToneArrayBasic[i] != -1)
                arrBasicSad[i] = SOUND_TONES_BASIC_T[myToneArrayBasic[i]];
            else {
                arrBasicSad[i] = -1;
            }
        }*/


        arrBasic = new int[myToneArrayBasic_sw.length];

        for (int i = 0; i < arrBasic.length; i++) {
            if (myToneArrayBasic_sw[i] != -1)
                arrBasic[i] = SOUND_TONES_BASIC_T[myToneArrayBasic_sw[i]];
            else {
                arrBasic[i] = -1;
            }
        }




       /* arrSharpSad = new int[myToneArraySharp.length];

        for (int i = 0; i < arrSharp.length; i++) {


            if (myToneArraySharp[i] != -1)
                arrSharpSad[i] = SOUND_TONES_SHARP_T[myToneArraySharp[i]];
            else {
                arrSharpSad[i] = -1;
            }
        }*/


        arrSharp = new int[myToneArraySharp_sw.length];

        for (int i = 0; i < arrSharp.length; i++) {


            if (myToneArraySharp_sw[i] != -1)
                arrSharp[i] = SOUND_TONES_SHARP_T[myToneArraySharp_sw[i]];
            else {
                arrSharp[i] = -1;
            }
        }




        /*soundID5 = soundPool.load(this, R.raw.chord1edit, 1);
        soundID6 = soundPool.load(this, R.raw.chord2edit, 1);
        soundID7 = soundPool.load(this, R.raw.chord3edit, 1);
        soundID8 = soundPool.load(this, R.raw.chord4edit, 1);
        soundID9 = soundPool.load(this, R.raw.chord5edit, 1);
        soundID10 = soundPool.load(this, R.raw.chord7edit, 1);
        soundID11 = soundPool.load(this, R.raw.chord8edit, 1);
        soundID12 = soundPool.load(this, R.raw.chord9edit, 1);
        soundID13 = soundPool.load(this, R.raw.chord10edit, 1);*/


    }


    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.PLAY_NOTIFICATION));
            isReceiverRegistered = true;
        }
    }

    private void checkDeviceTone() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String tone = sharedPreferences.getString("tone", "-1");
        if (tone == "-1") {
            toneEditText.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        } else {
            myTone = tone;
        }

    }

    private void setDeviceTone(String tone) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("tone", tone);
        editor.commit();

        myTone = tone;

        button.setVisibility(View.GONE);
        toneEditText.setVisibility(View.GONE);
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] <= 5) {


            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            float actualVolume = (float) audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume / maxVolume;

            if (flashImage.getText().equals("Press")) {

                Log.d("sensor", String.valueOf(toneInt));

                if (toneMode == PlayActivity.BASIC) {
                    //count++;

                    soundPool.play(arrBasic[toneInt], volume, volume, 1, 0, 1f);


                } else  {
                    count = 0;
                    soundPool.play(arrSharp[toneInt], volume, volume, 1, 0, 1f);

                }
            }
        }
    }

    public void play() {


        if (mood.equals("sadness")) {
            int[] SOUND_TONES_BASIC_T = new int[7];
            int[] SOUND_TONES_SHARP_T = new int[7];

           /* int myToneArrayBasic[] =  {1,  1,-1,  1,  4,-1,  1,  1, -1, 1,-1,  4,  1,  1,  -1,-1,  4,-1,-1,  0,  0,-1,  4,  5, -1, 4};
            int myToneArraySharp[] = {-1,-1, 2, -1, -1, 3, -1, -1, 2, -1, 5, -1, -1, -1,   1, 6, -1, 3, 2, -1, -1, 6, -1, -1, 5, -1};*/

            //TypedArray typedArrayBasic = getResources().obtainTypedArray(R.array.sound_tones_basic_sad);
            //TypedArray typedArraySharp = getResources().obtainTypedArray(R.array.sound_tones_sharp_sad);

            TypedArray typedArrayBasic = getResources().obtainTypedArray(R.array.sound_tones_basic_sw);
            TypedArray typedArraySharp = getResources().obtainTypedArray(R.array.sound_tones_sharp_sw);

            for (int i = 0; i < typedArrayBasic.length(); i++) {
                SOUND_TONES_BASIC_T[i] = soundPool.load(this, typedArrayBasic.getResourceId(i, -1), 1);
            }

            for (int i = 0; i < typedArraySharp.length(); i++) {
                SOUND_TONES_SHARP_T[i] = soundPool.load(this, typedArraySharp.getResourceId(i, -1), 1);
            }

            arrBasic = new int[myToneArrayBasic_sw.length];

            for (int i = 0; i < arrBasic.length; i++) {
                if (myToneArrayBasic_sw[i] != -1)
                    arrBasic[i] = SOUND_TONES_BASIC_T[myToneArrayBasic_sw[i]];
                else {
                    arrBasic[i] = -1;
                }
            }

            arrSharp = new int[myToneArraySharp_sw.length];

            for (int i = 0; i < arrSharp.length; i++) {


                if (myToneArraySharp_sw[i] != -1)
                    arrSharp[i] = SOUND_TONES_SHARP_T[myToneArraySharp_sw[i]];
                else {
                    arrSharp[i] = -1;
                }
            }


        }

        Calendar c = Calendar.getInstance();
        CountDownTimer startDelay = new CountDownTimer((Long.parseLong(time) - System.currentTimeMillis()), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                finallyPlay();
            }
        };
        startDelay.start();


    }

    private void finallyPlay() {
        Log.d("myTone",myTone);
        final int[] start = {0};
        CountDownTimer countDownTimer = new CountDownTimer(21700, 800) {
            @Override
            public void onTick(long millisUntilFinished) {
                count++;
                tv.setText("Count: " + count);
                if (start[0] < arrBasic.length) {
                    //ImageView iv=(ImageView)findViewById(R.id.imageView);
                    Log.d(String.valueOf(start[0]), String.valueOf(arrBasic.length));
                    if (Arrays.binarySearch(myTone.split(","), String.valueOf(myToneArrayBasic_sw[start[0]])) != -1) {

                        Log.d("basic tick", String.valueOf(myToneArrayBasic_sw[start[0]]));
                        //if (myTone.equals(String.valueOf(myToneArrayBasic_sw[start[0]]))) {
                        keyLayout.setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                        toneInt = start[0];
                        toneMode = PlayActivity.BASIC;
                        Log.d("flashImage", String.valueOf(myToneArrayBasic_sw[start[0]]));

                        imageView.setActivated(true);

                        flashImage.setText("Press");

                       /* Toast.makeText(PlayActivity.this, "Press", Toast.LENGTH_SHORT).show();*/

                        Log.d("flashImageV", String.valueOf(flashImage.getVisibility()));


                    } else if (Arrays.binarySearch(myTone.split(","), String.valueOf(myToneArraySharp_sw[start[0]])) != -1) {
                        //} else if (myTone.equals(String.valueOf(myToneArraySharp_sw[start[0]]))) {

                        Log.d("sharp tick", String.valueOf(myToneArraySharp_sw[start[0]]));
                        keyLayout.setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                        toneInt = start[0];
                        toneMode = PlayActivity.SHARP;
                        Log.d("flashImage", String.valueOf(myToneArrayBasic_sw[start[0]]));
                        imageView.setActivated(true);
                        flashImage.setText("Press");
                       /* Toast.makeText(PlayActivity.this, "Press", Toast.LENGTH_SHORT).show();*/

                        Log.d("flashImageV", String.valueOf(flashImage.getVisibility()));


                    } else {

                        keyLayout.setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.black));
                        Log.d("start0 less", String.valueOf(start[0]));
                        imageView.setActivated(false);
                        tv.setText("Wait");
                        flashImage.setText("Dont");
                    }
                    start[0]++;
                } else {

                    keyLayout.setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.black));
                    Log.d("start0 more", String.valueOf(start[0]));
                    imageView.setActivated(false);
                    tv.setText("Wait");
                    flashImage.setText("Dont");
                }
            }

            @Override
            public void onFinish() {
                flashImage.setText("Dont");
                tv.setText("Music Over");
            }
        };
        countDownTimer.start();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_button:
                setDeviceTone(toneEditText.getText().toString());
                break;
            case R.id.play_button:
                play();
                break;
        }
    }
}
