package com.loera.monstersearch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//TO BE IMPLEMENTED IN THE FUTURE

public class EventGuideActivity extends AppCompatActivity {

    private static Event event;
    private  final String LOGTAG = "Event Guide";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();

        if(intent != null){

            event = intent.getParcelableExtra("event");

            displayEvent();

        }
    }


    public void displayEvent(){


        ImageView imageView = (ImageView)findViewById(R.id.bannerImage);
        imageView.setImageBitmap(BitmapFactory.decodeFile(event.bossImage));

        TextView text = (TextView)findViewById(R.id.minElement);
        text.setText(event.minionElement);
        text = (TextView)findViewById(R.id.bossType);
        text.setText(event.type);
        text = (TextView)findViewById(R.id.bossAbility);
        text.setText(event.ability);
        text = (TextView)findViewById(R.id.bossDifficulty);
        text.setText(event.bossDifficulty);
        text = (TextView)findViewById(R.id.bossElement);
        text.setText(event.bossElement);
        text = (TextView)findViewById(R.id.gimmicks);
        text.setText(event.gimmicks);
        text = (TextView)findViewById(R.id.speedClear);
        text.setText(event.speedClear);
        text = (TextView)findViewById(R.id.difficulty);
        text.setText(event.difficulty);

        if(event.overview != null){


            for(int position = 0;position<event.overview.size();position++){
            LinearLayout list = (LinearLayout)findViewById(R.id.overview);

            TextView text2 = new TextView(getApplicationContext());


            text.setText(text.getText() + event.overview.get(position));

            if(position%2 == 0){

                text.setTypeface(null, Typeface.BOLD);

            }

            list.addView(text2);
        }
        }

        new DisplayRecommendations(event.recommendations).execute();


    }


    private class DisplayRecommendations extends AsyncTask<Void,Void,Void>{

        private ArrayList<String> recommendations;

        public DisplayRecommendations(ArrayList<String> s){

            this.recommendations = s;

        }
        @Override
        protected Void doInBackground(Void... params) {

            for(int m = 0;m<recommendations.size();m++) {
                String thumbnail;

                String[] split = recommendations.get(m).split("END");

                int intCheck = getNum(split[0]);

                if (intCheck > 1063) {


                    thumbnail = "http://strikeshot.net/sites/default/files/styles/thumbnail/public/" + intCheck + ".jpg";

                } else {

                    thumbnail = "http://www.monsterstrikedatabase.com/monsters/" + intCheck + ".jpg";

                }

                try {
                    URL url3 = new URL(thumbnail);
                    HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
                    connection3.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                    connection3.connect();
                    InputStream thumbStream = connection3.getInputStream();
                    Bitmap tempPic = BitmapFactory.decodeStream(thumbStream);

                    File temp = getApplicationContext().getDir("temp", Context.MODE_PRIVATE);

                    if(!temp.exists())
                        temp.mkdirs();

                    File bitmap = new File(temp, intCheck + ".jpg");

                    FileOutputStream out = new FileOutputStream(bitmap);

                    tempPic.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    String tempS = recommendations.get(m);

                    recommendations.remove(m);

                    recommendations.add(m,bitmap.getPath() + tempS.substring(tempS.indexOf("END")));

                    Log.i(LOGTAG,"thumbnail is " + recommendations.get(m));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public void onPostExecute(Void result){

            LinearLayout list = (LinearLayout)findViewById(R.id.recommendedMonsters);


            for(int position = 0;position<recommendations.size();position++) {
                TextView text = new TextView(getApplicationContext());
                text.setLayoutParams(list.getLayoutParams());

                text.setText("");
                text.setTextColor(Color.WHITE);


                String[] split = recommendations.get(position).split("END");

                for (int a = 0; a < split.length; a++) {

                    if (a == 0) {

                        Bitmap b = BitmapFactory.decodeFile(split[a]);

                        Drawable d = new BitmapDrawable(getResources(), b);

                        float scale = getResources().getDisplayMetrics().density;

                        int stretch, height;

                        switch (HomeActivity.screenSize) {

                            case Configuration.SCREENLAYOUT_SIZE_LARGE:

                                stretch = b.getWidth() - (b.getWidth() / 3);
                                height = 100;

                                break;

                            case Configuration.SCREENLAYOUT_SIZE_XLARGE:

                                stretch = b.getWidth() - (b.getWidth() / 3);

                                height = 100;

                                break;


                            default:
                                stretch = (int) (b.getWidth() * 1.5);
                                height = 250;


                        }

                        if (scale <= 1.5) {
                            stretch = stretch - (stretch / 2);
                            height = height - (height / 3);
                        }

                        d.setBounds(0, 0, stretch, stretch);
                        text.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        text.getLayoutParams().height = height;
                        text.setPadding(10, 10, 10, 0);

                        text.setCompoundDrawables(d, null, null, null);

                    } else {
                        text.setText(text.getText() + "- " + split[a] + "\n");
                    }
                }


                text.setCompoundDrawablePadding(5);

                list.addView(text);


            }
        }
    }

    public int getNum(String s){

        return Integer.parseInt(s.substring(s.indexOf("/") + 1));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
