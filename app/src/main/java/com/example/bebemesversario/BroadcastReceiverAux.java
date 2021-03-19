package com.example.bebemesversario;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.bebemesversario.MainActivity.CHANNEL_ID;

public class BroadcastReceiverAux extends BroadcastReceiver {


    public BroadcastReceiverAux() {

    }

    public void onReceive(Context context, Intent intent) {
        String ID_BEBE = intent.getStringExtra("ID_BEBE");
        String NOME_BEBE = intent.getStringExtra("NOME_BEBE");
        Log.v("TESTE", "Broadcast Notificação id: " + ID_BEBE);
        Log.v("TESTE", "Broadcast Notificação nome: " + NOME_BEBE);

        Intent intentAlbuns = new Intent(context, Albuns.class);
        intentAlbuns.putExtra("EXTRA_BEBE_ID", ID_BEBE);
        intentAlbuns.putExtra("NOME_BEBE", NOME_BEBE);
        intentAlbuns.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentAlbuns, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baby_head)
                .setContentTitle("Mesversario")
                .setContentText("O mesversario do seu bebe é hoje!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}

