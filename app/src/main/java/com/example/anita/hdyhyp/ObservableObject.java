package com.example.anita.hdyhyp;

import android.app.PendingIntent;
import android.util.Log;

import java.util.Observable;


public class ObservableObject extends Observable {
    private static ObservableObject instance = new ObservableObject();

    private boolean isOrientationPortrait = true;
    //private int pendingIntentRequestID;


    public static ObservableObject getInstance() {
        return instance;
    }

    private ObservableObject() {
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

    public boolean isOrientationPortrait() {
        return isOrientationPortrait;
    }

    public void setOrientationPortrait(boolean orientationPortrait) {
        isOrientationPortrait = orientationPortrait;
    }

    /*public void setPendingIntentRequestID(int pendingIntentRequestID) {
        this.pendingIntentRequestID = pendingIntentRequestID;
    }

    public int getPendingIntentRequestID() {
        return pendingIntentRequestID;
    }*/
}