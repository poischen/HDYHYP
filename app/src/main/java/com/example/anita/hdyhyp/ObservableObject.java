package com.example.anita.hdyhyp;

import java.util.Observable;


public class ObservableObject extends Observable {
    private static ObservableObject instance = new ObservableObject();

    private boolean isScreenOn;


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

    public boolean getIsScreenOn(){
        return isScreenOn;
    }

    public void setIsScreenOn(boolean isScreenOn){
        this.isScreenOn = isScreenOn;
    }

}