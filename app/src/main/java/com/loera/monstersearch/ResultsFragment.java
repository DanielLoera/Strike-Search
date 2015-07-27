package com.loera.monstersearch;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Daniel on 7/10/2015.
 *
 * :)
 */
public class ResultsFragment extends DialogFragment {
    ArrayList<String> monsters;
    ArrayList<Monster> monData;
    private boolean selected,running;
    private DisplayResultsPage page;
    View view;
    Context context;

    DialogListener mBack;

public interface DialogListener{

    public void setLoading(boolean isLoading);

 }


    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getDialog().setTitle("Search Results");
        getDialog().setCanceledOnTouchOutside(false);
         monsters = getArguments().getStringArrayList("monsters");

       return inflater.inflate(R.layout.fragment_results,container,false);
    }

    public void onAttach(Activity activity){

        super.onAttach(activity);

        mBack = (ResultsFragment.DialogListener)activity;

}



    public void onStart(){
        super.onStart();

        selected = false;

        view = getView();
        context = getActivity();

        page = new DisplayResultsPage();

        if(Home.screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE){
            Log.i("Results","Large Screen Detected");
            int width = (int)getResources().getDimension(R.dimen.popupWidthLarge);
            int height = (int)getResources().getDimension(R.dimen.popupHeightLarge);
        getDialog().getWindow().setLayout(width,height);
        }

        running = true;

        page.execute();



    }

    public void onDismiss(DialogInterface dialog){

        if(!running){

        super.onDismiss(dialog);

        if(!selected)
        mBack.setLoading(false);

       removeImages();
        }else{

            mBack.setLoading(false);
            page.stop = true;


        }


    }

    public void removeImages(){

        for(Monster m:monData){
            if(m.bitmap != null)
                new File(m.bitmap).delete();
            if(m.thumb != null)
                new File(m.thumb).delete();


        }


    }








    private class DisplayResultsPage extends AsyncTask<Void,Void,Void>{

      public boolean cancelled,stop;
        @Override
        protected Void doInBackground(Void... params) {

            monData = new ArrayList<>();
            cancelled = false;

            Log.i("Results","removing non monster data");

            for(int r = 0;r<monsters.size();r++){

                if(!monsters.get(r).contains("monster/")) {
                    monsters.remove(r);
                    r--;
                }

            }


            for(int count  = 0;count<monsters.size();count++ ){

                Monster monster = new Monster();

                monster.name = getMonName(monsters.get(count));

                monster.num = getMonNum(monsters.get(count));

                try {

                    File temp = context.getDir("temp",Context.MODE_PRIVATE);

                    if(!temp.exists())
                        temp.mkdir();

                    URL url3 = new URL("http://www.monsterstrikedatabase.com/monsters/" + monster.num + ".jpg");
                    HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
                    connection3.connect();
                    InputStream thumbStream = connection3.getInputStream();
                    Bitmap tempPic = BitmapFactory.decodeStream(thumbStream);

                    File image  = new File(temp,monster.num + ".jpg");

                    FileOutputStream out = new FileOutputStream(image);

                    tempPic.compress(Bitmap.CompressFormat.JPEG,100,out);
                    monster.thumb = image.getPath();

                    Log.i("ResultsPage", "adding thumbnail");


                }catch(Exception ignored){

                    ignored.printStackTrace();

                    if(monster.name == null)
                        cancelled = true;


                }



                monData.add(monster);



            }





            return null;
        }

        protected void onPostExecute(Void result){

            super.onPostExecute(result);

            if(!stop)
            if(!cancelled) {
                final LinearLayout layout = (LinearLayout) view.findViewById(R.id.monsterList);
                TextView textView;

                for (int i = 0; i < monData.size(); i++) {
                    Monster curMon = monData.get(i);
                    textView = new TextView(context);
                    textView.setId(i);
                    textView.setTextColor(Color.parseColor("#000000"));
                    textView.setLayoutParams(layout.getLayoutParams());
                    ViewGroup.LayoutParams lp = textView.getLayoutParams();

                    int stretch = 0;
                    int padding = 0;
                    int height = 0;

                    if (curMon.thumb != null) {
                        Bitmap thumbnail = BitmapFactory.decodeFile(curMon.thumb);
                        Drawable draw = new BitmapDrawable(context.getResources(), thumbnail);

                        switch (Home.screenSize){

                            case Configuration.SCREENLAYOUT_SIZE_LARGE:

                                stretch = thumbnail.getWidth() - (thumbnail.getWidth()/3);
                                padding =15;
                                height = 100;

                                break;

                            case Configuration.SCREENLAYOUT_SIZE_XLARGE:

                                stretch = thumbnail.getWidth() - (thumbnail.getWidth()/3);
                                padding =15;
                                height = 100;

                                break;


                            default:
                               stretch  =  thumbnail.getWidth() * 2;
                               padding = 30;
                                height = 250;

                        }

                        draw.setBounds(0, 0,  stretch ,stretch );
                        lp.height = height;
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;

                        textView.setCompoundDrawables(draw, null, null, null);

                        int pixel = thumbnail.getPixel(2,thumbnail.getHeight()/2);


                        textView.setBackgroundColor(pixel);

                    }
                    textView.setText("  " + curMon.name);

                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    textView.setPadding(padding, 0, padding, 0);
                    textView.setTextSize(22);

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                     if (!selected) {

                        selected = true;

                        new DataGrabber(getMonUrl(monsters.get(v.getId())), context,getActivity()).execute();

                        dismiss();
                       }

                        }
                    });


                    layout.addView(textView);


                }

            }else{

                Toast.makeText(context.getApplicationContext(),"Network Error\nMonster failed to download",Toast.LENGTH_SHORT).show();

            }

                setLoading(false);
                running = false;

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

        public String getMonName(String url){

            url = url.substring(url.indexOf("monster/"));

            int carrot = url.indexOf(">")+1;

            url = url.substring(carrot);

            String result = "";

            for(int s = 0;s<url.length();s++){
                if(url.charAt(s)!= '<')
                    result+= url.charAt(s);
                else
                    break;
            }

            return result;
        }



    }


public void setLoading(boolean isLoading) {

    ProgressBar bar = (ProgressBar) view.findViewById(R.id.progressBarResults);

    if(isLoading){


        bar.setVisibility(View.VISIBLE);

    }else {

        bar.setVisibility(View.GONE);
    }



}

}

