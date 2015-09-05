package com.loera.monstersearch;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
*
*
* This is the single most important Activity, the Monster Page!
* Here the all Views are filled with information Monsters
* received within the Intent. The ViewPager for Monster swiping
* is also set up to change between evolutions and ascensions.
*
*
* */

public class MonsterPageActivity extends AppCompatActivity implements ResultsFragment.DialogListener, InformationFragment.MaterialsListener, MonsterGrabber.LoadingListener {
    private ArrayList<Monster> monsters;
    private ImageAdapter adapter;
    private MonsterImageView viewPager;
    private Menu menu;
    private int initWidth, currentSelected;
    private boolean finish, isMonsterBox;
    private SearchView search;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_monster_page);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        viewPager = (MonsterImageView) findViewById(R.id.imageSwipe);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getSupportActionBar().setElevation(0);
            viewPager.setTransitionName("boxToPage");

            getWindow().setExitTransition(new Explode().setDuration(500));
            getWindow().setEnterTransition(new Explode().setDuration(500));

        }

        Intent intent = getIntent();

        //this int is to select a Monster if it was Selected from the
        //MonsterBoxFragment.
        int position = intent.getIntExtra("selection", 999);
        //Parcelable Monsters stored from the intent.
        monsters = intent.getParcelableArrayListExtra("monsters");

        //this ViewPager changes all the Views on the page based on
        //what page is selected.
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentSelected = position;
                displayMonster(monsters.get(position));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //initialize and set the adapter
        adapter = new ImageAdapter(context);
        viewPager.setAdapter(adapter);
        if (position == 999) {
            isMonsterBox = false;
            viewPager.setCurrentItem(0);
            displayMonster(monsters.get(0));
            getSupportActionBar().setTitle("Strike Search");
        } else {
            viewPager.setCurrentItem(position);
            displayMonster(monsters.get(position));
            isMonsterBox = true;
            getSupportActionBar().setTitle("Monster Box");

        }

    }


    public void onStop() {
        super.onStop();

        if (MonsterGrabber.running)
            MonsterGrabber.stop = true;

        if (finish)
            finish();

        setLoading(false);

    }


    @Override
    public void onBackPressed() {

        if (!search.isIconified()) {

            search.setQuery("", false);
            search.setIconified(true);

        } else {


            super.onBackPressed();

        }


    }

    public void onDestroy() {

        super.onDestroy();

        if (!isMonsterBox)
            removeImages();

    }
/*
*
* This method deletes all Monster images
* Use with caution.
*
* */
    public void removeImages() {

        for (Monster m : monsters) {
            if (m.bitmap != null)
                new File(m.bitmap).delete();
            if (m.thumb != null)
                new File(m.thumb).delete();


        }

    }

    /*
    *
    * This is a custom adapter for the ViewPager to show the
    * correct Monsters on swipe.
    *
    * */

    private class ImageAdapter extends PagerAdapter {

        Context context;

        public ImageAdapter(Context context) {

            this.context = context;
        }


        public int getItemPosition(Object object) {


            int index = monsters.indexOf(object);

            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public int getCount() {
            return monsters.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        public Object instantiateItem(ViewGroup container, int position) {
            final ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(BitmapFactory.decodeFile(monsters.get(position).bitmap));

            //floating animations for the Imageview
            if (imageView.getAnimation() == null) {
                imageView.clearAnimation();
                TranslateAnimation up = new TranslateAnimation(0, 0, 0, imageView.getY() - 15);
                up.setRepeatCount(-1);
                up.setRepeatMode(Animation.REVERSE);
                up.setDuration(1500);
                up.setFillAfter(true);
                imageView.setAnimation(up);
            }
            container.addView(imageView);

            return imageView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
           //this stops animations from duplicating
            ((ImageView) object).clearAnimation();
            container.removeView((ImageView) object);
        }


    }


    /*
    *
    * This AsyncTask searches the strikeshot.net
    * for any matched and either displays the Monster,
    * or lists the found results.
    *
    * */

    private class DataCheck extends AsyncTask<Void, Void, Void> {
        Context context;
        String monsterNum;
        boolean error, cancelled;
        int matches;


        public DataCheck(String n, Context c) {
            this.monsterNum = n;
            this.context = c;
            error = false;

            cancelled = false;

        }


        @Override
        protected Void doInBackground(Void... params) {


            boolean isNum = false;
            int checker = 0;
            for (int n = 0; n < monsterNum.length(); n++)
                if (monsterNum.charAt(n) >= '0' && monsterNum.charAt(n) <= '9')
                    checker++;
            if (checker == monsterNum.length())
                isNum = true;

            //htmlLines is used to store all found matching results.
            ArrayList<String> htmlLines = new ArrayList<>();


            try {
                if (isNum) {
                    Log.i("DataCheck", "searching for monster with number " + monsterNum);

                    htmlLines.add("/monster/" + monsterNum);

                } else {
                    //replace spaces with "+"
                    monsterNum = addPlus(monsterNum);

                    //this connections is the main search page that
                    // sifts through all results.
                    URL url = new URL("http://www.strikeshot.net/search-page?search=" + monsterNum);
                    HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
                    connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                    connect2.connect();
                    BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));


                    String inputLine;


                    Log.i("DataCheck", "searching for all possible matches of " + monsterNum);
                    while ((inputLine = htmlStream.readLine()) != null)
                        if (inputLine.contains("nothing-1") && inputLine.contains("#") && inputLine.contains("search-page-header") && inputLine.contains("monster/"))
                            htmlLines.add(inputLine);
                    htmlStream.close();
                }

                matches = htmlLines.size();

                if (!htmlLines.isEmpty()) {

                    Log.i("Datacheck", "results found " + htmlLines.size() + " " + htmlLines);
                    //if only one results is found, another MonsterPageActivity is called.
                    if (htmlLines.size() == 1) {

                        new MonsterGrabber(getMonUrl(htmlLines.get(0)), context, MonsterPageActivity.this).execute();
                        finish = true;

                    } else { //if more than one results is found, a ResultsFragment is created

                        Log.i("DataCheck", "creating results page intent");

                        ResultsFragment resultsFragment = new ResultsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("monsters", htmlLines);
                        resultsFragment.setArguments(bundle);

                        finish = true;
                        resultsFragment.show(getFragmentManager(), "Results");

                    }

                } else {
                    Log.i("Datacheck", "No results found");
                    error = true;

                }
            } catch (Exception ignored) {

                cancelled = true;
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (error) {
                error();
                return;
            }
            if (cancelled) {
                Toast.makeText(context.getApplicationContext(), "Network Error\nMonster failed to download", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }

            String foundMessage;

            if (matches > 1)
                foundMessage = "Found " + matches + " Matches";
            else
                foundMessage = "Found 1 Match";


            Toast.makeText(context.getApplicationContext(), foundMessage, Toast.LENGTH_SHORT).show();

        }

        /*
        *
        * This method replaces all spaces in a string with a "+"
        *
        * */

        public String addPlus(String monsterNum) {

            if (monsterNum.trim().contains(" ")) {

                String temp = monsterNum;
                monsterNum = "";

                for (int c = 0; c < temp.length(); c++) {

                    if (temp.charAt(c) == ' ')
                        monsterNum += "+";
                    else
                        monsterNum += temp.charAt(c);


                }

            }
            return monsterNum;
        }

        /*
        *
        * This method gets the Monster's url from a HTML line,
        * if it is found.
        *
        * */
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

/*

TODO: implement Event page for easy guide tutorials.
    private class EventGrabber extends AsyncTask<Void, Void, Void> {

        public String link, banner;
        public Event eventData;


        public EventGrabber(String l, String b) {

            this.link = l;
            this.banner = b;
        }

        public String removeTags(String s) {

            String ans = s;

            if (s.contains(">")) {

                ans = s.substring(s.indexOf(">") + 1);

                ans = ans.substring(0, ans.indexOf("<"));

            }

            if (ans.contains(">"))
                return removeTags(ans);

            if (ans.isEmpty()) {
                ans = s.substring(1);
                return removeTags((ans.substring(ans.indexOf("<") + 1)));

            }

            return ans;

        }

        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<String> htmlLines = new ArrayList<>();
            try {
                URL url = new URL("http://www.strikeshot.net" + link);
                HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
                connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                connect2.connect();
                BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));


                Log.i("EventGrabber", "accessing page");


                String inputLine;

                while ((inputLine = htmlStream.readLine()) != null)
                    htmlLines.add(inputLine);

                htmlStream.close();

            } catch (Exception ex) {

                ex.printStackTrace();
            }

            eventData = new Event();

            for (int m = 0; m < htmlLines.size(); m++) {

                String curData = htmlLines.get(m);

                if (curData.contains("Minion Element:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.minionElement = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "Minion Element = " + eventData.minionElement);

                }

                if (curData.contains("Boss Type:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.type = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "type = " + eventData.type);


                }
                if (curData.contains("Boss Ability:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.ability = temp.substring(0, temp.indexOf("<"));


                    Log.i("EventGrabber", "ability = " + eventData.ability);
                }

                if (curData.contains("Boss Difficulty:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.bossDifficulty = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "Boss difficulty = " + eventData.bossDifficulty);
                }
                if (curData.contains("Boss Element:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.bossElement = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "Boss element = " + eventData.bossElement);


                }
                if (curData.contains("Boss Gimmicks:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.gimmicks = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "Boss gimmicks = " + eventData.gimmicks);

                }
                if (curData.contains("Speed Clear:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.speedClear = temp.substring(0, temp.indexOf("<"));


                }
                if (curData.contains(">Difficulty:")) {

                    String temp = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf(">") + 1);

                    eventData.difficulty = temp.substring(0, temp.indexOf("<"));

                    Log.i("EventGrabber", "difficulty = " + eventData.difficulty);
                }

                if (curData.contains("Event Overview:")) {
                    eventData.overview = new ArrayList<>();
                    int count = m + 1;
                    String line = htmlLines.get(count);

                    while (!line.contains("</div>")) {

                        if (line.contains("<p>")) {

                            if (line.contains("<strong>")) {
                                String title = line.substring(line.indexOf("<strong>") + 8, line.indexOf("</strong>"));

                                eventData.overview.add(title);
                            }
                        }

                        if (line.contains("<li>")) {

                            String message = line.substring(line.indexOf("<li>") + 4, line.indexOf("</li>"));
                            if (message.contains(">")) {
                                message = message.substring(message.indexOf(">") + 1);
                                message = message.substring(0, message.indexOf("<"));
                            }

                            eventData.overview.add(message);

                        }

                        count++;

                        line = htmlLines.get(count);

                    }

                    for (String s : eventData.overview)
                        Log.i("EventGrabber Overview", s);


                }

                if (curData.contains("<tr>") && htmlLines.get(m + 1).contains("thumbnail")) {

                    if (eventData.recommendations == null) {

                        eventData.recommendations = new ArrayList<>();

                    }
                    String recommendation = htmlLines.get(m + 1).substring(htmlLines.get(m + 1).indexOf("monster/"));

                    recommendation = recommendation.substring(0, recommendation.indexOf(">") - 1) + "END";

                    int count = m + 1;
                    String line = htmlLines.get(count);

                    while (!line.contains("</tr>")) {

                        if (line.contains("<li>")) {

                            recommendation += line.substring(line.indexOf("<li>") + 4, line.indexOf("</li>")) + "END";

                        }

                        count++;
                        line = htmlLines.get(count);
                    }

                    eventData.recommendations.add(recommendation);

                }

                if (curData.contains("Procedure of Conquest:")) {

                    if (eventData.stageData == null)
                        eventData.stageData = new ArrayList<>();

                    String stage = "";

                    String imageAndTitle = htmlLines.get(m - 1);
                    if (imageAndTitle.contains("src=")) {
                        String image = imageAndTitle.substring(imageAndTitle.indexOf("src=") + 5, imageAndTitle.indexOf(".png") + 4) + "END";
                        String title = imageAndTitle.substring(imageAndTitle.indexOf("<strong>") + 8, imageAndTitle.indexOf("</strong>")) + "END";

                        stage = image + title;
                    }

                    int count = m + 1;

                    String step = htmlLines.get(count);

                    while (!step.contains("</div>")) {

                        if (step.contains("<p>"))
                            step = step.substring(step.indexOf("<p>") + 3, step.indexOf("</p>")) + "END";
                        else
                            step = step.substring(0, step.indexOf("<")) + "END";

                        stage += step;

                        count++;
                        step = htmlLines.get(count);


                    }

                    eventData.stageData.add(stage);

                }

                if (curData.contains("Boss Moveset:")) {

                    String set = "";

                    for (int count = m + 1; count < m + 9; count++) {

                        String line = htmlLines.get(count);


                        line = removeTags(line);


                        set += line + "END";

                    }

                    eventData.bossMoveset = set;

                    Log.i("EventGrabber", "Boss Moveset = " + set);


                }
            }

            eventData.bossImage = banner;

            return null;
        }

        public void onPostExecute(Void result) {


            Intent intent = new Intent(context, EventGuideActivity.class);
            intent.putExtra("event", eventData);
            context.startActivity(intent);


        }

    }

*/
    /*
    *
    * This method initiates each View on MonsterPageActivity
    * and stores the corresponding data from the Monster
    * parameter.
    *
    * */

    public void displayMonster(Monster monsterData) {

        if (!(menu == null)) {
            MenuItem favorite = menu.findItem(R.id.favoriteButton);
            if (monsterData.favorited)
                favorite.setIcon(R.drawable.ic_heart);
            else
                favorite.setIcon(R.drawable.ic_heart_outline);
        }

        if (monsterData.thumb != null) {
            ImageView thumb = (ImageView) findViewById(R.id.thumbImage);
            Bitmap image = BitmapFactory.decodeFile(monsterData.thumb);
            thumb.setImageBitmap(image);
            if (image != null) {
                int pixel = image.getPixel(image.getWidth() - 5, image.getHeight() / 4);
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                hsv[2] *= 0.8f;
                pixel = Color.HSVToColor(hsv);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(pixel));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    this.setTaskDescription(new ActivityManager.TaskDescription(null, null, pixel));

                    hsv[2] *= 0.7f;
                    pixel = Color.HSVToColor(hsv);
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    window.setStatusBarColor(pixel);


                }
            }
        }


        TextView view;
        //set title, class, and max Level
        view = (TextView) findViewById(R.id.title);
        view.setText(monsterData.name);
        view = (TextView) findViewById(R.id.monClass);
        view.setText(monsterData.monClass);
        view = (TextView) findViewById(R.id.level);
        view.setText(monsterData.maxLevel);
        //change all progress bars

        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarHP);


        //health
        int total = 0;
        double calc;
        if (monsterData.maxHealth != null && monsterData.plusHealth != null) {
            if (monsterData.maxHealth.isEmpty())
                monsterData.maxHealth = "0";
            if (monsterData.plusHealth.isEmpty())
                monsterData.plusHealth = "0";
            total = Integer.parseInt(monsterData.maxHealth) + Integer.parseInt(monsterData.plusHealth);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bar.getLayoutParams();

            if (initWidth == 0)
                initWidth = lp.width;

            calc = (total / 25000.0);
            if (calc > 1)
                calc = 1;

            lp.width = (int) (initWidth * calc);
            bar.setLayoutParams(lp);

            bar.setMax(total);
            bar.setProgress(Integer.parseInt((monsterData.maxHealth)));
            bar.invalidate();

        }
        //attack
        bar = (ProgressBar) findViewById(R.id.progressBarAttack);
        if (monsterData.maxAttack != null && monsterData.plusAttack != null) {
            if (monsterData.maxAttack.isEmpty())
                monsterData.maxAttack = "0";
            if (monsterData.plusAttack.isEmpty())
                monsterData.plusAttack = "0";

            total = Integer.parseInt(monsterData.maxAttack) + Integer.parseInt(monsterData.plusAttack);


            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bar.getLayoutParams();
            calc = (total / 35000.0);
            if (calc > 1)
                calc = 1;
            lp.width = (int) (initWidth * calc);
            bar.setLayoutParams(lp);

            bar.setMax(total);
            bar.setProgress(Integer.parseInt(monsterData.maxAttack));
        }
        //speed
        bar = (ProgressBar) findViewById(R.id.progressBarSpeed);

        if (monsterData.maxSpeed != null && monsterData.plusSpeed != null) {

            if (monsterData.maxSpeed.isEmpty())
                monsterData.maxSpeed = "0";
            if (monsterData.plusSpeed.isEmpty())
                monsterData.plusSpeed = "0";

            total = (int) (Double.parseDouble(monsterData.maxSpeed) + Double.parseDouble(monsterData.plusSpeed));

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bar.getLayoutParams();
            calc = (total / 420.0);
            if (calc > 1)
                calc = 1;
            lp.width = (int) (initWidth * calc);
            bar.setLayoutParams(lp);

            bar.setMax(total);
            bar.setProgress((int) Double.parseDouble(monsterData.maxSpeed));

        }
        //set the stats

        //health
        view = (TextView) findViewById(R.id.healthNum);
        if (monsterData.maxHealth == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.maxHealth);
        view = (TextView) findViewById(R.id.plusHealth);
        if (monsterData.plusHealth == null)
            view.setText("+0");
        else
            view.setText("+" + monsterData.plusHealth);
        //attack
        view = (TextView) findViewById(R.id.attackNum);
        if (monsterData.maxAttack == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.maxAttack);
        view = (TextView) findViewById(R.id.plusAttack);
        if (monsterData.plusAttack == null)
            view.setText("+0");
        else
            view.setText("+" + monsterData.plusAttack);
        //speed
        view = (TextView) findViewById(R.id.speedNum);
        if (monsterData.maxSpeed == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.maxSpeed);
        view = (TextView) findViewById(R.id.plusSpeed);
        if (monsterData.plusSpeed == null)
            view.setText("+0");
        else
            view.setText("+" + monsterData.plusSpeed);

        //set the text

        view = (TextView) findViewById(R.id.abilityBlock);
        if (monsterData.impact == null && monsterData.ability == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.impact + "\n" + monsterData.ability);
        view = (TextView) findViewById(R.id.type);
        if (monsterData.type == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.type + " Type");

        view = (TextView) findViewById(R.id.strikeShotBlock);
        if (monsterData.strikeInfo == null && monsterData.strikeName == null)
            view.setText("Unknown");
        else
            view.setText(removeJunk(monsterData.strikeName) + ":\n" + monsterData.strikeInfo);
        view = (TextView) findViewById(R.id.cooldown);
        if (monsterData.cooldown == null)
            view.setText("Unknown");
        else
            view.setText(monsterData.cooldown);

        view = (TextView) findViewById(R.id.bcBlock);
        if (monsterData.bcName == null && monsterData.bcInfo == null)
            view.setText("Unknown");
        else
            view.setText("Bump Combo:\n" + monsterData.bcName + "\n" + monsterData.bcInfo);
        view = (TextView) findViewById(R.id.bcPower);
        if (monsterData.bcPower == null)
            view.setText("Unknown");
        else
            view.setText("Power: " + monsterData.bcPower);


    }

    /*
    *
    * This method removes any extra characters left by the HTML.
    *
    * */
    public String removeJunk(String junk) {

        int begin = junk.indexOf("&");
        int end = junk.indexOf(";");

        if (begin == -1 || end == -1)
            return junk;

        String dejunked = "";

        for (int a = 0; a < junk.length(); a++) {

            if (a < begin || a > end)
                dejunked += junk.charAt(a);
            else if (a == begin)
                dejunked += "'";

        }

        return removeJunk(dejunked);
    }

    /*
    *
    * This method displays makes the ProgressBar for
    * loading visible or invisible.
    *
    * */
    public void setLoading(boolean isLoading) {
        ScrollView scroll = (ScrollView) findViewById(R.id.textScrollView);
        ProgressBar bar = (ProgressBar) findViewById(R.id.monsterLoading);

        if (isLoading) {
            viewPager.setPagingEnabled(false);
            scroll.fullScroll(ScrollView.FOCUS_UP);
            bar.setVisibility(View.VISIBLE);
        } else {
            viewPager.setPagingEnabled(true);
            scroll.fullScroll(ScrollView.FOCUS_UP);
            bar.setVisibility(View.INVISIBLE);
        }

    }
/*
*
* This is the default error method when no Monster
* results are found.
*
* */
    public void error() {


        AlertDialog.Builder error = new AlertDialog.Builder(this);
        error.setTitle("Monster Not Found");
        error.setMessage("Please try another name or number.");
        error.setPositiveButton("OK", null);
        setLoading(false);
        error.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monster_page, menu);
        //setup search bar
        final MenuItem searchItem = menu.findItem(R.id.actionSearch);
        search = (SearchView) MenuItemCompat.getActionView(searchItem);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                SearchFragment data = new SearchFragment();


                if (data.invalid(query))
                    error();
                else {
                    setLoading(true);
                    search.setQuery("", false);
                    search.setIconified(true);
                    new DataCheck(query, context).execute();

                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        //setup favorite button
        final MenuItem favoriteButton = menu.findItem(R.id.favoriteButton);

        if (monsters.get(currentSelected).favorited)
            favoriteButton.setIcon(R.drawable.ic_heart);
        else
            favoriteButton.setIcon(R.drawable.ic_heart_outline);


        this.menu = menu;


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final Monster curMon = monsters.get(currentSelected);
        if (id == R.id.materialsButton) {

            if ((curMon.ascMat == null && curMon.evoMat == null)) {

                Toast.makeText(context, "Current Monster has no information to display", Toast.LENGTH_SHORT).show();


            } else {

                InformationFragment mat;

                Bundle bundle = new Bundle();
                bundle.putString("asc", curMon.ascMat);
                bundle.putString("evo", curMon.evoMat);
                bundle.putString("pics", curMon.ascLinks);
                Log.i("TEST", curMon.ascLinks + "");
                if (curMon.event != null)
                    if (curMon.event.contains("event"))
                        bundle.putString("event", curMon.event);

                mat = new InformationFragment();
                mat.setArguments(bundle);

                mat.show(getFragmentManager(), "Materials");


            }
        }


        if (id == R.id.favoriteButton) {


            if (!curMon.favorited) {
                item.setIcon(R.drawable.ic_heart);
                curMon.favorited = true;
                HomeActivity.newFav = true;
                MonsterBoxFragment.addFavorite(curMon, context);
                Toast.makeText(context.getApplicationContext(), "Added to Monster Box", Toast.LENGTH_SHORT).show();
            } else {

                if (isMonsterBox) {
                    AlertDialog.Builder error = new AlertDialog.Builder(this);
                    error.setTitle("Monster Removal");
                    error.setMessage("Remove\n" + monsters.get(currentSelected).name + "\nfrom your Monster Box?");
                    error.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            MonsterBoxFragment.removeFavorite(curMon.num, context);
                            monsters.remove(currentSelected);

                            if (!monsters.isEmpty())
                                if (currentSelected == monsters.size())
                                    displayMonster(monsters.get(currentSelected - 1));
                                else
                                    displayMonster(monsters.get(currentSelected));

                            if (monsters.isEmpty())
                                onBackPressed();
                            else
                                adapter.notifyDataSetChanged();

                        }
                    });
                    error.setNegativeButton("Cancel", null);
                    setLoading(false);
                    error.show();
                } else {
                    item.setIcon(R.drawable.ic_heart_outline);
                    curMon.favorited = false;
                    MonsterBoxFragment.removeFavorite(curMon.num, context);
                    Toast.makeText(context.getApplicationContext(), "Removed Monster", Toast.LENGTH_SHORT).show();

                }
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
