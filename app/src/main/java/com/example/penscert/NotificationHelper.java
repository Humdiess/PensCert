package com.example.penscert;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class NotificationHelper {

    private static final String PREF_NAME = "PensCertNotifications";
    private static final String KEY_NOTIFICATIONS = "notifications_list";
    private final SharedPreferences prefs;
    private final Gson gson;

    public NotificationHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addNotification(String title, String message, String type, String certId) {
        List<Notification> list = getAllNotifications();
        Notification notif = new Notification(
                UUID.randomUUID().toString(), title, message, type, certId);
        list.add(0, notif);
        saveAll(list);
    }

    public List<Notification> getAllNotifications() {
        String json = prefs.getString(KEY_NOTIFICATIONS, "[]");
        List<Notification> list = gson.fromJson(json, new TypeToken<List<Notification>>(){}.getType());
        if (list == null) list = new ArrayList<>();
        Collections.sort(list, (a, b) -> {
            try {
                return Long.compare(Long.parseLong(b.timestamp), Long.parseLong(a.timestamp));
            } catch (Exception e) { return 0; }
        });
        return list;
    }

    public int getUnreadCount() {
        int count = 0;
        for (Notification n : getAllNotifications()) {
            if (!n.isRead) count++;
        }
        return count;
    }

    public void markAsRead(String notificationId) {
        List<Notification> list = getAllNotifications();
        for (Notification n : list) {
            if (n.id.equals(notificationId)) {
                n.isRead = true;
                break;
            }
        }
        saveAll(list);
    }

    public void markAllAsRead() {
        List<Notification> list = getAllNotifications();
        for (Notification n : list) {
            n.isRead = true;
        }
        saveAll(list);
    }

    public void clearAll() {
        saveAll(new ArrayList<>());
    }

    private void saveAll(List<Notification> list) {
        prefs.edit().putString(KEY_NOTIFICATIONS, gson.toJson(list)).apply();
    }

    public String getFormattedTime(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            long diff = System.currentTimeMillis() - time;
            if (diff < 60000) return "Baru saja";
            if (diff < 3600000) return (diff / 60000) + " menit lalu";
            if (diff < 86400000) return (diff / 3600000) + " jam lalu";
            return (diff / 86400000) + " hari lalu";
        } catch (Exception e) { return "-"; }
    }
}
