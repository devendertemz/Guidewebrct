package com.atmosol.guide;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SimpleSdpObserver implements SdpObserver {
    private static final String TAG = "SimpleSdpObserver";


    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG,"onCreateSuccess"+sessionDescription.toString());
    }


    @Override
    public void onSetSuccess() {
        Log.d(TAG,"onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG,"onCreateFailure"+s);

    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG,"onSetFailure"+s);

    }

}
