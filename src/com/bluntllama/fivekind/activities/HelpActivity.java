package com.bluntllama.fivekind.activities;

import android.app.Activity;
import android.os.Bundle;

import com.bluntllama.fivekind.R;

/**
 * Created by Matt on 7/26/13.
 */
public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
