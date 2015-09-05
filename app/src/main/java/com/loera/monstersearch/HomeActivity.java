package com.loera.monstersearch;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
*
* This is the Main Activity of the entire Application.
* It is home to the SearchFragment and the MonsterBoxFragment.
*
* This is where your search first begins :)
*
* */


public class HomeActivity extends AppCompatActivity implements ResultsFragment.DialogListener, MonsterGrabber.LoadingListener{


    public static String randomImage;
    public static int screenSize;
    private ListView list;
    public static String currentFrag,lastFrag;
    private static int totalChanged;
    private static Fragment fragment;
    private ActionBarDrawerToggle drawerToggle;
    private static Menu menu;
    public static boolean newFav;
    private ProgressDialog monsterSync;
    private static final String DATACOMPARE = "DATACOMPARE";
    Context context;

    /*
    *
    * This is the interface method "setLoading" which either enables
    * or disables loading Views such as progress bars.
    *
    * */

    public void setLoading(boolean isLoading) {

         ((SearchFragment)fragment).setLoading(isLoading);

    }

/*
*
* This class is the listener for switching between
* the SearchFragment, MonsterBoxFragment, or launching the
* SettingsActivity.
*
* */

    private class DrawerItemListener implements ListView.OnItemClickListener{


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // "currentFrag" is the currently displayed fragment.
            //By default, the fragment is the SearchFragment.

            if(currentFrag == null)
                currentFrag = "Strike Search";

            DrawerLayout dl = (DrawerLayout)findViewById(R.id.homeLayout);
            dl.closeDrawer(Gravity.LEFT);

            list.clearChoices();
            list.requestLayout();

            boolean pass = true;

            //ints for transition animations between fragments
            int in = 0,out = 0;

            //This switch changes fragments by looking at the
            //currentFrag and switching to the new appropriate one,
            //if any.

            switch(position){

                case 0:
                    if(!currentFrag.equals("Strike Search")){
                    fragment = new SearchFragment();
                    lastFrag = currentFrag;
                    currentFrag = "Strike Search";
                    in = R.anim.slideinleft;
                    out = 0;
                        getSupportActionBar().setTitle("Strike Search");

                    }else{

                       pass = false;
                    }
                    break;
                case 1:

                    if(!currentFrag.equals("Monster Box")) {
                        fragment = new MonsterBoxFragment();
                        lastFrag = currentFrag;
                        currentFrag = "Monster Box";
                        in = R.anim.slideinup;
                        out = 0;
                        getSupportActionBar().setTitle("Monster Box");
                    }else{

                       pass = false;
                    }

                    break;

                case 2:

                    Intent intent = new Intent(context,SettingsActivity.class);
                    context.startActivity(intent);
                    pass = false;
                }

            if(pass){ //If fragment needs to switch
            FragmentTransaction ft  = getFragmentManager().beginTransaction();
            ft.add(R.id.contentFrame, fragment);
            ft.setCustomAnimations(in, out);
            ft.show(fragment);
            ft.addToBackStack(currentFrag);
            ft.commit();
            }
         }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setEnterTransition(new Explode().setDuration(500));
            getWindow().setExitTransition(new Explode().setDuration(500));
            getSupportActionBar().setElevation(0);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#1976D2"));
            this.setTaskDescription(new ActivityManager.TaskDescription(null,null,Color.parseColor("#1976D2")));
             }

        if(savedInstanceState != null)
            currentFrag = savedInstanceState.getString("fragment");
        //Removes any temporary images at startup
        removeImages();
        //This global variable is shared with the entire app
        // and is used to scale images based on screen size
        screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
        setContentView(R.layout.activity_home);

        DrawerLayout dl = (DrawerLayout)findViewById(R.id.homeLayout);
        drawerToggle = new ActionBarDrawerToggle(this,dl,R.string.open_drawer,R.string.close_drawer){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                invalidateOptionsMenu();

            }

            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                invalidateOptionsMenu();

            }


        };

        dl.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        context = this;

        //Checking to see if there were
        // any previous fragments.

        if(getFragmentManager().getBackStackEntryCount()== 0){
         FragmentTransaction ft  = getFragmentManager().beginTransaction();
         if(currentFrag == null)
          fragment = new SearchFragment();
           else{
             switch (currentFrag){

                case "Strike Search":
                    fragment = new SearchFragment();
                    break;
                case "Monster Box":
                    fragment = new MonsterBoxFragment();
                 if(menu != null){
                    MenuItem sync =  menu.findItem(R.id.syncFavButton);
                    sync.setVisible(true);
                    }
                  }

                 }
        ft.replace(R.id.contentFrame,fragment);
        ft.commit();
       }

        //Initialize the drawer text and
        //scale the icons accordingly

        final String[]  titles  = getResources().getStringArray(R.array.listArray);
        list = (ListView)findViewById(R.id.homeDrawer);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, titles){
            @Override
        public View getView(int position,View convertView,ViewGroup parent){

                View v = super.getView(position,convertView,parent);

                int draw = 0;

                switch(position){
                    case 0:
                        draw = R.drawable.ic_magnify_black;
                        break;
                    case 1:
                        draw = R.drawable.ic_archive_black;
                        break;
                    case 2:
                        draw = R.drawable.ic_cog_black;
                }


                Bitmap bit = BitmapFactory.decodeResource(context.getResources(),draw);
                Drawable icon = new BitmapDrawable(context.getResources(),bit);

                int iconSize;
                int padding;
                float scale = getResources().getDisplayMetrics().density;

                switch (screenSize){

                    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                        iconSize = 30;
                        padding = 10;
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_LARGE:
                        iconSize = 50;
                        padding = 20;
                        break;

                    default: iconSize = 100;
                             padding  = 25;
                                        }

                if(scale <= 1.5)
                    iconSize /=2;
                
                icon.setBounds(0,0,iconSize,iconSize);

                ((TextView) v).setCompoundDrawables(icon, null, null, null);
                ((TextView) v).setCompoundDrawablePadding(padding);
                ((TextView)v).setTextColor(Color.parseColor("#000000"));

            return v;
            }



    });

        list.setOnItemClickListener(new DrawerItemListener());
        MonsterBoxFragment.readFavoriteMonsters(this);
   }

    public void onPostCreate(Bundle savedInstanceState){

        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

    }

    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        //save any fragments before destruction
        state.putString("fragment",currentFrag);

    }

    public void onConfigurationChanged(Configuration newConfig){

        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

    }

    public void onRestart(){

        super.onRestart();

        // the global variable "newFav" is true whenever the user
        //favorites a new Monster from the MonsterBoxFragment
        //in order to update the GridView to display new Monsters.
        if(newFav)
            MonsterBoxFragment.readFavoriteMonsters(this);

    }


    public void onBackPressed(){

        //This if statement determines whether to switch back to a
        //previous fragment or to close the app.

        if(getFragmentManager().getBackStackEntryCount() != 0 && !lastFrag.equals("")){

           getSupportActionBar().setTitle(lastFrag);

            currentFrag = lastFrag;
            int in = 0, out = 0;

           MenuItem sync = null;
            if(menu != null) {
                sync = menu.findItem(R.id.syncFavButton);
            }
            switch(currentFrag){

                case "Strike Search": fragment = new SearchFragment();
                            in = R.anim.slideinleft;
                            out = R.anim.slideoutright;
                    if(sync!=null)
                    sync.setVisible(false);

                    break;
                case "Monster Box": fragment = new MonsterBoxFragment();

                    in  = R.anim.slideinright;
                    out = R.anim.slideoutleft;
                    if(sync!=null)
                    sync.setVisible(true);
            }
            getFragmentManager().beginTransaction().setCustomAnimations(in,out).replace(R.id.contentFrame,fragment).commit();
            lastFrag = "";
        }else{
           super.onBackPressed();
          }
    }

    /*
    *
    * This method opens the "Temp" folder, which contains all
    * temporary data, and deletes everything!
    * Use with caution.
    *
    * */

    public void removeImages(){

       File temp = this.getDir("temp",Context.MODE_PRIVATE);

        if(!temp.exists())
            temp.mkdirs();

        File [] files = temp.listFiles();

        if(files != null && files.length != 0)
          for(File f:files)
              f.delete();
    }

   /*
   *
   * This method is used purely to update the
   * ProgressDialog created by MonsterSync
   *
   * */

    public void updateMonsterSync(boolean changed,int number){
       if(changed){
            totalChanged++;
            monsterSync.setMessage("Total Changed: "+totalChanged+"\nLast Updated:\n"+MonsterBoxFragment.favs.get(number).name);
            monsterSync.incrementProgressBy(1);

        }else{
            monsterSync.incrementProgressBy(1);
         }

        if(monsterSync.getProgress() == MonsterBoxFragment.favs.size()){
            monsterSync.dismiss();
            if(totalChanged > 0)
            Toast.makeText(context,totalChanged + " Monsters Updated!",Toast.LENGTH_SHORT).show();
            else
            Toast.makeText(context,"No Updates Found",Toast.LENGTH_SHORT).show();


        }


    }

    /*
    *
    * Both displayRandomImage and showResults are
    * interface methods implemented by SearchFragment.
    *
    * */

    public void displayRandomImage(View v){

        ((SearchFragment)fragment).displayRandomImage(v);

    }

    public void showResults(View view){

        ((SearchFragment)fragment).showResults(view);

    }

    /*
    *
    * MonsterSync is used to see if any offline Monsters stored have outdated information.
    *
    * Steps:
    * 1. A connection is established to the appropriate Monster's web page
    *
    * 2. As Monster data is stored, it is compared to the reference monster.
    *
    * 3. If data is ever different from the reference Monster and the newly downloaded Monster,
    * the boolean "changed" becomes true to signify the data needs to updated.
    *
    *
    * */

    public class MonsterSync extends AsyncTask<Void,Void,Void> {

        String message;
        ArrayList<Monster> mons;
        Monster monsterData,refernece;
        Context context;
        public boolean cancelled,running,stop;
        private boolean changed;
        int number;



        public MonsterSync(int n, Context c){

            this.number = n;
            this.context = c;
            cancelled = false;
            changed = false;
            running = true;


        }


        @Override
        protected Void doInBackground(Void... params) {

            //default values for every Monster that does not change
            mons = new ArrayList<>();
            ArrayList<String> htmlLines = new ArrayList<>();
            monsterData = new Monster();
            refernece = MonsterBoxFragment.favs.get(number);
            monsterData.evoMat = refernece.evoMat;
            monsterData.ascMat = refernece.ascMat;
            monsterData.ascLinks = refernece.ascLinks;
            monsterData.bitmap = refernece.bitmap;
            monsterData.thumb = refernece.thumb;
            monsterData.num = refernece.num;
            monsterData.favorited = true;

                   try {

                    URL url = new URL("http://www.strikeshot.net/monster/" + refernece.num);
                    HttpURLConnection connect2 = (HttpURLConnection) url.openConnection();
                    connect2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
                    connect2.connect();
                    BufferedReader htmlStream = new BufferedReader(new InputStreamReader(connect2.getInputStream()));


                    Log.i("DataGrabber", "accessing page");


                    String inputLine;

                    while ((inputLine = htmlStream.readLine()) != null)
                        htmlLines.add(inputLine);

                    htmlStream.close();


                }catch(FileNotFoundException ex){

                    ex.printStackTrace();
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
                            Log.i(DATACOMPARE,monsterData.name);
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

                            if(!changed)
                            if(!level.equals(refernece.maxLevel) || !monsterData.name.equals(refernece.name)){
                                changed = true;
                            Log.i(DATACOMPARE,"Changed name/level");
                            }

                        }


                        if(curData.contains("monster-atrri")){
                            if(curData.contains("bounce"))
                                monsterData.impact = "Bounce";
                            else
                                monsterData.impact = "Pierce";


                            if(!changed)
                            if(!monsterData.impact.equals(refernece.impact)){
                                changed = true;
                                Log.i(DATACOMPARE,"Changed impact");
                            }

                        }

                        if (curData.contains("Class: ")){
                            monsterData.monClass = addBetweenTags(curData,curData.indexOf("</span>")+8);

                            if(!changed)
                                if(!monsterData.monClass.equals(refernece.monClass)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed class");
                                }

                        }

                        if (curData.contains("Type: ")){
                            monsterData.type= addBetweenTags(curData,curData.indexOf("</span>")+7);
                            if(!changed)
                                if(!monsterData.type.equals(refernece.type)){
                                    changed =true;
                                    Log.i(DATACOMPARE,"Changed type");
                                }
                        }

                        if (curData.contains("Max Luck :")){
                            monsterData.maxLuck = addBetweenTags(curData,curData.indexOf("</span>")+7);

                            if(!changed)
                                if(!monsterData.maxLuck.equals(refernece.maxLuck)) {
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed maxLuck");
                                }
                        }

                        if (curData.contains("Ability: ")){
                            monsterData.ability = addBetweenTags(curData,curData.indexOf("</span>")+7);
                            if(htmlLines.get(m+1).contains("Gauge Shot"))
                                monsterData.ability = monsterData.ability+"\nGauge:  "+ addBetweenTags(htmlLines.get(m+1),htmlLines.get(m+1).indexOf("</span>")+7);

                            if(!changed)
                                if(!monsterData.ability.equals(refernece.ability)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed ability");
                                }
                        }

                        if(curData.contains(">Health<")){
                            monsterData.maxHealth = addBetweenTags(htmlLines.get(m+2),htmlLines.get(m+2).indexOf(">")+1);
                            monsterData.plusHealth = addBetweenTags(htmlLines.get(m+3),htmlLines.get(m+3).indexOf(">")+1);

                            if(!changed)
                               if(!monsterData.maxHealth.equals(refernece.maxHealth) ||
                                  !monsterData.plusHealth.equals(refernece.plusHealth)){
                                   changed = true;
                                   Log.i(DATACOMPARE,"Changed healtj");
                               }

                        }
                        if(curData.contains(">Attack<")){
                            monsterData.maxAttack = addBetweenTags(htmlLines.get(m+2),htmlLines.get(m+3).indexOf(">")+1);
                            monsterData.plusAttack = addBetweenTags(htmlLines.get(m+3),htmlLines.get(m+3).indexOf(">")+1);

                            if(!changed)
                                if(!monsterData.maxAttack.equals(refernece.maxAttack) ||
                                   !monsterData.plusAttack.equals(refernece.plusAttack)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed attack");
                                }

                        }
                        if(curData.contains(">Speed<")){
                            monsterData.maxSpeed = addBetweenTags(htmlLines.get(m+2),htmlLines.get(m+2).indexOf(">")+1);
                            monsterData.plusSpeed = addBetweenTags(htmlLines.get(m+3),htmlLines.get(m+3).indexOf(">")+1);

                            if(!changed)
                                if(!monsterData.maxSpeed.equals(refernece.maxSpeed) ||
                                   !monsterData.plusSpeed.equals(refernece.plusSpeed)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed speed");
                                }

                        }



                        if (curData.contains("strike-shot-title")) {

                            monsterData.strikeName = addBetweenTags(curData,curData.indexOf(" - ")+3);

                            monsterData.strikeInfo = addBetweenTags(htmlLines.get(m + 1),htmlLines.get(m + 1).indexOf(">")+1);

                            monsterData.cooldown = addBetweenTags(htmlLines.get(m + 2),htmlLines.get(m + 2).indexOf("<span>")+6);

                            if(!changed)
                                if(!monsterData.strikeName.equals(refernece.strikeName)||
                                   !monsterData.strikeInfo.equals(refernece.strikeInfo)||
                                   !monsterData.cooldown.equals(refernece.cooldown)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed StrikeShot");
                                }

                        }

                        if (curData.contains("bumper-combo-title")) {

                            monsterData.bcName = addBetweenTags(curData,curData.indexOf(" - ")+3);

                            monsterData.bcInfo = addBetweenTags(htmlLines.get(m + 1),htmlLines.get(m + 1).indexOf(">")+1);

                            monsterData.bcPower = addBetweenTags(htmlLines.get(m + 2),htmlLines.get(m + 2).indexOf("<span>")+6);

                            if(!changed)
                                if(!monsterData.bcName.equals(refernece.bcName)||
                                   !monsterData.bcInfo.equals(refernece.bcInfo)||
                                   !monsterData.bcPower.equals(refernece.bcPower))
                                    changed = true;

                           }

                      /*
                      TODO: search Monster evolutions to find any changes.

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


                            if(!changed)
                                if(!monsterData.evoMat.equals(refernece.evoMat)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed evo mat");
                                }

                        }

                        if(curData.contains("ascen-material")){

                            String[] found = curData.split("href=");

                            monsterData.ascMat = "";

                            for(int g = 2;g<found.length;g+=2){

                                String preAmt = found[g].substring(found[g].indexOf("/monster"));
                                preAmt = preAmt.substring(preAmt.indexOf("<")+1);
                                char doubleDigit = preAmt.charAt(preAmt.indexOf("<")-2);
                                if(isNum(doubleDigit))
                                    preAmt =" "+ doubleDigit+preAmt.charAt(preAmt.indexOf("<")-1);
                                else
                                    preAmt =" "+ preAmt.charAt(preAmt.indexOf("<")-1);


                                monsterData.ascMat +=(getMonUrl( found[g])+ preAmt) + "END";
                            }


                            if(!changed)
                                if(!monsterData.ascMat.equals(refernece.ascMat)){
                                    changed = true;
                                    Log.i(DATACOMPARE,"Changed ascmat");
                                }


                        }*/
                      }
                }
            return null;
        }

        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if(!stop)
                if(cancelled) {
                    Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }else{


                    if(changed){

                        //removes outdated Monster from favorites and saves new file
                        // for a replacement.
                        MonsterBoxFragment.favs.remove(number);
                        MonsterBoxFragment.favs.add(number, monsterData);
                        createNewFile(monsterData);
                        Log.i("DataCompare","Updated Monster " + number);
                        updateMonsterSync(true,number);
                    }else{
                        updateMonsterSync(false,number);
                        Log.i("DataCompare","Monster " + number + " not updated");
                    }
                }

            running = false;



        }

        /*
        *
        * This method takes a Monster and makes a new data file to
        * be saved in the "Monsters" folder.
        *
        * */

        public void createNewFile(Monster m){
          try{
            File internal = context.getDir("Monsters",Context.MODE_PRIVATE);
            File monsterFolder = new File(internal,m.num);

            if(!monsterFolder.exists())
             monsterFolder.mkdirs();

            File dataFile = new File(monsterFolder,"data.txt");

            if(dataFile.exists())
                dataFile.delete();

            FileOutputStream out = new FileOutputStream(dataFile);

            PrintWriter writer  = new PrintWriter(out);

            writer.println("num\n" + m.num);
            writer.println("name\n" + m.name);
            writer.println("class\n" + m.monClass);
            writer.println("health");
            writer.println(m.maxHealth);
            writer.println(m.plusHealth);
            writer.println("attack");
            writer.println(m.maxAttack);
            writer.println(m.plusAttack);
            writer.println("speed");
            writer.println(m.maxSpeed);
            writer.println(m.plusSpeed);
            writer.println("impact\n" + m.impact);
            writer.println("ability\n" + m.ability);
            writer.println("type\n" + m.type);
            writer.println("strikename\n" + m.strikeName);
            writer.println("strikeinfo\n" + m.strikeInfo);
            writer.println("cooldown\n" + m.cooldown);
            writer.println("bcname\n" + m.bcName);
            writer.println("bcinfo\n" + m.bcInfo);
            writer.println("bcpower\n" + m.bcPower);
            writer.println(m.evoMat);
            writer.print(m.ascMat);

            writer.close();
   }catch(Exception e){
       e.printStackTrace();

   }



        }

        /*
        *
        * Method that returns data between HTML tags
        * with a starting point.
        *
        * */
        public String addBetweenTags(String s1,int start) {
            String sAns1 = "";
            int sCount1 = start;
            while (s1.charAt(sCount1) != '<') {
                sAns1 += s1.charAt(sCount1);
                sCount1++;
            }

            return sAns1;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        this.menu = menu;

        //Makes the MonsterSync ActionBar button visible or invisible
        //based on the currently visible fragement.
        MenuItem sync =  menu.findItem(R.id.syncFavButton);
          if(currentFrag != null)
            if(currentFrag.equals("Monster Box")){
              sync.setVisible(true);
              }else{
               sync.setVisible(false);
               }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        //Creates a dialog to initiate MonsterSync to update any
        // outdated data!
        if (id == R.id.syncFavButton) {
            AlertDialog.Builder confirmSync = new AlertDialog.Builder(context);
            confirmSync.setTitle("Monster Sync");
            confirmSync.setMessage("Check for any outdated Monster data?");
            confirmSync.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    monsterSync = new ProgressDialog(context);
                    monsterSync.setCanceledOnTouchOutside(false);
                    monsterSync.setTitle("Syncing Monster Data");
                    monsterSync.setMessage("Total Updated: 0\nLast Updated:");
                    totalChanged  = 0;
                    monsterSync.setMax(MonsterBoxFragment.favs.size());
                    monsterSync.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    monsterSync.setIndeterminate(false);
                    monsterSync.setProgress(0);
                    monsterSync.show();

                    for (int c  = 0;c<MonsterBoxFragment.favs.size();c++)
                       new MonsterSync(c,getApplicationContext()).execute();



                }

            });
            confirmSync.setNegativeButton("Cancel",null);
            confirmSync.show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}