package com.example.pw.hideyourmessageinwav;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowExtractedMessage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted__message);

        TextView messageTextView = (TextView) findViewById(R.id.extractedMessage);

        Bundle extras = getIntent().getExtras();
        String extractedMessage = extras.getString("msg");

        messageTextView.setText(messageTextView.getText() + " " + extractedMessage);
    }
}
