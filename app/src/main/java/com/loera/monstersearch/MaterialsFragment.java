package com.loera.monstersearch;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MaterialsFragment extends DialogFragment {

    private String[] evo;
    private String[] pics;
    private String[] asc;
    private Context context;
    private View view;
    private boolean displayed;

    private MaterialsListener mListener;

    public static MaterialsFragment newInstance(String param1, String param2) {
        MaterialsFragment fragment = new MaterialsFragment();

        return fragment;
    }

    public MaterialsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        displayed = false;


        String evoS = getArguments().getString("evo");
        String ascS = getArguments().getString("asc");
        String picS = getArguments().getString("pics");

        if(evoS != null)
            evo = evoS.split("END");

        if(ascS != null)
            asc = ascS.split("END");

        if(picS != null)
            pics = picS.split("END");






        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_materials, container, false);
    }

    public void onStart(){

        super.onStart();

        view = getView();
        context = getActivity();

        if(!displayed)
        displayMaterials();



    }




        public void displayMaterials() {

            LinearLayout l = (LinearLayout)view.findViewById(R.id.materialsLayout);

            TextView text;

            if(evo != null){


            l.setOrientation(LinearLayout.VERTICAL);

            text = new TextView(context);

            text.setText("Evolution");
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                text.setTypeface(null, Typeface.BOLD);

                text.setTextColor(Color.WHITE);
                text.setTextSize(20);

                l.addView(text);

            for (int i = 0; i < evo.length; i++) {

                String m = evo[i];
                text = new TextView(context);

                text.setText(" " +m);
                text.setTextSize(20);
                text.setPadding(20,0,20,0);
                if(m != null)
                text.setCompoundDrawablesWithIntrinsicBounds(getEvoId(m), 0, 0, 0);

                l.addView(text);


            }
        }

            if(asc != null){


                text = new TextView(context);
                text.setText("Ascension");

                text.setTextColor(Color.WHITE);
                text.setTypeface(null, Typeface.BOLD);
                text.setTextSize(20);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                l.addView(text);

                GridView grid = new GridView(context,null);
                grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                grid.setNumColumns(2);
                grid.setColumnWidth(65);
                grid.setVerticalSpacing(20);
                grid.setHorizontalSpacing(20);
                grid.setGravity(Gravity.CENTER);


                grid.setAdapter(new MaterialsImageAdapter(context));



                l.addView(grid);




            }

            displayed = true;

        }


        public int getEvoId(String s){


            int stop  = s.indexOf(" x ");
            if(stop == -1)
                stop = s.indexOf(" X ");
            String check = "";
            for(int a = 0;a<s.length();a++){

                if(a == stop)
                    break;
                else
                    check += s.charAt(a);

            }

            Log.i("TEST", check.trim().toLowerCase());

            int drawable;



            switch(check.trim().toLowerCase()){

                case "divine sharl": drawable = R.drawable.divinesharl;
                    break;
                case "max stoan": drawable = R.drawable.maxstoan;
                    break;
                case "stoan": drawable = R.drawable.stoan;
                    break;
                case "mini stoan": drawable = R.drawable.ministoan;
                    break;
                case "red stoan": drawable = R.drawable.redstoan;
                    break;
                case "red sharl" : drawable = R.drawable.redsharl;
                    break;
                case "blue stoan" : drawable = R.drawable.bluestoan;
                    break;
                case "blue sharl" : drawable = R.drawable.bluesharl;
                    break;
                case "green stoan" : drawable = R.drawable.greenstoan;
                    break;
                case "green sharl" : drawable = R.drawable.greensharl;
                    break;
                case "light stoan" : drawable = R.drawable.lightstoan;
                    break;
                case "light sharl" : drawable = R.drawable.lightsharl;
                    break;
                case "dark stoan" : drawable = R.drawable.darkstoan;
                    break;
                case "dark sharl" : drawable = R.drawable.darksharl;
                    break;

                default: drawable = R.drawable.stoan;

                 }

            return drawable;


        }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mListener = (MaterialsListener)activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public interface MaterialsListener {
        public void setLoading(boolean isLoading);
    }



    private class MaterialsImageAdapter extends BaseAdapter {

        Context context;

        public MaterialsImageAdapter(Context c){

            this.context = c;
        }

        public int getCount() {
            return asc.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text;

            int width,paramWidth,paramHeight;
            float scale = getResources().getDisplayMetrics().density;

            switch (Home.screenSize){

                case Configuration.SCREENLAYOUT_SIZE_LARGE:

                    width = 90;
                    paramWidth = 200;
                    paramHeight = 100;

                    break;

                case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                    width = 50;
                    paramWidth = 100;
                    paramHeight = 50;

                    break;

                default: width = 170;
                    paramWidth = 370;
                    paramHeight = 270;


            }

            if(scale <= 1.5){
                width = width - (width/2);
                paramWidth = paramWidth - (paramWidth/2);
                paramHeight = paramHeight - (paramHeight/3);
            }

            if(convertView  == null){

                text = new TextView(context);
                text.setLayoutParams(new GridView.LayoutParams(paramWidth,paramHeight));

            }else{

                text = (TextView)convertView;
            }

            Bitmap b = BitmapFactory.decodeFile(pics[position]);
            Drawable d = new BitmapDrawable(context.getResources(),b);
            d.setBounds(0,0,width,width);
            text.setPadding(0,0,0,0);
            text.setCompoundDrawables(d, null, null, null);
            text.setText(" x " + getAmount(asc[position]));
            text.setTextSize(22);

            text.setId(position);
            text.setGravity(Gravity.RIGHT);
            text.setGravity(Gravity.CENTER_VERTICAL);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String clickedMon = asc[v.getId()];
                    mListener.setLoading(true);

                    clickedMon = "/" + clickedMon.substring(0,clickedMon.indexOf(" "));

                    Log.i("Materials","Selected " + clickedMon);

                    Toast.makeText(context.getApplicationContext(),"Loading Monster Data",Toast.LENGTH_SHORT).show();
                    new DataGrabber(clickedMon,context,getActivity()).execute();



                    dismiss();

                }
            });


            return text;
        }
    }
    public String getAmount(String s){

        return s.substring(s.indexOf(" ")+ 1);

    }


}
