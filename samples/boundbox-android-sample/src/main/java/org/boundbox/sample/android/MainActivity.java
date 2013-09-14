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

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonMain = (Button) findViewById(R.id.button_main);
        textViewMain = (TextView) findViewById(R.id.textview_main);
        initializeViews();
    }

    // -------------------------------
    // PRIVATE METHODS
    // -------------------------------

    private void initializeViews() {
        buttonMain.setOnClickListener(new OnComputeClickListener());
    }

    // protected for testing
    protected void clickOnCompute() {
        buttonMain.callOnClick();
    }

    // protected for testing
    protected int getDisplayedResult() {
        return Integer.parseInt((String) textViewMain.getText());
    }

    // -------------------------------
    // INNER CLASSES
    // -------------------------------

    public class OnComputeClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            int result = 42;
            textViewMain.setText(String.valueOf(result));
        }
    }
}
