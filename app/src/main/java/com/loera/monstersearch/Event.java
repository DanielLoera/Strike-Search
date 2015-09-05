package com.loera.monstersearch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 *
 * This is the Parcelable object for Event data
 * (TO BE IMPLEMENTED IN THE FUTURE)
 * :)
 */
public class Event implements Parcelable {

    public String drop;
    public String usedFor;
    public String minionElement;
    public String bossElement;
    public String type;
    public String ability;
    public String bossDifficulty;
    public String gimmicks;
    public String speedClear;
    public String difficulty;
    public String stageImages;
    public String bossImage;
    public String bossMoveset;
    public ArrayList<String> overview;
    public ArrayList<String> recommendations;
    public ArrayList<String> stageData;



    public Event(){}


    protected Event(Parcel in) {
        drop = in.readString();
        usedFor = in.readString();
        minionElement = in.readString();
        bossElement = in.readString();
        type = in.readString();
        ability = in.readString();
        bossDifficulty = in.readString();
        gimmicks = in.readString();
        speedClear = in.readString();
        difficulty = in.readString();
        stageImages = in.readString();
        bossImage = in.readString();
        bossMoveset = in.readString();

        if (in.readByte() == 0x01) {
            overview = new ArrayList<>();
            in.readList(overview, String.class.getClassLoader());
        } else {
            overview = null;
        }if (in.readByte() == 0x01) {
            recommendations = new ArrayList<>();
            in.readList(recommendations, String.class.getClassLoader());
        } else {
            recommendations = null;
        }
        if (in.readByte() == 0x01) {
            stageData = new ArrayList<>();
            in.readList(stageData, String.class.getClassLoader());
        } else {
            stageData = null;
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(drop);
        dest.writeString(usedFor);
        dest.writeString(minionElement);
        dest.writeString(bossElement);
        dest.writeString(type);
        dest.writeString(ability);
        dest.writeString(bossDifficulty);
        dest.writeString(gimmicks);
        dest.writeString(speedClear);
        dest.writeString(difficulty);
        dest.writeString(stageImages);
        dest.writeString(bossImage);
        dest.writeString(bossMoveset);

        if (overview == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(overview);
        }if (recommendations == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(recommendations);
        }
        if (stageData == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(stageData);
        }

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}