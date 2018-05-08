package com.example.pw.hideyourmessageinwav;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.regex.Pattern;

public class HideMessage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int levelsOfDecomposition;
    private String message;
    private String filePath;
    private String newFilePath;
    private Wavelet wavelet;
    EditText et;
    private static final String[] spinnerLevelValues = new String[]{"1", "2", "3", "4"};
    private static final String[] waveletTypes = new String[]{"Haar", "Daubechies D4"};


    public static Wavelet createWaveletFromType(Wavelet.WaveletType type) {
        Wavelet wavelet;

        switch (type) {
            case HaarWavelet:
                wavelet = new HaarWavelet();
                break;

            case DaubechiesD4Wavelet:
                wavelet = new DaubechiesD4Wavelet();
                break;
            default:
                wavelet = new Wavelet();
                break;
        }

        return wavelet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_message);

        /// INPUT ///
        ArrayAdapter levelsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLevelValues);
        Spinner decompLevel = (Spinner) findViewById(R.id.decompLevelSpinner);
        decompLevel.setAdapter(levelsAdapter);
        decompLevel.setOnItemSelectedListener(this);

        ArrayAdapter waveletsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waveletTypes);
        Spinner waveletTypes = (Spinner) findViewById(R.id.waveletTypesSpinner);
        waveletTypes.setAdapter(waveletsAdapter);
        waveletTypes.setOnItemSelectedListener(this);

        et = (EditText) findViewById(R.id.textToHide);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                message = et.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button buttonSearch = (Button) findViewById(R.id.buttonSearchFile);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(HideMessage.this)
                        .withFilter(Pattern.compile(".*\\.wav$"))
                        .withRequestCode(1000)
                        .withHiddenFiles(true)
                        .start();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (adapterView.getId()) {
            case R.id.waveletTypesSpinner:
                String stringWaveletType = String.valueOf(waveletTypes[i]);
                switch (stringWaveletType) {
                    case "Haar":
                        wavelet = createWaveletFromType(Wavelet.WaveletType.HaarWavelet);
                        break;
                    case "Daubechies D4":
                        wavelet = createWaveletFromType(Wavelet.WaveletType.DaubechiesD4Wavelet);
                        break;
                    default:
                        wavelet = null;
                        break;
                }
                break;
            case R.id.decompLevelSpinner:
                levelsOfDecomposition = Integer.valueOf(spinnerLevelValues[i]);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        }
    }

    private boolean isMessageLongEnoughForFile() {

        boolean result = true;

        try {
            WavFile wavFile = WavFile.openWavFile(new File(filePath));
            double sampleCount = wavFile.getNumFrames();

            if (message.length() > sampleCount/Math.pow(2,levelsOfDecomposition) ) {
                result = false;
            }

        } catch (Exception e) {
            System.err.println(e);
        }

        return result;
    }

    public void embedMessage(View v) {

        if (filePath != null && message != null && isMessageLongEnoughForFile()) {

            final int index = filePath.lastIndexOf("/");
            final String absolutePath = filePath.substring(0, index + 1);

            AlertDialog.Builder fileNameAlert = new AlertDialog.Builder(this);

            fileNameAlert.setMessage("Name of the new .wav file:");

            final EditText inputFileName = new EditText(this);
            fileNameAlert.setView(inputFileName);

            fileNameAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    newFilePath = absolutePath + inputFileName.getText().toString() + ".wav";

                    new LongOperation().execute();
                    Toast.makeText(getApplicationContext(), "MESSAGE EMBEDDED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                }
            });

            fileNameAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            fileNameAlert.show();

        } else {
            if (filePath == null) {
                Toast.makeText(getApplicationContext(), "File not chosen! Please search for .wav file", Toast.LENGTH_SHORT).show();
            }else if (message == null) {
                Toast.makeText(getApplicationContext(), "No message was written to be embedded! Please write your message", Toast.LENGTH_SHORT).show();
            } else if (!isMessageLongEnoughForFile()){
                Toast.makeText(getApplicationContext(), "Message is too long to be embedded! Please shorten your message or provide longer .wav file", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private class LongOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                ///OPEN FILE///
                WavFile wavFile = WavFile.openWavFile(new File(filePath));

                double[] completeArrayOfSamples = new double[(int) wavFile.getNumFrames()];
                int framesRead;
                int lengthOfSignal = completeArrayOfSamples.length;

                ///READING SIGNAL///
                do {
                    framesRead = wavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
                }
                while (framesRead != 0);

                StegoEngine stegoEngine = new StegoEngine();

                ///EMBEDDING///
                stegoEngine.embedStegoMessageInSignal(completeArrayOfSamples, levelsOfDecomposition, message, wavelet);

//                newFilePath = filePath.substring(0, filePath.length() - 4);
//                newFilePath = newFilePath + "embedded.wav";

                ///SAVING FILE///
                WavFile newWavFile = WavFile.newWavFile(new File(newFilePath), wavFile.getNumChannels(), wavFile.getNumFrames(), wavFile.getValidBits(), wavFile.getSampleRate());
                newWavFile.writeFrames(completeArrayOfSamples, (int) wavFile.getNumFrames());

                wavFile.close();
                newWavFile.close();

            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }


    }


}
