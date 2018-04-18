package com.example.pw.hideyourmessageinwav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

public class HideMessage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int levelsOfDecomposition;
    private String message;
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
        setContentView(R.layout.activity_hide_message);

        try {
            /// INPUT ///

            ArrayAdapter levelsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLevelValues);
            Spinner decompLevel = (Spinner) findViewById(R.id.decompLevelSpinner);
            decompLevel.setAdapter(levelsAdapter);
            decompLevel.setOnItemSelectedListener(this);

            ArrayAdapter waveletsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waveletTypes);
            Spinner waveletTypes = (Spinner) findViewById(R.id.waveletTypesSpinner);
            waveletTypes.setAdapter(waveletsAdapter);
            waveletTypes.setOnItemSelectedListener(this);

            Button buttonOK = (Button) findViewById(R.id.buttonOK);
            buttonOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText et = (EditText) findViewById(R.id.textToHide);
                    message = et.getText().toString();
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    //et.setText("");
                    //et.requestFocus();
                }
            });

            ///OPEN FILE///
            WavFile wavFile = WavFile.openWavFile(new File("d:\\Documents\\Studia\\Inf\\VII+VIII Praca eng\\implemTest\\src\\resources\\0564.wav"));

            ///DISPLAY INFO///
            wavFile.display();

            double[] completeArrayOfSamples = new double[(int) wavFile.getNumFrames()];

            int framesRead;

            int lengthOfSignal = completeArrayOfSamples.length;

            ///READING SIGNAL///
            do {
                framesRead = wavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
            }
            while (framesRead != 0);

            System.out.println("Length of string: " + message.length());

            StegoEngine stegoEngine = new StegoEngine();

            ///EMBEDDING///
            System.out.println("EMBEDDING");
            stegoEngine.embedStegoMessageInSignal(completeArrayOfSamples, levelsOfDecomposition, message, wavelet);


            ///SAVING FILE///
            WavFile newWavFile = WavFile.newWavFile(new File("0564_new.wav"), wavFile.getNumChannels(), wavFile.getNumFrames(), wavFile.getValidBits(), wavFile.getSampleRate());
            newWavFile.writeFrames(completeArrayOfSamples, (int) wavFile.getNumFrames());
            newWavFile.display();

            wavFile.close();
            newWavFile.close();

            ///OPENING FILE WITH EMBEDDED MESSAGE///
            WavFile embeddedWavFile = WavFile.openWavFile(new File("d:\\Documents\\Studia\\Inf\\VII+VIII Praca eng\\implemTest\\0564_new.wav"));

            embeddedWavFile.display();

            completeArrayOfSamples = new double[(int) embeddedWavFile.getNumFrames()];
            lengthOfSignal = completeArrayOfSamples.length;

            ///READING SIGNAL///
            do {
                framesRead = embeddedWavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
            }
            while (framesRead != 0);

            System.out.println("EXCTRACTING");
            System.out.println("EMBEDDED message: " + message);

            ///DECOMPOSING SIGNAL AND EXTRACTING THE MESSAGE///
            System.out.println("EXCRACTED Message: " + stegoEngine.extractStegoMessageFromSignal(completeArrayOfSamples, levelsOfDecomposition, wavelet));

        } catch (Exception e) {
            System.err.println(e);
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
                Toast.makeText(getApplicationContext(), "" + stringWaveletType, Toast.LENGTH_SHORT).show();
                break;
            case R.id.decompLevelSpinner:
                levelsOfDecomposition = Integer.valueOf(spinnerLevelValues[i]);
                Toast.makeText(getApplicationContext(), "" + levelsOfDecomposition, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    public void getMessageFromEditText(View v) {
//
//        EditText et = (EditText) findViewById(R.id.textToHide);
//        message = et.getText().toString();
//
//        if (message == "" || message == null) {
//            Toast.makeText(getApplicationContext(), "No text?", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//        }
//
//    }
}
