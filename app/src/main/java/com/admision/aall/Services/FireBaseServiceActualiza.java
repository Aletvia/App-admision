package com.admision.aall.Services;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by AletviaAnaid on 13/11/2017.
 */

public class FireBaseServiceActualiza extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String actualizar = remoteMessage.getData().get("Actualizar");//guarda nombre del archivo a actualizar envia
        String mensaje = remoteMessage.getData().get("Mensaje");//guarda nombre del archivo a actualizar envia
        Mensaje(actualizar, mensaje);
    }

    private void Mensaje(String actuali, String mensaje){
        Intent i = new Intent("my-event");
        i.putExtra("actualizar",actuali);
        i.putExtra("mensaje",mensaje);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
    }
}
