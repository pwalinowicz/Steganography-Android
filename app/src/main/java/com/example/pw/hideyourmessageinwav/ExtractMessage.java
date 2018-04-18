package com.example.pw.hideyourmessageinwav;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class ExtractMessage extends AppCompatActivity {

    private int levelsOfDecomposition;
    private String message;
    private String filePath;
    private Wavelet wavelet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract_message);

        try {
            ///OPENING FILE WITH EMBEDDED MESSAGE///
            WavFile embeddedWavFile = WavFile.openWavFile(new File(filePath));

            embeddedWavFile.display();

            double[] completeArrayOfSamples = new double[(int) embeddedWavFile.getNumFrames()];
            int framesRead;
            int lengthOfSignal = completeArrayOfSamples.length;

            ///READING SIGNAL///
            do {
                framesRead = embeddedWavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
            }
            while (framesRead != 0);

            System.out.println("EXCTRACTING");
            System.out.println("EMBEDDED message: " + message);

            ///DECOMPOSING SIGNAL AND EXTRACTING THE MESSAGE///
            StegoEngine stegoEngine = new StegoEngine();
            String extractedMessage = stegoEngine.extractStegoMessageFromSignal(completeArrayOfSamples, levelsOfDecomposition, wavelet);
            System.out.println("EXTRACTED Message: " + extractedMessage);

            Toast.makeText(getApplicationContext(), "EXTRACTED Message: " + extractedMessage, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "EXCTRACTING SUCCESSFUL", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
