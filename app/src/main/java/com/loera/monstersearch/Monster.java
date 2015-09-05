package com.loera.monstersearch;

import android.os.Parcel;
import android.os.Parcelable;

/**
*
*
* This class is the Monster Model.
* this parcelable object lists all the variables a Monster
* could possibly have and change.
*
*
* */

public class Monster implements Parcelable {

    public String num;
    public String name;
    public String monClass;
    public String type;
    public String obtain;
    public String maxLuck;
    public String ability;
    public String impact;
    public String maxLevel;
    public String maxHealth;
    public String maxAttack;
    public String maxSpeed;
    public String plusHealth;
    public String plusAttack;
    public String plusSpeed;
    public String strikeName;
    public String strikeInfo;
    public String cooldown;
    public String bcName;
    public String bcPower;
    public String bcInfo;
    public String link;
    public String ascLinks;
    public String bitmap;
    public String thumb;
    public String evoMat;
    public String ascMat;
    public String event;

    public boolean favorited;


    public Monster() {


    }


    protected Monster(Parcel in) {
        num = in.readString();
        name = in.readString();
        monClass = in.readString();
        type = in.readString();
        obtain = in.readString();
        maxLuck = in.readString();
        ability = in.readString();
        impact = in.readString();
        maxLevel = in.readString();
        maxHealth = in.readString();
        maxAttack = in.readString();
        maxSpeed = in.readString();
        plusHealth = in.readString();
        plusAttack = in.readString();
        plusSpeed = in.readString();
        strikeName = in.readString();
        strikeInfo = in.readString();
        cooldown = in.readString();
        bcName = in.readString();
        bcPower = in.readString();
        bcInfo = in.readString();
        link = in.readString();
        ascLinks = in.readString();
        bitmap = in.readString();
        thumb = in.readString();
        evoMat = in.readString();
        ascMat = in.readString();
        event = in.readString();
        favorited = in.readByte() != 0x00;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {


        dest.writeString(num);
        dest.writeString(name);
        dest.writeString(monClass);
        dest.writeString(type);
        dest.writeString(obtain);
        dest.writeString(maxLuck);
        dest.writeString(ability);
        dest.writeString(impact);
        dest.writeString(maxLevel);
        dest.writeString(maxHealth);
        dest.writeString(maxAttack);
        dest.writeString(maxSpeed);
        dest.writeString(plusHealth);
        dest.writeString(plusAttack);
        dest.writeString(plusSpeed);
        dest.writeString(strikeName);
        dest.writeString(strikeInfo);
        dest.writeString(cooldown);
        dest.writeString(bcName);
        dest.writeString(bcPower);
        dest.writeString(bcInfo);
        dest.writeString(link);
        dest.writeString(ascLinks);
        dest.writeString(bitmap);
        dest.writeString(thumb);
        dest.writeString(evoMat);
        dest.writeString(ascMat);
        dest.writeString(event);
        dest.writeByte((byte) (favorited ? 0x01 : 0x00));

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Monster> CREATOR = new Parcelable.Creator<Monster>() {
        @Override
        public Monster createFromParcel(Parcel in) {
            return new Monster(in);
        }

        @Override
        public Monster[] newArray(int size) {
            return new Monster[size];
        }
    };
}