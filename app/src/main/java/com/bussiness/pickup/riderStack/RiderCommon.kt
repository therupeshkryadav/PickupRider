import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bussiness.pickup.R
import com.bussiness.pickup.riderStack.riderModel.RiderInfoModel

object RiderCommon {
    val NOTI_BODY: String = "body"
    val NOTI_TITLE: String = "title"

    val TOKEN_REFERENCE: String = "Token"
    var currentUser: RiderInfoModel? = null
    val RIDER_INFO_REFERENCE: String = "RiderInfo"
    val RIDER_LOCATION_REFERENCE: String = "RidersLocation"

    // Use safe calls to avoid NullPointerException
    fun buildWelcomeMessage(): String {
        val firstName = currentUser!!.firstName ?: "User"
        val lastName = currentUser!!.lastName ?: ""
        return StringBuilder("Welcome, ")
            .append(firstName)
            .append(" ")
            .append(lastName)
            .toString()
    }

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent?= null
        if(intent != null)
            pendingIntent= PendingIntent.getActivity(context,id,intent!!,PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "Pickup"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,"Pickup",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "Pickup"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.noti_pickup)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.noti_pickup))

        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent!!)
        val notification = builder.build()
        notificationManager.notify(id,notification)

    }
}
