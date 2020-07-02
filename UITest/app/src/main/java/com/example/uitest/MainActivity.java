package com.example.uitest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    //declaring variables
    int duration_var, sample_rate_var, interval_var, num_pulses_var;
    int seekbar1_val = 5000;
    int seekbar2_val = 10000;

    RadioGroup radioGroup;
    RadioButton radioButton1;
    RadioButton radioButton2;

    Button emitButton;

    EditText inputDuration;
    EditText inputSampleRate;
    EditText inputInterval;
    EditText inputNumPulses;

    SeekBar frequency1_seekbar;
    TextView frequency1_display;
    SeekBar frequency2_seekbar;
    TextView frequency2_display;

    LineGraphSeries<DataPoint> series;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize fields
        inputDuration = (EditText) findViewById(R.id.duration_input);
        inputSampleRate = (EditText) findViewById(R.id.sample_rate_input);
        inputInterval = (EditText) findViewById(R.id.PRI_input);
        inputNumPulses = (EditText) findViewById(R.id.pulses_input);

        emitButton = (Button) findViewById(R.id.emitButton);

        frequency1_seekbar = (SeekBar) findViewById(R.id.frequency1_seekbar);
        frequency1_display = (TextView) findViewById(R.id.frequency1_display);
        frequency2_seekbar = (SeekBar) findViewById(R.id.frequency2_seekbar);
        frequency2_display = (TextView) findViewById(R.id.frequency2_display);

        radioGroup = findViewById(R.id.radioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.radio_one);
        radioButton2 = (RadioButton) findViewById(R.id.radio_two);


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

        // on click function
        emitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                duration_var = Integer.parseInt(inputDuration.getText().toString());
                sample_rate_var = Integer.parseInt(inputSampleRate.getText().toString());
                interval_var = Integer.parseInt(inputInterval.getText().toString());
                num_pulses_var = Integer.parseInt(inputNumPulses.getText().toString());

                if (radioButton1.isChecked()) {
                    playSin(seekbar1_val, duration_var, sample_rate_var, interval_var, num_pulses_var);
                }
                else if (radioButton2.isChecked()) {
                    playChirp(seekbar1_val, seekbar2_val, duration_var, sample_rate_var, interval_var, num_pulses_var);
                }
            }
        });
    }

    private void playSin(int seekbar1_val, int duration_variable, int sample_rate_variable, int interval_variable, int num_pulses_variable) {

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
    }

    private void playChirp(int seekbar1_val, int seekbar2_val, int duration_variable, int sample_rate_variable, int interval_variable, int num_pulses_variable) {

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

    }

    private void graphFunction(int frequency_1, int frequency_2){
        final int freq1 = frequency_1;
        final int freq2 = frequency_2;
        final double sampleRate;
        if(inputSampleRate.getText().toString().matches(""))
        {
            sampleRate = 44000.0;
        }else {
            sampleRate = Double.parseDouble(inputSampleRate.getText().toString());
        }


        if (radioButton1.isChecked()) {
            double y,x;
            x = 0;
            double inc = .05;
            int samples = 2000;

            GraphView graph = (GraphView) findViewById(R.id.graph);
            graph.removeAllSeries();

            series = new LineGraphSeries<DataPoint>();

            for (int i = 0; i < samples; i++){
                x = x + inc;
                y = Math.sin(2 * Math.PI * i / (sampleRate/freq1));
                series.appendData(new DataPoint(x, y), true, samples);
            }
            graph.addSeries(series);

            if (freq1 < 2000){
                graph.getViewport().setMaxX(100);
            }
            else if (freq1 < 10000) {
                graph.getViewport().setMaxX(25);
            }
            else {
                graph.getViewport().setMaxX(10);
            }
            graph.getViewport().setMinX(0);
            graph.getViewport().setMinY(-1);
            graph.getViewport().setMaxY(1);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setXAxisBoundsManual(true);

        }

        else if (radioButton2.isChecked()) {
            double instfreq=0, numerator;
            double y,x;
            x = 0;
            double inc = .1;
            int samples = 2000;

            GraphView graph = (GraphView) findViewById(R.id.graph);
            graph.removeAllSeries();

            series = new LineGraphSeries<DataPoint>();
            for (int i = 0; i < samples; i++){
                x = x + inc;

                numerator=(double)(i)/sampleRate;
                instfreq =freq1+(numerator*(freq2-freq1));
                y=Math.sin(2*Math.PI*i/(sampleRate/instfreq));

                series.appendData(new DataPoint(x, y), true, samples);
            }
            graph.addSeries(series);

            if (freq1 < 3000){
                graph.getViewport().setMaxX(100);
            }
            else if (freq1 < 10000) {
                graph.getViewport().setMaxX(50);
            }
            else {
                graph.getViewport().setMaxX(35);
            }

            graph.getViewport().setMinX(0);
            graph.getViewport().setMinY(-1);
            graph.getViewport().setMaxY(1);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void checkButton(View v) {
        if (radioButton1.isChecked()) {
            frequency2_seekbar.setVisibility(View.INVISIBLE);
            frequency2_display.setVisibility(View.INVISIBLE);
            graphFunction(seekbar1_val, seekbar2_val);
        }
        else if (radioButton2.isChecked()) {
            frequency2_seekbar.setVisibility(View.VISIBLE);
            frequency2_display.setVisibility(View.VISIBLE);
            graphFunction(seekbar1_val, seekbar2_val);
        }
    }
}