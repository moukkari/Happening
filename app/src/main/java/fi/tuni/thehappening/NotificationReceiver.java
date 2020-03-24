package fi.tuni.thehappening;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

// Creates a unique pending alarm for the task depending on values sent by MainActivity.setAlarm
// The alarm should work also if the app is closed
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String sid = intent.getStringExtra("id");
        int id = Integer.parseInt(intent.getStringExtra("id"));
        String title = intent.getStringExtra("title");

        NotificationManager nM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, MainActivity.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pI = PendingIntent.getActivity(context,
                id,
                repeating_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                "Happening")
                .setContentIntent(pI)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("The Happening")
                .setContentText("Your task named " + title + " is due soon")
                .setAutoCancel(true);

        if (intent.getAction().equals(sid)) {
            Log.d("TAG", "Notification for id: " + id);
            nM.notify(id, builder.build());
        }
    }
}
