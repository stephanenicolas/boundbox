package org.boundbox.sample.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    // -------------------------------
    // ATTRIBUTES
    // -------------------------------

    private Button buttonMain;

    private TextView textViewMain;

    // -------------------------------
    // LIFECYCLE
    // -------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonMain = (Button) findViewById(R.id.button_main);
        textViewMain = (TextView) findViewById(R.id.textview_main);
        buttonMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int result = 42;
                textViewMain.setText(String.valueOf(result));
            }
        });
    }
}
