package com.loera.monstersearch;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

import java.io.File;


public class Home extends AppCompatActivity implements ResultsFragment.DialogListener{


    public static String randomImage;
    public static int screenSize;
    private ListView list;
    private String currentFrag,lastFrag;
    private static Fragment fragment;
    private ActionBarDrawerToggle drawerToggle;
    public static boolean newFav;


    Context context;

    public void setLoading(boolean isLoading) {

         ((SearchFragment)fragment).setLoading(isLoading);

    }



    private class DrawerItemListener implements ListView.OnItemClickListener{


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(currentFrag == null)
                currentFrag = "Strike Search";

            DrawerLayout dl = (DrawerLayout)findViewById(R.id.homeLayout);
            dl.closeDrawer(Gravity.LEFT);

            list.clearChoices();
            list.requestLayout();

            boolean pass = true;

            int in = 0,out = 0;

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
                        fragment = new FavoriteFragment();
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

                    context.startActivity(new Intent(context,SettingsActivity.class));
                    pass = false;




            }

            if(pass){
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

        removeImages();
        screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        Log.i("Home", "Screensize " + screenSize + " was detected");


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
                    fragment = new FavoriteFragment();

            }

        }
        ft.add(R.id.contentFrame,fragment);

        ft.commit();
        }

        final String[]  titles  = getResources().getStringArray(R.array.listArray);
        list = (ListView)findViewById(R.id.homeDrawer);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, titles){
            @Override
        public View getView(int position,View convertView,ViewGroup parent){

                View v = super.getView(position,convertView,parent);

                int draw = 0;

                switch(position){
                    case 0:
                        draw = R.drawable.search_black;
                        break;
                    case 1:
                        draw = R.drawable.box;
                        break;
                    case 2:
                        draw = R.drawable.cog;
                }


                Bitmap bit = BitmapFactory.decodeResource(context.getResources(),draw);
                Drawable icon = new BitmapDrawable(context.getResources(),bit);
                icon.setBounds(0,0,100,100);

                ((TextView) v).setCompoundDrawables(icon, null, null, null);
                ((TextView) v).setCompoundDrawablePadding(50);
                ((TextView)v).setTextColor(Color.parseColor("#000000"));

            return v;
            }



    });


        list.setOnItemClickListener(new DrawerItemListener());

        FavoriteFragment.storeFavorites(this);

    }

    public void onPostCreate(Bundle savedInstanceState){

        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

    }

    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        state.putString("fragment",currentFrag);

    }

    public void onConfigurationChanged(Configuration newConfig){

        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

    }

    public void onRestart(){

        super.onRestart();

        if(newFav)
            FavoriteFragment.storeFavorites(this);

    }


    public void onBackPressed(){




        if(getFragmentManager().getBackStackEntryCount() != 0 && !lastFrag.equals("")){

           getSupportActionBar().setTitle(lastFrag);

            currentFrag = lastFrag;
            int in = 0, out = 0;
            switch(currentFrag){

                case "Strike Search": fragment = new SearchFragment();
                            in = R.anim.slideinleft;
                            out = R.anim.slideoutright;

                    break;
                case "Monster Box": fragment = new FavoriteFragment();

                    in  = R.anim.slideinright;
                    out = R.anim.slideoutleft;


            }

            getFragmentManager().beginTransaction().setCustomAnimations(in,out).replace(R.id.contentFrame,fragment).commit();

            lastFrag = "";



        }else{

            super.onBackPressed();

        }

    }

    public void onStop(){

        super.onStop();

    }

    public void removeImages(){

       File temp = this.getDir("temp",Context.MODE_PRIVATE);

        if(!temp.exists())
            temp.mkdir();

        File [] files = temp.listFiles();

          for(File f:files)
              f.delete();
    }





    public void displayRandomImage(View v){

        ((SearchFragment)fragment).displayRandomImage(v);

    }

    public void showResults(View view){

        ((SearchFragment)fragment).showResults(view);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
