package com.example.pw.hideyourmessageinwav;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.regex.Pattern;

public class ExtractMessage extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private int levelsOfDecomposition;
    private String extractedMessage;
    private String filePath;
    private Wavelet wavelet;
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
        setContentView(R.layout.activity_extract_message);


        /// INPUT ///
        ArrayAdapter levelsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLevelValues);
        Spinner decompLevel = (Spinner) findViewById(R.id.decompLevelSpinner);
        decompLevel.setAdapter(levelsAdapter);
        decompLevel.setOnItemSelectedListener(this);

        ArrayAdapter waveletsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waveletTypes);
        Spinner waveletTypes = (Spinner) findViewById(R.id.waveletTypesSpinner);
        waveletTypes.setAdapter(waveletsAdapter);
        waveletTypes.setOnItemSelectedListener(this);

        Button buttonSearch = (Button) findViewById(R.id.buttonSearchFile);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(ExtractMessage.this)
                        .withFilter(Pattern.compile(".*\\.wav$"))
                        .withRequestCode(1000)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });
    }

    public void extractMessage(View v) {

       if (filePath == null) {
           Toast.makeText(getApplicationContext(), "File not chosen! Please search for .wav file", Toast.LENGTH_SHORT).show();
       } else {
           new LongOperation().execute();
       }

    }

    private class LongOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                ///OPENING FILE WITH EMBEDDED MESSAGE///
                WavFile embeddedWavFile = WavFile.openWavFile(new File(filePath));

                double[] completeArrayOfSamples = new double[(int) embeddedWavFile.getNumFrames()];
                int framesRead;
                int lengthOfSignal = completeArrayOfSamples.length;

                ///READING SIGNAL///
                do {
                    framesRead = embeddedWavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
                }
                while (framesRead != 0);

                ///DECOMPOSING SIGNAL AND EXTRACTING THE MESSAGE///
                StegoEngine stegoEngine = new StegoEngine();
                extractedMessage = stegoEngine.extractStegoMessageFromSignal(completeArrayOfSamples, levelsOfDecomposition, wavelet);

                embeddedWavFile.close();

            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent i = new Intent(ExtractMessage.this, ShowExtractedMessage.class);
            i.putExtra("msg",extractedMessage);
            startActivity(i);
        }
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

}
