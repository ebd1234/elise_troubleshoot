package com.example.donor.ui.gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.donor.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class GalleryFragment extends Fragment {

    //declaring variables
    int duration_var, sample_rate_var, interval_var, num_pulses_var;
    int seekbar1_val = 5000;
    int seekbar2_val = 10000;

    Button emitButton;
    Button sinButton;
    Button chirpButton;

    EditText inputDuration;
    EditText inputSampleRate;
    EditText inputInterval;
    EditText inputNumPulses;

    SeekBar frequency1_seekbar;
    TextView frequency1_display;
    SeekBar frequency2_seekbar;
    TextView frequency2_display;

    Boolean waveform = true;

    GraphView graphId;
    LineGraphSeries<DataPoint> series;


    //stuff for requesting permissions
    private static final String LOG_TAG = "donor.ui.gallery";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    //private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    // Requesting permission to RECORD_AUDIO and setting up recording information
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    public ArrayList<Integer> amplitudes = new ArrayList<Integer>(0);
    public ArrayList<Double> time = new ArrayList<Double>(0);
    public Timer timer;
    public Helper task;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) getActivity().finish();;

    }

    // Class to help keep track of recorded waves
    class Helper extends TimerTask
    {
        public double seconds = 0;
        public int i = 0;
        public void run()
        {
            if(recorder != null) {
                amplitudes.add(recorder.getMaxAmplitude());
                time.add(seconds);
                seconds += 0.1;
                Log.d(LOG_TAG, Integer.toString(amplitudes.get(i)));
                i += 1;
            }
        }
    }
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
        timer = new Timer();
        task = new Helper();
        timer.schedule(task, 1, 100);

        Toast.makeText(getActivity(), "Recording", Toast.LENGTH_SHORT).show();
    }
    private void stopRecording() {
        try {
            Thread.sleep(duration_var * num_pulses_var);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        task.cancel();
        timer.purge();
        recorder.stop();
        recorder.release();
        recorder = null;

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    public View onCreateView(@SuppressLint("SetTextI18n") LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //getting permission to record
        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        //initialize fields
        inputDuration = (EditText) root.findViewById(R.id.duration_input);
        inputSampleRate = (EditText) root.findViewById(R.id.sample_rate_input);
        inputInterval = (EditText) root.findViewById(R.id.PRI_input);
        inputNumPulses = (EditText) root.findViewById(R.id.pulses_input);

        emitButton = (Button) root.findViewById(R.id.emitButton);
        sinButton = (Button) root.findViewById(R.id.sin_button);
        sinButton.setTypeface(null, Typeface.BOLD_ITALIC);
        chirpButton = (Button) root.findViewById(R.id.chirp_button);
        chirpButton.setTypeface(null, Typeface.NORMAL);

        frequency1_seekbar = (SeekBar) root.findViewById(R.id.frequency1_seekbar);
        frequency1_display = (TextView) root.findViewById(R.id.frequency1_display);
        frequency2_seekbar = (SeekBar) root.findViewById(R.id.frequency2_seekbar);
        frequency2_display = (TextView) root.findViewById(R.id.frequency2_display);

        graphId = (GraphView) root.findViewById(R.id.graph);

        graphFunction(seekbar1_val, seekbar2_val);

        //frequency 1 updater
        frequency1_display.setText("Enter Frequency One: " + frequency1_seekbar.getProgress() + " Hz");
        frequency1_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbar1_val = progress;
                frequency1_display.setText("Enter Frequency One: " + seekbar1_val + " Hz");
                graphFunction(seekbar1_val, seekbar2_val);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frequency1_display.setText("Enter Frequency One: " + seekbar1_val + " Hz");
                graphFunction(seekbar1_val, seekbar2_val);
            }
        });

        //frequency 2 updater
        frequency2_display.setText("Enter Frequency Two: " +frequency2_seekbar.getProgress() + " Hz");
        frequency2_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbar2_val = progress;
                frequency2_display.setText("Enter Frequency Two: " + seekbar2_val + " Hz");
                graphFunction(seekbar1_val, seekbar2_val);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frequency2_display.setText("Enter Frequency Two: " + seekbar2_val + " Hz");
                graphFunction(seekbar1_val, seekbar2_val);
            }
        });

        emitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                duration_var = Integer.parseInt(inputDuration.getText().toString());
                sample_rate_var = Integer.parseInt(inputSampleRate.getText().toString());
                interval_var = Integer.parseInt(inputInterval.getText().toString());
                num_pulses_var = Integer.parseInt(inputNumPulses.getText().toString());

                if (waveform) {
                    playSin(seekbar1_val, duration_var, sample_rate_var, interval_var, num_pulses_var);
                }
                else {
                    playChirp(seekbar1_val, seekbar2_val, duration_var, sample_rate_var, interval_var, num_pulses_var);
                }
            }
        });

        sinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frequency2_seekbar.setVisibility(View.INVISIBLE);
                frequency2_display.setVisibility(View.INVISIBLE);
                waveform = true;
                updateButton(waveform);
                graphFunction(seekbar1_val, seekbar2_val);
            }
        });

        chirpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frequency2_seekbar.setVisibility(View.VISIBLE);
                frequency2_display.setVisibility(View.VISIBLE);
                waveform = false;
                updateButton(waveform);
                graphFunction(seekbar1_val, seekbar2_val);
            }
        });

        return root;
    }

    private void playSin(int seekbar1_val, int duration_variable, int sample_rate_variable, int interval_variable, int num_pulses_variable) {
        startRecording();
        // variables
        final double freqOfTone = seekbar1_val; // ex 440 hz
        final double totalPulses = num_pulses_variable;

        final double durationSound;
        durationSound = duration_variable/1000.0;
        final int sampleRate = sample_rate_variable; // ex 8000
        final int soundSamples = (int) (durationSound * sampleRate);

        final double durationEmpty = (interval_variable/1000.0) - durationSound;
        final int emptySamples = (int) (durationEmpty * sampleRate);

        final int totalSamples = emptySamples + soundSamples;
        final double[] sample = new double[totalSamples];

        final byte[] generatedSnd = new byte[2 * totalSamples];

        final Handler handler = new Handler();

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                getTone();
                handler.post(new Runnable() {
                    public void run() {
                        playSound();
                    }
                });
            }

            void getTone () {
                // fill out the array
                for (int i = 0; i < totalSamples; ++i) {
                    if (i < soundSamples) {
                        sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
                    }
                    else{
                        sample[i] = 0;
                    }
                }

                // convert to 16 bit pcm sound array
                int index = 0;
                for (final double dVal : sample) {
                    // scale to maximum amplitude
                    final short sVal = (short) ((dVal * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSnd[index++] = (byte) (sVal & 0x00ff);
                    generatedSnd[index++] = (byte) ((sVal & 0xff00) >>> 8);

                }
            }

            void playSound () {
                startRecording();
                final int[] pulseVar = {0};
                final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                        AudioTrack.MODE_STATIC);
                audioTrack.write(generatedSnd, 0, generatedSnd.length);

                audioTrack.play();

                audioTrack.setPositionNotificationPeriod(totalSamples);
                audioTrack.setNotificationMarkerPosition(totalSamples);

                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

                    @Override
                    public void onMarkerReached(AudioTrack arg0) {
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack arg0) {
                        if (pulseVar[0] < totalPulses-1) {
                            audioTrack.stop();
                            audioTrack.play();
                            pulseVar[0]++;
                        }
                        else{
                            audioTrack.stop();
                        }
                    }
                });
            }
        });
        thread.start();
        //stoprecording thread
        final Thread recording = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(duration_var * num_pulses_var);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopRecording();
                Log.d(LOG_TAG, "done");
            }
        });
        recording.start();
    }

    private void playChirp(int seekbar1_val, int seekbar2_val, int duration_variable, int sample_rate_variable, int interval_variable, int num_pulses_variable) {
        startRecording();
        // variables
        final double totalPulsesVar = num_pulses_variable;
        final double freq1=seekbar1_val;
        final double freq2=seekbar2_val;

        final double durationSound;
        durationSound=duration_variable/1000.0;
        final int sampleRate=sample_rate_variable;
        final int soundSamples = (int) (durationSound * sampleRate);

        final double durationEmpty = (interval_variable/1000.0) - durationSound;
        final int emptySamples = (int) (durationEmpty * sampleRate);

        final int totalSamples = emptySamples + soundSamples;

        final double[] sample =new double[totalSamples];
        final byte[] generatedSnd= new byte[2*totalSamples];

        final Handler handler=new Handler();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                handler.post(new Runnable() {
                    public void run() {
                        playSound();
                    }
                });
            }

            void genTone(){
                double instfreq=0, numerator;
                for (int i=0;i<totalSamples; i++ ) {
                    if (i < soundSamples) {
                        numerator=(double)(i)/(double)soundSamples;
                        instfreq =freq1+(numerator*(freq2-freq1));
                        sample[i]=Math.sin(2*Math.PI*i/(sampleRate/instfreq));
                    } else {
                        sample[i] = 0;
                    }
                }
                int idx = 0;
                for (final double dVal : sample) {
                    // scale to maximum amplitude
                    final short val = (short) ((dVal * 32767)); // max positive sample for signed 16 bit integers is 32767
                    // in 16 bit wave PCM, first byte is the low order byte (pcm: pulse control modulation)
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);


                }
            }

            void playSound(){
                final int[] pulseVar = {0};
                final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                        AudioTrack.MODE_STATIC);
                audioTrack.write(generatedSnd, 0, generatedSnd.length);

                audioTrack.play();

                audioTrack.setPositionNotificationPeriod(totalSamples);
                audioTrack.setNotificationMarkerPosition(totalSamples);

                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

                    @Override
                    public void onMarkerReached(AudioTrack arg0) {
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack arg0) {
                        if (pulseVar[0] < totalPulsesVar-1) {
                            audioTrack.stop();
                            audioTrack.play();
                            pulseVar[0]++;
                        }
                        else{
                            audioTrack.stop();
                        }
                    }
                });
            }

        });
        thread.start();
        //stoprecording thread
        final Thread recording = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(duration_var * num_pulses_var);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopRecording();
                Log.d(LOG_TAG, "done");
            }
        });
        recording.start();

    }

    private void graphFunction(int frequency_1, int frequency_2){

        GraphView graph = (GraphView) graphId;
        graph.removeAllSeries();

        series = new LineGraphSeries<DataPoint>();

        final double sampleRate;
        if (inputSampleRate.getText().toString().matches("")) {
            sampleRate = 96000.0;
        }
        else {
            sampleRate = Double.parseDouble(inputSampleRate.getText().toString());
        }

        final double duration;
        if (inputSampleRate.getText().toString().matches("")) {
            duration = 5;
        }
        else {
            duration = Integer.parseInt(inputDuration.getText().toString());
        }

        final int freq1 = frequency_1;
        final int freq2 = frequency_2;

        final double xMax;
        final double step;

        if (waveform) {
            xMax = 1.0/freq1;
            step = 1.0/sampleRate;

            int samples = (int) Math.ceil(xMax/step);

            double[] x = getArray(xMax, step);
            double[] y = new double[samples];

            for (int i = 0; i < x.length; i++) {
                y[i] = Math.sin(2 * Math.PI * freq1 * x[i]);

                series.appendData(new DataPoint(x[i], y[i]), true, x.length);
            }
        }

        else {
            xMax = duration/1000.0;
            step = 1.0/sampleRate;

            int samples = (int) Math.ceil(xMax/step);

            double[] x = getArray(xMax, step);
            double[] y = new double[samples];

            for (int i = 0; i < x.length; i++) {
                y[i] = Math.cos(Math.PI * (((freq2-freq1)/xMax) * x[i] * x[i] + (2 * freq1 * x[i])));

                series.appendData(new DataPoint(x[i], y[i]), true, x.length);
            }
        }

        graph.addSeries(series);

        graph.getViewport().setMaxX(xMax);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMinY(-1);
        graph.getViewport().setMaxY(1);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
    }

    public double[] getArray (double stop, double step)
    {
        int samples = (int) Math.ceil(stop/step);
        double[] result = new double[samples];
        double val = 0.0;

        for(int i=0;i<samples;i++) {
            result[i] = val;
            val = val + step;
        }

        return result;
    }

    private void updateButton (boolean waveform){
        if (waveform){
            sinButton.setTypeface(null, Typeface.BOLD_ITALIC);
            sinButton.setBackgroundColor(Color.parseColor("#EAAA00"));
            chirpButton.setTypeface(null, Typeface.NORMAL);
            chirpButton.setBackgroundColor(Color.parseColor("#F5D580"));
        }
        else {
            sinButton.setTypeface(null, Typeface.NORMAL);
            sinButton.setBackgroundColor(Color.parseColor("#F5D580"));
            chirpButton.setTypeface(null, Typeface.BOLD_ITALIC);
            chirpButton.setBackgroundColor(Color.parseColor("#EAAA00"));
        }
    }
}
