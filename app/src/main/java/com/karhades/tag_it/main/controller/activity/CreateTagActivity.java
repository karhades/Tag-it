package com.karhades.tag_it.main.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.CreateTagFragment;
import com.karhades.tag_it.main.model.NfcHandler;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagActivity extends SingleFragmentActivity implements CreateTagFragment.Callbacks {

    private NfcHandler nfcHandler;

    @Override
    protected Fragment createFragment() {
        return CreateTagFragment.newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make content appear behind status bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setupNfcHandler();
    }

    private void setupNfcHandler() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        FragmentManager fragmentManager = getSupportFragmentManager();
        CreateTagFragment currentFragment = (CreateTagFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);

        // Indicates whether the write operation can start.
        boolean isReady = NfcHandler.getWriteMode();
        if (!isReady) {
            currentFragment.makeSnackBar();
        } else {
            nfcHandler.handleNfcWriteTag(intent);
        }
    }

    @Override
    public void setOnTagWriteListener(NfcHandler.OnTagWriteListener onTagWriteListener) {
        nfcHandler.setOnTagWriteListener(onTagWriteListener);
    }
}
