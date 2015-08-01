package com.loera.monstersearch;

/**
 * Created by Daniel on 7/5/2015.
 */

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


public class DataGrabber extends AsyncTask<Void,Void,Void> {

    String monInput,message;
    ArrayList<Monster> mons;
    ArrayList<String> total;
    Monster monsterData;
    Context context;
    Activity parent;
    public static boolean cancelled,running,stop;

    public interface LoadingListener{

        public void setLoading(boolean isLoading);

    }



    public DataGrabber(String num, Context c,Activity p){


       this.monInput = num;
       this.context = c;
        this.parent = p;
        cancelled = false;
        running = true;


    }


    @Override
    protected Void doInBackground(Void... params) {

       mons = new ArrayList<>();

        try {
             total = new ArrayList<>();
             total.add(monInput);
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
                    total.add(getMonUrl(inputLine));
                else if(inputLine.contains("ascen-title-result") && inputLine.contains("href="))
                    total.add(getMonUrl(inputLine));
                else if(next) {
                    total.add(getMonUrl(inputLine));
                    next = false;
                }else if(inputLine.contains("Base Monster:"))
                    next = true;

            }



            htmlStream.close();




        }catch(Exception ignored){

            ignored.printStackTrace();
            message = "Network Error\nMonster failed to download";
            cancelled = true;

        }




        File dir = context.getDir("Monsters",Context.MODE_PRIVATE);
        ArrayList<File> folders  = new ArrayList<>(Arrays.asList(dir.listFiles()));


        for(int i = 0;i<total.size();i++){

            Log.i("DataGrabber", "creating monster " + i);

        monsterData = new Monster();


        monsterData.num = total.get(i).substring(total.get(i).indexOf("monster/")+8);
        ArrayList<String> htmlLines = new ArrayList();
        try {

            //Favorited or Not


            monsterData.favorited = false;

            for(File f:folders){

             if(f.toString().endsWith(monsterData.num))
               monsterData.favorited = true;


            }

            File temp = context.getDir("temp",Context.MODE_PRIVATE);

            if(!temp.exists())
                temp.mkdir();

            monsterData.link = total.get(i);

            URL url = new URL("http://www.strikeshot.net/"+total.get(i));
            HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
            connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            connect2.connect();
            BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));


            Log.i("DataGrabber", "accessing page");


            String inputLine;

            while ((inputLine = htmlStream.readLine()) != null)
                htmlLines.add(inputLine);

            htmlStream.close();

            String mainImage, thumbnail;

            int intCheck = Integer.parseInt(monsterData.num);

            if(intCheck > 1063){


                mainImage = "http://strikeshot.net/sites/default/files/styles/monstermainpage/public/"+monsterData.num+".png";
                thumbnail = "http://strikeshot.net/sites/default/files/styles/thumbnail/public/"+monsterData.num+".jpg";

            }else{

                mainImage = "http://www.monsterstrikedatabase.com/monsters/big/" + monsterData.num + ".png";
                thumbnail = "http://www.monsterstrikedatabase.com/monsters/"+ monsterData.num+ ".jpg";

            }


            URL url2 = new URL(mainImage);
            HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            connection.connect();
            InputStream imgStream = connection.getInputStream();
            Bitmap tempPic = BitmapFactory.decodeStream(imgStream);

            File bitmap = new File(temp,monsterData.num + ".png");

            FileOutputStream out = new FileOutputStream(bitmap);

            tempPic.compress(Bitmap.CompressFormat.PNG,100,out);

            monsterData.bitmap = bitmap.getPath();

            Log.i("DataGrabber", "adding bitmap");

            //creating temp Files

            URL url3 = new URL(thumbnail);
            HttpURLConnection connection3 = (HttpURLConnection)url3.openConnection();
            connection3.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            connection3.connect();
            InputStream thumbStream = connection3.getInputStream();
             tempPic = BitmapFactory.decodeStream(thumbStream);

            bitmap = new File(temp,monsterData.num + ".jpg");

            out = new FileOutputStream(bitmap);

            tempPic.compress(Bitmap.CompressFormat.JPEG,100,out);

            monsterData.thumb = bitmap.getPath();

            Log.i("DataGrabber", "added thumbnail");




        }catch(FileNotFoundException ex){

            cancelled = true;
            message = "404 Monster Data Not Found";


        }catch (Exception timeout) {

            timeout.printStackTrace();

            if(htmlLines.size() == 0){
                cancelled = true;
                message = "Network Error\nMonster failed to download";
            }

        }

            if(!cancelled){
       for (int m = 0; m < htmlLines.size(); m++) {
            String curData = htmlLines.get(m);


           if (curData.contains("monster-title")){

               monsterData.name = addBetweenTags(curData,curData.indexOf("-title")+8);
               Log.i("DataGrabber", "adding " + monsterData.name);
               String level = "";
               switch(monsterData.name.charAt(monsterData.name.length()-1)){

                   case '1':level = "5";
                       break;
                   case '2': level = "15";
                       break;
                   case '3': level = "20";
                       break;
                   case '4': level = "40";
                       break;
                   case '5': level = "70";
                       break;
                   case '6': level = "99";



               }

               monsterData.maxLevel = level;

            }


           if(curData.contains("monster-atrri")){
               if(curData.contains("bounce"))
                   monsterData.impact = "Bounce";
               else
                   monsterData.impact = "Pierce";


           }

            if (curData.contains("Class: ")){
                Log.i("DataGrabber", "added class");
                monsterData.monClass = addBetweenTags(curData,curData.indexOf("</span>")+8);
           }

            if (curData.contains("Type: ")){
                Log.i("DataGrabber", "added type");
                monsterData.type= addBetweenTags(curData,curData.indexOf("</span>")+7);
           }

            if (curData.contains("Obtain: ")){
                monsterData.obtain = addBetweenTags(curData,curData.indexOf("</span>")+7);
           Log.i("DataGrabber", "added obtain");}

            if (curData.contains("Max Luck :")){
                Log.i("DataGrabber", "added luck");
                monsterData.maxLuck = addBetweenTags(curData,curData.indexOf("</span>")+7);
          }

            if (curData.contains("Ability: ")){
                Log.i("DataGrabber", "added ability");
                monsterData.ability = addBetweenTags(curData,curData.indexOf("</span>")+7);
                if(htmlLines.get(m+1).contains("Gauge Shot"))
                monsterData.ability = monsterData.ability+"\nGauge:  "+ addBetweenTags(htmlLines.get(m+1),htmlLines.get(m+1).indexOf("</span>")+7);
          }

           if(curData.contains(">Health<")){

               Log.i("DataGrabber", "added health");
               monsterData.maxHealth = addBetweenTags(htmlLines.get(m+2),4);
               monsterData.plusHealth = addBetweenTags(htmlLines.get(m+3),4);


          }
           if(curData.contains(">Attack<")){

               Log.i("DataGrabber", "added attack");
               monsterData.maxAttack = addBetweenTags(htmlLines.get(m+2),4);
               monsterData.plusAttack = addBetweenTags(htmlLines.get(m+3),4);

               }
           if(curData.contains(">Speed<")){
               Log.i("DataGrabber", "added speed");
               monsterData.maxSpeed = addBetweenTags(htmlLines.get(m+2),4);
               monsterData.plusSpeed = addBetweenTags(htmlLines.get(m+3),4);


        }



            if (curData.contains("strike-shot-title")) {

                Log.i("DataGrabber", "added strike shot");

                monsterData.strikeName = addBetweenTags(curData,curData.indexOf(" - ")+3);

                monsterData.strikeInfo = addBetweenTags(htmlLines.get(m + 1),htmlLines.get(m + 1).indexOf(">")+1);

                monsterData.cooldown = addBetweenTags(htmlLines.get(m + 2),htmlLines.get(m + 2).indexOf("<span>")+6);

          }

            if (curData.contains("bumper-combo-title")) {
                Log.i("DataGrabber", "added bump combo");
                monsterData.bcName = addBetweenTags(curData,curData.indexOf(" - ")+3);

                monsterData.bcInfo = addBetweenTags(htmlLines.get(m + 1),htmlLines.get(m + 1).indexOf(">")+1);

                monsterData.bcPower = addBetweenTags(htmlLines.get(m + 2),htmlLines.get(m + 2).indexOf("<span>")+6);



              }

             if(curData.contains("evo-material")){

                 String look;

               if(!curData.contains("</span>"))
                   look = "</p>";
                 else
               look = "</span>";

                 curData = curData.substring(0,curData.indexOf(look));
               int begin = 0;

               for(int j = curData.length()-1;j >=0;j-- ){

                   if(curData.charAt(j) == '>'){
                       begin = j + 1;
                      break;
                   }


               }


               monsterData.evoMat = "" + curData.substring(begin) + "END";

               for(int k =  1;k<4;k++) {


                   curData = htmlLines.get(m + k);

                   if (curData.contains("</p>")){

                       curData = curData.substring(0, curData.indexOf("</p>"));
                   begin = 0;

                   for (int j = curData.length() - 1; j >= 0; j--) {

                       if (curData.charAt(j) == '>') {
                           begin = j + 1;
                           break;
                       }


                   }

                       monsterData.evoMat += curData.substring(begin) +"END";

               }
               }



                   Log.i("Evolution Materials",monsterData.evoMat);

              }

              if(curData.contains("ascen-material")){

                  String[] found = curData.split("href=");

                  monsterData.ascMat = "";

                  for(int g = 2;g<found.length;g+=2){

                      String preAmt = found[g].substring(found[g].indexOf("/monster"));
                      preAmt = preAmt.substring(preAmt.indexOf("<")+1);
                      preAmt =" "+ preAmt.charAt(preAmt.indexOf("<")-1);

                      monsterData.ascMat +=(getMonUrl( found[g])+ preAmt) + "END";
                  }




                      Log.i("Ascension Materials",monsterData.ascMat);




              }




        }
                if(monsterData.ascMat != null){



                       String[] asc = monsterData.ascMat.split("END");

                        monsterData.ascLinks = "";



                    for(int m = 0;m<asc.length;m++){

                        try{
                            URL url3 = new URL("http://www.monsterstrikedatabase.com/monsters/"+ getNum(asc[m]) + ".jpg");
                            HttpURLConnection connection3 = (HttpURLConnection)url3.openConnection();
                            connection3.connect();
                            InputStream thumbStream = connection3.getInputStream();

                            Bitmap bitmap = BitmapFactory.decodeStream(thumbStream);

                            File temp = context.getDir("temp",Context.MODE_PRIVATE);
                            File tempPic = new File(temp,monsterData.num+"asc"+m + ".jpg");
                            FileOutputStream out = new FileOutputStream(tempPic);


                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

                            monsterData.ascLinks += tempPic.getPath()+"END";






                        }catch(Exception ex){

                            ex.printStackTrace();

                        }



                    }
                    Log.i("Ascension Thumbnails",monsterData.ascLinks);



                }

    }

            mons.add(monsterData);
            Log.i("DataGrabber", "Monster " + i + " done.");

    }

        return null;
    }

    protected void onPostExecute(Void result){
        super.onPostExecute(result);



        if(!stop)
       if(cancelled) {



            Toast.makeText(context.getApplicationContext(),message , Toast.LENGTH_SHORT).show();
           ((LoadingListener)parent).setLoading(false);

        }else{

            Log.i("DataGrabber", "storing monsters to Monster Page");



            Log.i("DataGrabber", "Sending intent to Monster Page");

            Intent intent = new Intent(context,MonsterPage.class);
            intent.putExtra("monsters",mons);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());

            }else{
            context.startActivity(intent);
            }

        }

        running = false;



    }

    public String getNum(String s){

        return s.substring(s.indexOf("/")+1,s.indexOf(" "));

    }


    public String addBetweenTags(String s1,int start) {
        String sAns1 = "";
        int sCount1 = start;
        while (s1.charAt(sCount1) != '<') {
            sAns1 += s1.charAt(sCount1);
            sCount1++;
        }

        return sAns1;
    }


    public String getMonUrl(String url){
        String ans = "";
        if(url.contains("monster/")){
            String temp1 = url.substring(url.indexOf("monster/"));
            for(int c  = 0;c<temp1.length();c++)
                if(temp1.charAt(c)!= '"')
                    ans+=temp1.charAt(c);
                else
                    break;

        }

        return ans;

    }

    public String getMonNum(String url){

        String temp = "";
        String real = "";

        url = url.substring(url.indexOf("monster/"));

        for(int c = 0;c<url.length();c++){
            if(url.charAt(c) >= '0' && url.charAt(c) <= '9'){
                temp+= url.charAt(c);
                if(Integer.parseInt(temp) < 1064)
                    real = temp;

            }
        }

        return real;

    }



}
