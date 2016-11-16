package com.example.anita.hdyhyp;

import android.util.Log;

import java.util.Observable;


public class ObservableObject extends Observable {
    private static ObservableObject instance = new ObservableObject();

   // private boolean isScreenOn = true;
    private boolean isKeyboardVisible;
    private boolean isOrientationPortrait;


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

    /*public boolean getIsScreenOn(){
        return isScreenOn;
    }

    public void setIsScreenOn(boolean isScreenOn){
        isScreenOn = isScreenOn;
    }
*/
    public boolean isKeyboardVisible() {
        return isKeyboardVisible;
    }

    public void setKeyboardVisible(boolean keyboardVisible) {
        isKeyboardVisible = keyboardVisible;
    }

    public boolean isOrientationPortrait() {
        return isOrientationPortrait;
    }

    public void setOrientationPortrait(boolean orientationPortrait) {
        isOrientationPortrait = orientationPortrait;
    }
}