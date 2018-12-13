package com.example.user_pc.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Data {
}

class WeatherData implements Parcelable {
    private String message;
    private int hour;
    private int minute;

    public WeatherData() { }

    public WeatherData(Parcel in) {
        readFromParcel(in);
    }

    public WeatherData(String message, int hour, int minute) {
        this.message = message;
        this.hour = hour;
        this.minute = minute;
    }

    public static final Creator<WeatherData> CREATOR = new Creator<WeatherData>() {
        public WeatherData createFromParcel(Parcel in) {
            return new WeatherData(in);
        }

        public WeatherData[] newArray (int size) {
            return new WeatherData[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public String getMessage() {
        return message;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.message);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
    }
    void readFromParcel(Parcel in) {
        message = in.readString();
        hour = in.readInt();
        minute = in.readInt();
    }
}