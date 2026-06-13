package com.example.penscert;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private TextView tvNotifSubtitle, btnMarkAllRead;
    private RecyclerView rvNotifications;
    private View emptyState;
    private NotificationAdapter adapter;
    private NotificationHelper notifHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNotifSubtitle = view.findViewById(R.id.tvNotifSubtitle);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        emptyState = view.findViewById(R.id.emptyState);

        notifHelper = new NotificationHelper(requireContext());

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>(), new NotificationAdapter.OnNotifClickListener() {
            @Override
            public void onMarkRead(String notifId) {
                notifHelper.markAsRead(notifId);
                refreshList();
            }

            @Override
            public void onClick(Notification notif) {
                notifHelper.markAsRead(notif.id);
                refreshList();
                if (notif.certId != null && !notif.certId.isEmpty()) {
                    Intent intent = new Intent(requireContext(), DocumentDetailActivity.class);
                    intent.putExtra("CERT_ID", notif.certId);
                    startActivity(intent);
                }
            }
        });
        rvNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> {
            notifHelper.markAllAsRead();
            refreshList();
            Toast.makeText(requireContext(), "Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show();
        });

        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notifHelper != null) {
            refreshList();
        }
    }

    private void refreshList() {
        List<Notification> list = notifHelper.getAllNotifications();
        int unread = notifHelper.getUnreadCount();
        tvNotifSubtitle.setText(unread > 0
                ? unread + " notifikasi belum dibaca"
                : "Pusat pemberitahuan dokumen");

        if (list.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.updateData(list);
        }
    }

    // --- Notification Adapter ---
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Notification> list;
        private final OnNotifClickListener listener;

        interface OnNotifClickListener {
            void onMarkRead(String notifId);
            void onClick(Notification notif);
        }

        NotificationAdapter(List<Notification> list, OnNotifClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        void updateData(List<Notification> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification notif = list.get(position);
            NotificationHelper helper = new NotificationHelper(holder.itemView.getContext());

            holder.tvNotifTitle.setText(notif.title);
            holder.tvNotifMessage.setText(notif.message);
            holder.tvNotifTime.setText(helper.getFormattedTime(notif.timestamp));

            // Unread dot
            holder.unreadDot.setVisibility(notif.isRead ? View.GONE : View.VISIBLE);

            // Icon based on type
            int iconRes, bgRes, tintColor;
            String type = notif.type != null ? notif.type : "STATUS_CHANGE";
            switch (type) {
                case "APPROVED":
                    iconRes = R.drawable.ic_check_circle;
                    bgRes = R.drawable.menu_icon_bg_success;
                    tintColor = holder.itemView.getContext().getResources().getColor(R.color.success);
                    break;
                case "REJECTED":
                    iconRes = R.drawable.ic_lock;
                    bgRes = R.drawable.menu_icon_bg_destructive;
                    tintColor = holder.itemView.getContext().getResources().getColor(R.color.destructive);
                    break;
                case "NEW_REQUEST":
                    iconRes = R.drawable.ic_document;
                    bgRes = R.drawable.menu_icon_bg_warning;
                    tintColor = holder.itemView.getContext().getResources().getColor(R.color.warning);
                    break;
                default:
                    iconRes = R.drawable.ic_notification;
                    bgRes = R.drawable.menu_icon_bg_primary;
                    tintColor = holder.itemView.getContext().getResources().getColor(R.color.primary);
                    break;
            }
            holder.iconContainer.setBackgroundResource(bgRes);
            holder.ivNotifIcon.setImageResource(iconRes);
            holder.ivNotifIcon.setColorFilter(tintColor);

            holder.itemView.setOnClickListener(v -> listener.onClick(notif));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNotifTitle, tvNotifMessage, tvNotifTime;
            View unreadDot;
            FrameLayout iconContainer;
            ImageView ivNotifIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
                tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
                tvNotifTime = itemView.findViewById(R.id.tvNotifTime);
                unreadDot = itemView.findViewById(R.id.unreadDot);
                iconContainer = itemView.findViewById(R.id.iconContainer);
                ivNotifIcon = itemView.findViewById(R.id.ivNotifIcon);
            }
        }
    }
}
