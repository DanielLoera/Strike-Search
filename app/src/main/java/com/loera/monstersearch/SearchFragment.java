package com.loera.monstersearch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

/**
*
*
* This Fragment is the startup page with
* the main Search Box to begin your journey! :)
*
*
* */
public class SearchFragment extends android.app.Fragment {

    private View view;
    Context context;
    private int randomStart;
    private ImageView image;
    public static ResultsFragment resultsFragment;
    private boolean running;
    SharedPreferences prefs;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        return inflater.inflate(R.layout.fragment_search, container, false);

    }

    public void onCreate(Bundle state) {

        super.onCreate(state);


    }


    public void onStart() {

        super.onStart();


        view = getView();
        context = getActivity();


        image = (ImageView) view.findViewById(R.id.randomImage);

        //Long click opens MonsterPageActivity to display the Monster
        //from the Monster Image.
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (randomStart != 0) {
                    new MonsterGrabber("monster/" + randomStart, context, getActivity()).execute();
                    setLoading(true);
                    Toast.makeText(context, "Loading Monster " + randomStart, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        //Random Monster Image set based on
        //your settings.
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Log.i("Search", "randomImage = " + HomeActivity.randomImage);

        String prefImage = prefs.getString("pref_image", "none");

        if (prefImage.equals("none"))
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);

        prefImage = prefs.getString("pref_image", "none");

        if (prefImage.equals("random"))
            new DisplayRandomImage().execute();
        else if (prefImage.equals("box")) {

            File internal = context.getDir("Homescreen", Context.MODE_PRIVATE);
            if (!internal.exists())
                internal.mkdir();

            File[] imageFiles = internal.listFiles();

            if (imageFiles.length != 0) {

                Bitmap image = BitmapFactory.decodeFile(imageFiles[0].getPath());

                ImageView randomImage = (ImageView) view.findViewById(R.id.randomImage);

                randomImage.setImageBitmap(image);

            } else {
                new DisplayRandomImage().execute();
            }

        }

    }

    public void onStop() {

        super.onStop();

        if (MonsterGrabber.running)
            MonsterGrabber.stop = true;

        setLoading(false);

    }


    /*
    *
    * This method grabs a random image for display!
    *
    * */
    public void displayRandomImage(View view) {

        String prefOnTap = prefs.getString("pref_imageOnTap", "none");
        if (prefOnTap.equals("none"))
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        prefOnTap = prefs.getString("pref_imageOnTap", "none");

        if (prefOnTap.equals("random")) {
            if (!running) {
                randomStart = 0;
                HomeActivity.randomImage = null;
                TranslateAnimation anim = new TranslateAnimation(0, image.getX() - 2000, 0, 0);
                anim.setDuration(500);
                anim.setFillAfter(true);
                image.startAnimation(anim);
                running = true;
                new DisplayRandomImage().execute();
            }
        }

    }
/*
*
* This AsyncTask downloads the random image
* and sets its animations sliding right to left.
*
* */
    public class DisplayRandomImage extends AsyncTask<Void, Void, Void> {
        boolean cancelled;


        @Override
        protected Void doInBackground(Void... params) {
            cancelled = false;

            if (randomStart == 0)
                randomStart = (int) (Math.random() * 1060) + 1;

            if (HomeActivity.randomImage == null) {
                try {
                    URL imgURL = new URL("http://www.monsterstrikedatabase.com/monsters/big/" + randomStart + ".png");

                    HttpURLConnection connection = (HttpURLConnection) imgURL.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream randomStream = connection.getInputStream();

                    Bitmap random = BitmapFactory.decodeStream(randomStream);

                    File temp = context.getDir("temp", Context.MODE_PRIVATE);
                    if (!temp.exists())
                        temp.mkdirs();
                    File image = new File(temp, "randomImage.png");
                    FileOutputStream out = new FileOutputStream(image);

                    random.compress(Bitmap.CompressFormat.PNG, 100, out);

                    HomeActivity.randomImage = image.getPath();

                } catch (FileNotFoundException f) {
                    f.printStackTrace();
                    randomStart = 0;
                    new DisplayRandomImage().execute();

                } catch (Exception ignore) {
                    ignore.printStackTrace();
                    cancelled = true;
                    Log.i("Search Activity", "Image failed to download.");

                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (cancelled) {


                Toast.makeText(context.getApplicationContext(), "Network Error\nImage failed to download.", Toast.LENGTH_SHORT).show();

            } else {

                if (HomeActivity.randomImage != null) {

                    if (new File(HomeActivity.randomImage).exists()) {

                        TranslateAnimation anim = new TranslateAnimation(image.getX() + 2000, 0, 0, 0);
                        anim.setDuration(500);
                        anim.setFillAfter(true);

                        image.setImageBitmap(BitmapFactory.decodeFile(HomeActivity.randomImage));
                        image.startAnimation(anim);
                    } else {

                        displayRandomImage(new View(context));

                    }


                }
            }

            running = false;

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

            ArrayList<String> htmlLines = new ArrayList<>();


            try {


                if (isNum) {
                    Log.i("DataCheck", "searching for monster with number " + monsterNum);

                    htmlLines.add("/monster/" + monsterNum);

                } else {

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

                    if (htmlLines.size() == 1) {//launch MonsterPageActivity if only one result is found.

                        Log.i("DataCheck", "gathering monster data");

                        new MonsterGrabber(getMonUrl(htmlLines.get(0)), context, getActivity()).execute();

                    } else {//creates a results fragments if more than one result is found.

                        Log.i("DataCheck", "creating results page intent");


                        resultsFragment = new ResultsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("monsters", htmlLines);
                        resultsFragment.setArguments(bundle);


                        resultsFragment.show(getFragmentManager(), "Results");


                        Log.i("DataCheck", "added found monsters");


                    }


                } else {
                    Log.i("Datacheck", "No results found");
                    //no results
                    error = true;

                }
            } catch (Exception ignored) {

                ignored.printStackTrace();
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
                Toast.makeText(context.getApplicationContext(), "Network Error\nMonster failed to donwnload", Toast.LENGTH_SHORT).show();
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
        * This method replaces all spaces with a "+" in a string.
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
   *
   * This is the default error method when no monster is found.
   *
   * */
    public void error() {


        AlertDialog.Builder error = new AlertDialog.Builder(context);
        error.setTitle("Monster Not Found");
        error.setMessage("Please try another name or number.");
        error.setPositiveButton("OK", null);
        setLoading(false);
        error.show();


    }

    /*
    *
    * This method is used to check is the Text entered into
    * the search is invalid even before it connects to the
    * internet.
    *
    * */

    public boolean invalid(String potential) {
        if (potential.equals(""))
            return true;

        int count = 0;
        for (int a = 0; a < potential.length(); a++) {
            if (potential.charAt(a) >= '0' && potential.charAt(a) <= '9')
                count++;
        }

        if (potential.length() == count) {

            long number = Integer.parseInt(potential);

            if (number > 9999)
                return true;

            if (number == 0 || number > 1535)
                return true;

        } else {

            if (potential.length() <= 2)
                return true;

        }


        return false;

    }


    /*
    *
    * This method makes the loading ProgressBars
    * visible or invisible.
    *
    * */
    public void setLoading(boolean isLoading) {
        ProgressBar bar;


        if (isLoading) {
            bar = (ProgressBar) view.findViewById(R.id.monsterLoading);
            bar.setVisibility(View.VISIBLE);

        } else {

            EditText text = (EditText) view.findViewById(R.id.monsterText);
            text.setText("");
            bar = (ProgressBar) view.findViewById(R.id.monsterLoading);
            bar.setVisibility(View.INVISIBLE);
        }

    }

    /*
    *
    * This is the connected method connected to
    * the Main search button on the SearchFragment.
    *
    * */
    public void showResults(View v) {
        if (!MonsterGrabber.running) {
            EditText text = (EditText) view.findViewById(R.id.monsterText);
            String num = text.getText().toString();
            Log.i("Search Fragment", "found " + num);
            if (invalid(num)) {

                error();
            } else {
                setLoading(true);
                new DataCheck(num, context).execute();
            }
        }
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
