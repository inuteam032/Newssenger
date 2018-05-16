package com.team032.newssenger;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// Firebase 메시지 수신 (FirebaseMessagingService)
public class MyFcmService extends FirebaseMessagingService {
    private static final String TAG = MyFcmService.class.getSimpleName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "onMessageReceived DATA: " + remoteMessage.getData());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
