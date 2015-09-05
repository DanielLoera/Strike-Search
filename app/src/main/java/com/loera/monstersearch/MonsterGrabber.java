package com.loera.monstersearch;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This AsyncTask is what searches through a Monster's web page
 * and finds the evolution and ascension monsters, if any.
 * <p/>
 * After all data is gathered, it is passed to the MonsterPageActivity
 * to be displayed.
 */

public class MonsterGrabber extends AsyncTask<Void, Void, Void> {

    String monInput, message;
    ArrayList<Monster> mons;
    ArrayList<String> totalMonsters;
    Monster monsterData;
    Context context;
    Activity parent;
    public static boolean cancelled, running, stop;

    /**
     * This interface is strictly to signal other classes
     * when MonsterGrabber is downloading data and when it is
     * finished.
     */

    public interface LoadingListener {

        public void setLoading(boolean isLoading);

    }

    /*
    *
    * This constructor takes the Monster's number, the application context,
    * and the activity from where it was sent.
    *
    * */

    public MonsterGrabber(String num, Context c, Activity p) {
        this.monInput = num;
        this.context = c;
        this.parent = p;
        cancelled = false;
        running = true;
    }


    @Override
    protected Void doInBackground(Void... params) {

        mons = new ArrayList<>();

        String mainImage = "", thumbnail;

        try {

            //the first connections finds any evolution or ascension
            // monsters and stores their numbers in totalMonsters

            totalMonsters = new ArrayList<>();
            totalMonsters.add(monInput);
            URL url = new URL("http://www.strikeshot.net/" + monInput);
            HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
            connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            connect2.connect();
            BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));

            Log.i("DataGrabber", "looking for evolutions or ascensions");

            String inputLine;

            boolean next = false;
            while ((inputLine = htmlStream.readLine()) != null) {
                if (inputLine.contains("evo-result") && inputLine.contains("href="))
                    totalMonsters.add(getMonUrl(inputLine));
                else if (inputLine.contains("ascen-title-result") && inputLine.contains("href="))
                    totalMonsters.add(getMonUrl(inputLine));
                else if (next) {
                    totalMonsters.add(getMonUrl(inputLine));
                    next = false;
                } else if (inputLine.contains("Base Monster:"))
                    next = true;

            }
            htmlStream.close();

        } catch (Exception ignored) {

            ignored.printStackTrace();
            message = "Network Error\nMonster failed to download";
            cancelled = true;

        }
        //gathering list of all favorited monsters from "Monsters" folder.
        File dir = context.getDir("Monsters", Context.MODE_PRIVATE);
        ArrayList<File> folders = new ArrayList<>(Arrays.asList(dir.listFiles()));

        //This is the main for loop that creates gathers all data for
        //storing into Monster the Monster object "monsterData"

        for (int i = 0; i < totalMonsters.size(); i++) {
            Log.i("DataGrabber", "creating monster " + i);
            monsterData = new Monster();
            monsterData.num = totalMonsters.get(i).substring(totalMonsters.get(i).indexOf("monster/") + 8);
            ArrayList<String> htmlLines = new ArrayList();

            //decides what website to grab the thumbnail image
            // based on the Monsters number
            if (Integer.parseInt(monsterData.num) > 999)
                thumbnail = "http://strikeshot.net/sites/default/files/styles/thumbnail/public/" + monsterData.num + ".jpg";
            else
                thumbnail = "http://www.monsterstrikedatabase.com/monsters/" + monsterData.num + ".jpg";
            monsterData.link = totalMonsters.get(i);
            try {

                //This is the main connection to the Monster's
                // web page.

                URL url = new URL("http://www.strikeshot.net/" + totalMonsters.get(i));
                HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
                connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                connect2.connect();
                BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));
                Log.i("MonsterGrabber", "accessing page");

                String inputLine;

                //This while loop uses the BufferedReader object "htmlStream"
                //to store every HTML line into the ArrayList "htmlLines"

                while ((inputLine = htmlStream.readLine()) != null)
                    htmlLines.add(inputLine);

                htmlStream.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!cancelled) {

                // This for loop scans though all of htmlLines and
                // searches for any matching terms that deal with the Monster's
                // data. Once a match is found, it stores it into the appropriate variable in
                // the Monster object "monsterData"

                for (int m = 0; m < htmlLines.size(); m++) {
                    String curData = htmlLines.get(m);

                    if (curData.contains("monster-image")) {

                        Log.i("DataGrabber", "added image");

                        mainImage = curData.substring(curData.indexOf("src=") + 5, curData.indexOf("?"));

                    }


                    if (curData.contains("monster-title")) {

                        monsterData.name = addBetweenTags(curData, curData.indexOf("-title") + 8);
                        Log.i("DataGrabber", "adding " + monsterData.name);
                        String level = "";
                        switch (monsterData.name.charAt(monsterData.name.length() - 1)) {

                            case '1':
                                level = "5";
                                break;
                            case '2':
                                level = "15";
                                break;
                            case '3':
                                level = "20";
                                break;
                            case '4':
                                level = "40";
                                break;
                            case '5':
                                level = "70";
                                break;
                            case '6':
                                level = "99";
                        }

                        monsterData.maxLevel = level;

                    }


                    if (curData.contains("monster-atrri")) {
                        if (curData.contains("bounce"))
                            monsterData.impact = "Bounce";
                        else
                            monsterData.impact = "Pierce";


                    }

                    if (curData.contains("Class: ")) {
                        Log.i("DataGrabber", "added class");
                        monsterData.monClass = addBetweenTags(curData, curData.indexOf("</span>") + 8);
                    }

                    if (curData.contains("Type: ")) {
                        Log.i("DataGrabber", "added type");
                        monsterData.type = addBetweenTags(curData, curData.indexOf("</span>") + 7);
                    }

                    if (curData.contains("Obtain: ")) {
                        monsterData.obtain = addBetweenTags(curData, curData.indexOf("</span>") + 7);
                        Log.i("DataGrabber", "added obtain");
                    }

                    if (curData.contains("Max Luck :")) {
                        Log.i("DataGrabber", "added luck");
                        monsterData.maxLuck = addBetweenTags(curData, curData.indexOf("</span>") + 7);
                    }

                    if (curData.contains("Ability: ")) {
                        Log.i("DataGrabber", "added ability");
                        monsterData.ability = addBetweenTags(curData, curData.indexOf("</span>") + 7);
                        if (htmlLines.get(m + 1).contains("Gauge Shot"))
                            monsterData.ability = monsterData.ability + "\nGauge:  " + addBetweenTags(htmlLines.get(m + 1), htmlLines.get(m + 1).indexOf("</span>") + 7);
                    }

                    if (curData.contains(">Health<")) {

                        Log.i("DataGrabber", "added health");
                        monsterData.maxHealth = addBetweenTags(htmlLines.get(m + 2), htmlLines.get(m + 2).indexOf(">") + 1);
                        monsterData.plusHealth = addBetweenTags(htmlLines.get(m + 3), htmlLines.get(m + 3).indexOf(">") + 1);


                    }
                    if (curData.contains(">Attack<")) {

                        Log.i("DataGrabber", "added attack");
                        monsterData.maxAttack = addBetweenTags(htmlLines.get(m + 2), htmlLines.get(m + 2).indexOf(">") + 1);
                        monsterData.plusAttack = addBetweenTags(htmlLines.get(m + 3), htmlLines.get(m + 3).indexOf(">") + 1);

                    }
                    if (curData.contains(">Speed<")) {
                        Log.i("DataGrabber", "added speed");
                        monsterData.maxSpeed = addBetweenTags(htmlLines.get(m + 2), htmlLines.get(m + 2).indexOf(">") + 1);
                        monsterData.plusSpeed = addBetweenTags(htmlLines.get(m + 3), htmlLines.get(m + 3).indexOf(">") + 1);


                    }


                    if (curData.contains("strike-shot-title")) {

                        Log.i("DataGrabber", "added strike shot");

                        monsterData.strikeName = addBetweenTags(curData, curData.indexOf(" - ") + 3);

                        monsterData.strikeInfo = addBetweenTags(htmlLines.get(m + 1), htmlLines.get(m + 1).indexOf(">") + 1);

                        monsterData.cooldown = addBetweenTags(htmlLines.get(m + 2), htmlLines.get(m + 2).indexOf("<span>") + 6);

                    }

                    if (curData.contains("bumper-combo-title")) {
                        Log.i("DataGrabber", "added bump combo");
                        monsterData.bcName = addBetweenTags(curData, curData.indexOf(" - ") + 3);

                        monsterData.bcInfo = addBetweenTags(htmlLines.get(m + 1), htmlLines.get(m + 1).indexOf(">") + 1);

                        monsterData.bcPower = addBetweenTags(htmlLines.get(m + 2), htmlLines.get(m + 2).indexOf("<span>") + 6);


                    }

                    if (curData.contains("monster-quest-top-image")) {

                        monsterData.event = "";

                        String chunk = curData.substring(curData.indexOf("href=") + 6);
                        chunk = chunk.substring(0, chunk.indexOf(">") - 1);
                        monsterData.event += chunk + "END";
                        if (monsterData.event.contains("event")) {
                            chunk = curData.substring(curData.indexOf("src=") + 5);
                            chunk = chunk.substring(0, chunk.indexOf("\""));


                            try {
                                HttpURLConnection bannerImg = (HttpURLConnection) new URL(chunk).openConnection();
                                bannerImg.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                                bannerImg.connect();
                                InputStream stream = bannerImg.getInputStream();
                                Bitmap bannerBitmap = BitmapFactory.decodeStream(stream);

                                File internal = context.getDir("temp", Context.MODE_PRIVATE);
                                File banner = new File(internal, monsterData.num + "banner.png");
                                FileOutputStream out = new FileOutputStream(banner);
                                bannerBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                chunk = banner.getPath();


                            } catch (Exception ex) {
                                ex.printStackTrace();

                            }

                            monsterData.event += chunk;

                            Log.i("DataGrabber", "Event data  = " + monsterData.event);

                        }

                    }

                    if (curData.contains("evo-material")) {

                        String look;

                        if (!curData.contains("</span>"))
                            look = "</p>";
                        else
                            look = "</span>";

                        curData = curData.substring(0, curData.indexOf(look));
                        int begin = 0;

                        for (int j = curData.length() - 1; j >= 0; j--) {

                            if (curData.charAt(j) == '>') {
                                begin = j + 1;
                                break;
                            }


                        }


                        monsterData.evoMat = "" + curData.substring(begin) + "END";

                        for (int k = 1; k < 4; k++) {


                            curData = htmlLines.get(m + k);

                            if (curData.contains("</p>")) {

                                curData = curData.substring(0, curData.indexOf("</p>"));
                                begin = 0;

                                for (int j = curData.length() - 1; j >= 0; j--) {

                                    if (curData.charAt(j) == '>') {
                                        begin = j + 1;
                                        break;
                                    }


                                }

                                monsterData.evoMat += curData.substring(begin) + "END";

                            }
                        }


                        Log.i("Evolution Materials", monsterData.evoMat);

                    }

                    if (curData.contains("ascen-material")) {

                        String[] found = curData.split("href=");

                        monsterData.ascMat = "";

                        for (int g = 2; g < found.length; g += 2) {

                            String preAmt = found[g].substring(found[g].indexOf("/monster"));
                            preAmt = preAmt.substring(preAmt.indexOf("<") + 1);
                            char doubleDigit = preAmt.charAt(preAmt.indexOf("<") - 2);
                            if (isNum(doubleDigit))
                                preAmt = " " + doubleDigit + preAmt.charAt(preAmt.indexOf("<") - 1);
                            else
                                preAmt = " " + preAmt.charAt(preAmt.indexOf("<") - 1);


                            monsterData.ascMat += (getMonUrl(found[g]) + preAmt) + "END";
                        }


                        Log.i("Ascension Materials", monsterData.ascMat);


                    }


                }

                //This if statement downloads any ascension
                //thumbnails necessary for the InformationFragment

                if (monsterData.ascMat != null) {


                    Log.i("Asc Test", monsterData.ascMat);

                    String[] asc = monsterData.ascMat.split("END");

                    monsterData.ascLinks = "";


                    for (int m = 0; m < asc.length; m++) {

                        try {
                            String ascThumbnail;

                            if (Integer.parseInt(getNum(asc[m])) > 999) {

                                ascThumbnail = "http://strikeshot.net/sites/default/files/styles/thumbnail/public/" + getNum(asc[m]) + ".jpg";

                            } else {

                                ascThumbnail = "http://www.monsterstrikedatabase.com/monsters/75x75x" + getNum(asc[m]) + ".jpg.pagespeed.ic.06I6WEj3Gi.jpg";

                            }

                            URL url3 = new URL(ascThumbnail);
                            HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
                            connection3.connect();
                            InputStream thumbStream = connection3.getInputStream();

                            Bitmap bitmap = BitmapFactory.decodeStream(thumbStream);

                            File temp = context.getDir("temp", Context.MODE_PRIVATE);
                            File tempPic = new File(temp, monsterData.num + "asc" + m + ".jpg");
                            FileOutputStream out = new FileOutputStream(tempPic);


                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                            monsterData.ascLinks += tempPic.getPath() + "END";


                        } catch (Exception ex) {

                            monsterData.ascLinks += "ImageNotFoundEND";
                            ex.printStackTrace();

                        }


                    }
                    Log.i("Ascension Thumbnails", monsterData.ascLinks);


                }

            }


            try {

                monsterData.favorited = false;

                //scans the folders to determine whether the current Monster
                //is favorited or not.

                for (File f : folders) {

                    if (f.toString().endsWith(monsterData.num))
                        monsterData.favorited = true;


                }

                File temp = context.getDir("temp", Context.MODE_PRIVATE);

                if (!temp.exists())
                    temp.mkdir();

                //The next two connections are for the Monster's main Image
                // and the Monster's thumbnail. Both images are saved in the folder
                //"temp" and are deleted when the Monster's page is destroyed.

                URL url2 = new URL(mainImage);
                HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                connection.connect();
                InputStream imgStream = connection.getInputStream();
                Bitmap tempPic = BitmapFactory.decodeStream(imgStream);

                File bitmap = new File(temp, monsterData.num + ".png");

                FileOutputStream out = new FileOutputStream(bitmap);

                tempPic.compress(Bitmap.CompressFormat.PNG, 100, out);

                monsterData.bitmap = bitmap.getPath();

                Log.i("DataGrabber", "adding bitmap");

                //creating temp File

                URL url3 = new URL(thumbnail);
                HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
                connection3.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                connection3.connect();
                InputStream thumbStream = connection3.getInputStream();
                tempPic = BitmapFactory.decodeStream(thumbStream);

                bitmap = new File(temp, monsterData.num + ".jpg");

                out = new FileOutputStream(bitmap);

                tempPic.compress(Bitmap.CompressFormat.JPEG, 100, out);

                monsterData.thumb = bitmap.getPath();

                Log.i("DataGrabber", "added thumbnail");


            } catch (FileNotFoundException ex) {

                ex.printStackTrace();
                //cancelled = true;
                message = "404 Monster Data Not Found";


            } catch (Exception timeout) {

                timeout.printStackTrace();

                if (htmlLines.size() == 0) {
                    cancelled = true;
                    message = "Network Error\nMonster failed to download";
                }

            }


            mons.add(monsterData);
            Log.i("DataGrabber", "Monster " + i + " done.");

        }

        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);


        if (!stop)
            if (cancelled) {


                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                ((LoadingListener) parent).setLoading(false);

            } else {

                Log.i("DataGrabber", "storing monsters to Monster Page");


                Log.i("DataGrabber", "Sending intent to Monster Page");

                Intent intent = new Intent(context, MonsterPageActivity.class);
                intent.putExtra("monsters", mons);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());

                } else {
                    context.startActivity(intent);
                }

            }

        running = false;


    }

    public String getNum(String s) {

        return s.substring(s.indexOf("/") + 1, s.indexOf(" "));

    }

    public boolean isNum(char c) {


        if (c >= '0' && c <= '9')
            return true;

        return false;


    }


    public String addBetweenTags(String s1, int start) {
        String sAns1 = "";
        int sCount1 = start;
        while (s1.charAt(sCount1) != '<') {
            sAns1 += s1.charAt(sCount1);
            sCount1++;
        }

        return sAns1;
    }


    public String getMonUrl(String url) {
        String ans = "";
        if (url.contains("monster/")) {
            String temp1 = url.substring(url.indexOf("monster/"));
            for (int c = 0; c < temp1.length(); c++)
                if (temp1.charAt(c) != '"')
                    ans += temp1.charAt(c);
                else
                    break;

        }

        return ans;

    }


}
