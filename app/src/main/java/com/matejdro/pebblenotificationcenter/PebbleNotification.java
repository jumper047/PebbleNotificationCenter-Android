package com.matejdro.pebblenotificationcenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.matejdro.pebblenotificationcenter.appsetting.AppSettingStorage;
import com.matejdro.pebblenotificationcenter.appsetting.SharedPreferencesAppStorage;
import com.matejdro.pebblenotificationcenter.notifications.actions.NotificationAction;
import com.matejdro.pebblenotificationcenter.pebble.NativeNotificationIcon;

import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by Matej on 22.9.2014.
 */
public class PebbleNotification implements Parcelable
{
    private NotificationKey key;
    private String title;
    private String subtitle;
    private String text;
    private boolean dismissable;
    private long postTime;
    private AppSettingStorage settingStorage;
    private ArrayList<NotificationAction> actions;
    private boolean noHistory;
    private boolean forceActionMenu;
    private boolean forceSwitch;
    private boolean listNotification;
    private boolean scrollToEnd;
    private boolean hidingTextDisallowed;
    private String wearGroupKey;
    private int color;
    private Bitmap bigNotificationImage;
    private Bitmap notificationIcon;
    private NativeNotificationIcon nativeNotificationIcon;
    private long[] forcedVibrationPattern;

    public static final int WEAR_GROUP_TYPE_DISABLED = 0;
    public static final int WEAR_GROUP_TYPE_GROUP_MESSAGE = 1;
    public static final int WEAR_GROUP_TYPE_GROUP_SUMMARY = 2;
    private int wearGroupType;

    public PebbleNotification(String title, String text, NotificationKey key)
    {
        this.title = title == null ? "" : title;
        this.text = text == null ? "" : text;
        this.subtitle = "";

        this.key = key;
        postTime = System.currentTimeMillis();
        dismissable = false;
        noHistory = false;
        forceActionMenu = false;
        forceSwitch = false;
        listNotification = false;
        scrollToEnd = false;
        hidingTextDisallowed = false;
        forcedVibrationPattern = null;
        color = Color.TRANSPARENT;

        wearGroupType = WEAR_GROUP_TYPE_DISABLED;
        nativeNotificationIcon = NativeNotificationIcon.NOTIFICATION_GENERIC;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title == null ? "" : title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle == null ? "" : subtitle;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text == null ? "" : text;
    }

    public boolean isDismissable()
    {
        return dismissable;
    }

    public void setDismissable(boolean dismissable)
    {
        this.dismissable = dismissable;
    }

    public long getRawPostTime()
    {
        return postTime;
    }

    public long getPostTime()
    {
        return postTime + TimeZone.getDefault().getOffset(System.currentTimeMillis());
    }


    public void setPostTime(long postTime)
    {
        this.postTime = postTime;
    }

    public AppSettingStorage getSettingStorage(Context context)
    {
        if (settingStorage == null)
        {
            if (key.getPackage() == null)
                settingStorage = PebbleNotificationCenter.getInMemorySettings().getDefaultSettingsStorage();
            else
                settingStorage = new SharedPreferencesAppStorage(context, key.getPackage(), PebbleNotificationCenter.getInMemorySettings().getDefaultSettingsStorage());
        }

        return settingStorage;
    }

    public ArrayList<NotificationAction> getActions()
    {
        return actions;
    }

    public void setActions(ArrayList<NotificationAction> actions)
    {
        this.actions = actions;
    }

    public boolean isHistoryDisabled()
    {
        return noHistory;
    }

    public void setNoHistory(boolean noHistory)
    {
        this.noHistory = noHistory;
    }

    public boolean shouldForceActionMenu()
    {
        return forceActionMenu;
    }

    public void setForceActionMenu(boolean noActions)
    {
        this.forceActionMenu = noActions;
    }

    public boolean isListNotification()
    {
        return listNotification;
    }

    public void setListNotification(boolean listNotification)
    {
        this.listNotification = listNotification;
    }

    public boolean shouldNCForceSwitchToThisNotification()
    {
        return forceSwitch;
    }

    public void setForceSwitch(boolean forceSwitch)
    {
        this.forceSwitch = forceSwitch;
    }

    public String getWearGroupKey()
    {
        return wearGroupKey;
    }

    public void setWearGroupKey(String wearGroupKey)
    {
        this.wearGroupKey = wearGroupKey;
    }

    public int getWearGroupType()
    {
        return wearGroupType;
    }

    public void setWearGroupType(int wearGroupType)
    {
        this.wearGroupType = wearGroupType;
    }

    public boolean shouldScrollToEnd()
    {
        return scrollToEnd;
    }

    public void setScrollToEnd(boolean scrollToEnd)
    {
        this.scrollToEnd = scrollToEnd;
    }

    public void setHidingTextDisallowed(boolean hidingTextDisallowed)
    {
        this.hidingTextDisallowed = hidingTextDisallowed;
    }

    public boolean isHidingTextDisallowed()
    {
        return hidingTextDisallowed;
    }

    public NotificationKey getKey()
    {
        return key;
    }

    public void setKey(NotificationKey key)
    {
        this.key = key;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public @Nullable Bitmap getBigNotificationImage()
    {
        return bigNotificationImage;
    }

    public void setBigNotificationImage(@Nullable Bitmap bigNotificationImage)
    {
        this.bigNotificationImage = bigNotificationImage;
    }

    public Bitmap getNotificationIcon()
    {
        return notificationIcon;
    }

    public void setNotificationIcon(Bitmap notificationIcon)
    {
        this.notificationIcon = notificationIcon;
    }

    public NativeNotificationIcon getNativeNotificationIcon()
    {
        return nativeNotificationIcon;
    }

    public void setNativeNotificationIcon(@NonNull NativeNotificationIcon nativeNotificationIcon)
    {
        this.nativeNotificationIcon = nativeNotificationIcon;
    }

    public long[] getForcedVibrationPattern()
    {
        return forcedVibrationPattern;
    }

    public void setForcedVibrationPattern(long[] forcedVibrationPattern)
    {
        this.forcedVibrationPattern = forcedVibrationPattern;
    }

    public boolean isInSameGroup(PebbleNotification comparing)
    {
        if (getKey().getPackage() == null || !getKey().getPackage().equals(comparing.getKey().getPackage()))
            return false;

        if (getWearGroupType() == WEAR_GROUP_TYPE_DISABLED || comparing.getWearGroupType() == WEAR_GROUP_TYPE_DISABLED)
            return false;

        return getWearGroupKey().equals(comparing.getWearGroupKey());
    }

    public boolean hasIdenticalContent(PebbleNotification comparing)
    {
        return key.getPackage() != null && key.getPackage().equals(comparing.key.getPackage()) && comparing.text.equals(text) && comparing.title.equals(title) && comparing.subtitle.equals(subtitle);
    }

    public boolean isSameNotification(NotificationKey comparing)
    {
        return key.equals(comparing);
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeValue(title);
        parcel.writeValue(text);
        parcel.writeValue(key);
        parcel.writeByte((byte) (dismissable ? 1 : 0));
        parcel.writeByte((byte) (noHistory ? 1 : 0));
        parcel.writeByte((byte) (forceActionMenu ? 1 : 0));
        parcel.writeByte((byte) (listNotification ? 1 : 0));
        parcel.writeByte((byte) (forceSwitch ? 1 : 0));
        parcel.writeByte((byte) (scrollToEnd ? 1 : 0));
        parcel.writeByte((byte) (hidingTextDisallowed ? 1 : 0));
        parcel.writeLong(postTime);
        parcel.writeValue(subtitle);
        parcel.writeValue(text);
        parcel.writeValue(actions);
        parcel.writeValue(wearGroupKey);
        parcel.writeInt(wearGroupType);
        parcel.writeInt(color);
        parcel.writeValue(bigNotificationImage);
        parcel.writeValue(notificationIcon);
        parcel.writeValue(nativeNotificationIcon);
        parcel.writeValue(forcedVibrationPattern);
    }

    public static final Creator<PebbleNotification> CREATOR = new Creator<PebbleNotification>()
    {
        @Override
        public PebbleNotification createFromParcel(Parcel parcel)
        {
            String title = (String) parcel.readValue(String.class.getClassLoader());
            String text = (String) parcel.readValue(String.class.getClassLoader());
            NotificationKey key = (NotificationKey) parcel.readValue(((Object) this).getClass().getClassLoader());

            PebbleNotification notification = new PebbleNotification(title, text, key);
            notification.dismissable = parcel.readByte() == 1;
            notification.noHistory = parcel.readByte() == 1;
            notification.forceActionMenu = parcel.readByte() == 1;
            notification.listNotification = parcel.readByte() == 1;
            notification.forceSwitch = parcel.readByte() == 1;
            notification.scrollToEnd = parcel.readByte() == 1;
            notification.hidingTextDisallowed = parcel.readByte() == 1;
            notification.postTime = parcel.readLong();
            notification.subtitle = (String) parcel.readValue(((Object) this).getClass().getClassLoader());
            notification.text = (String) parcel.readValue(((Object) this).getClass().getClassLoader());
            notification.actions = (ArrayList) parcel.readValue(((Object) this).getClass().getClassLoader());
            notification.wearGroupKey = (String) parcel.readValue(((Object) this).getClass().getClassLoader());
            notification.wearGroupType = parcel.readInt();
            notification.color = parcel.readInt();
            notification.bigNotificationImage = (Bitmap) parcel.readValue(getClass().getClassLoader());
            notification.notificationIcon = (Bitmap) parcel.readValue(getClass().getClassLoader());
            notification.nativeNotificationIcon = (NativeNotificationIcon) parcel.readValue(getClass().getClassLoader());
            notification.forcedVibrationPattern = (long[]) parcel.readValue(getClass().getClassLoader());

            return notification;
        }

        @Override
        public PebbleNotification[] newArray(int i)
        {
            return new PebbleNotification[i];
        }
    };
}
