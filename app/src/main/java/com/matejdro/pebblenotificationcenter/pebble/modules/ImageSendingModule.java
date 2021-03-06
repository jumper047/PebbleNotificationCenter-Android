package com.matejdro.pebblenotificationcenter.pebble.modules;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.matejdro.pebblecommons.pebble.CommModule;
import com.matejdro.pebblecommons.pebble.PebbleCapabilities;
import com.matejdro.pebblecommons.pebble.PebbleCommunication;
import com.matejdro.pebblecommons.pebble.PebbleImageToolkit;
import com.matejdro.pebblecommons.pebble.PebbleTalkerService;
import com.matejdro.pebblenotificationcenter.NCTalkerService;
import com.matejdro.pebblenotificationcenter.PebbleNotificationCenter;
import com.matejdro.pebblenotificationcenter.ProcessedNotification;

import java.io.ByteArrayOutputStream;

import timber.log.Timber;

/**
 * Created by Matej on 29.11.2014.
 */
public class ImageSendingModule extends CommModule
{
    public static final int MAX_IMAGE_WIDTH = 144;
    public static final int MAX_IMAGE_HEIGHT = 168 - 16;
    public static final int MAX_IMAGE_SIZE = 9000;

    public static final int ICON_SIZE = 30;

    public static int MODULE_IMAGE_SENDING = 5;

    private byte[] imageData;
    private int nextByteToSend = -1;

    public ImageSendingModule(PebbleTalkerService service)
    {
        super(service);
    }

    @Override
    public boolean sendNextMessage()
    {
        if (nextByteToSend != -1)
        {
            sendImagePart();
            return true;
        }

        return false;
    }

    public void sendImagePart()
    {
        Timber.d("SendImagePart %d", nextByteToSend);

        //Substract appmessage overhead from maximum packet size
        // (1 byte for number of entries)
        // (3 entries  - 3x7 bytes)
        // (2 additional bytes (for entries 0 and 1))
        // (1 additional byte for storing size of the image for checksum)
        int maxImageFragmentSize = getService().getPebbleCommunication().getConnectedWatchCapabilities().getMaxAppmessageSize() - 3 * 7 - 2 - 1 - 1;

        int bytesToSend = Math.min(imageData.length - nextByteToSend, maxImageFragmentSize);
        byte[] bytes = new byte[bytesToSend + 1];
        bytes[0] = (byte) ((byte) (nextByteToSend % 256) & 0xFF);

        System.arraycopy(imageData, nextByteToSend, bytes, 1, bytesToSend);

        PebbleDictionary data = new PebbleDictionary();

        data.addUint8(0, (byte) 5);
        data.addUint8(1, (byte) 0);

        data.addBytes(2, bytes);

        getService().getPebbleCommunication().sendToPebble(data);

        nextByteToSend += bytesToSend;
        if (nextByteToSend >= imageData.length)
            nextByteToSend = -1;

    }

    public void startSendingImage(ProcessedNotification notification)
    {
        if (notification == null)
        {
            Timber.w("Invalid notification for image!");
            return;
        }

        imageData = notification.backgroundImageData;
        if (imageData == null)
        {
            Timber.w("Notification has no image!");
            return;
        }

        nextByteToSend = 0;
        getService().getPebbleCommunication().queueModule(this);
        getService().getPebbleCommunication().sendNext();
    }

    public void gotMessageStartSendingImage(PebbleDictionary message)
    {
        PebbleCommunication pebbleCommunication = getService().getPebbleCommunication();
        if (!pebbleCommunication.getConnectedWatchCapabilities().hasColorScreen())
            return;

        int notificationID = message.getInteger(2).intValue();

        ProcessedNotification notification = NCTalkerService.fromPebbleTalkerService(getService()).sentNotifications.get(notificationID);
        startSendingImage(notification);
    }

    public static byte[] prepareImage(Bitmap originalImage)
    {
        if (originalImage == null)
            return null;

        Bitmap image = PebbleImageToolkit.resizePreservingRatio(originalImage, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        image = PebbleImageToolkit.ditherToPebbleTimeColors(image);
        byte[] imageData = PebbleImageToolkit.getIndexedPebbleImageBytes(image);
        if (imageData.length > MAX_IMAGE_SIZE)
        {
            Timber.w("Too big Pebble image: %d bytes! Further resizing...", imageData.length);

            float sizeRatio = (float) MAX_IMAGE_SIZE / imageData.length;
            image = PebbleImageToolkit.resizePreservingRatio(originalImage, (int) (MAX_IMAGE_WIDTH * sizeRatio), (int) (MAX_IMAGE_HEIGHT * sizeRatio));
            image = PebbleImageToolkit.ditherToPebbleTimeColors(image);
            imageData = PebbleImageToolkit.getIndexedPebbleImageBytes(image);
        }

        return imageData;
    }

    public static byte[] prepareTintedIcon(Bitmap originalImage, Context context, PebbleCapabilities capabilities, @ColorInt int iconTint, boolean blackBackground)
    {
        if (originalImage == null)
            return null;

        boolean colorScreen = capabilities.hasColorScreen();

        Bitmap image = PebbleImageToolkit.resizePreservingRatio(originalImage, ICON_SIZE, ICON_SIZE, colorScreen);
        image = PebbleImageToolkit.createMaskFromAlpha(image, iconTint, blackBackground);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (colorScreen)
        {
            image = PebbleImageToolkit.ditherToPebbleTimeColors(image);
            PebbleImageToolkit.writeIndexedPebblePNG(image, outputStream, blackBackground ? Color.BLACK : Color.WHITE);
        }
        else
        {
            image = PebbleImageToolkit.reduceGrayscaleToBlackWhite(image);
            PebbleImageToolkit.writeMaskedTwoBitPng(image, outputStream, blackBackground);

        }

        return outputStream.toByteArray();
    }

    public static byte[] prepareIcon(Bitmap originalImage, Context context, PebbleCapabilities capabilities)
    {
        if (originalImage == null)
            return null;

        boolean whiteImage = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PebbleNotificationCenter.WHITE_NOTIFICATION_TEXT, false);
        return prepareTintedIcon(originalImage, context, capabilities, whiteImage ? Color.WHITE : Color.BLACK, whiteImage);
    }

    @Override
    public void gotMessageFromPebble(PebbleDictionary message)
    {
        int id = message.getUnsignedIntegerAsLong(1).intValue();

        Timber.d("image packet %d", id);

        switch (id)
        {
            case 0: //Pebble opened
                gotMessageStartSendingImage(message);
                break;

        }
    }

    public static ImageSendingModule get(PebbleTalkerService service)
    {
        return (ImageSendingModule) service.getModule(MODULE_IMAGE_SENDING);
    }
}
