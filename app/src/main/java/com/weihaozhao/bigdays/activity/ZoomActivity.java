package com.weihaozhao.bigdays.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weihaozhao.bigdays.R;
import com.weihaozhao.bigdays.db.Events;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.List;


/**
 * Created by Zhao Weihao on 2017/4/2.
 * Updated by Zhao Weihao on 2017/5/1
 */

public class ZoomActivity extends AppCompatActivity {

    public static final String EVENTS_ID="events_id";

    private int eventsIdZoom;

    private TextView eventsName;

    private TextView daysLeft;

   private Calendar calendar;

    private TextView beforeDay;

    private int year,month,day;

    private RelativeLayout relativeLayout;

    private TextView showDate;

    private Intent intent;

    private Toolbar toolbar;

    private ActionBar actionBar;

    private Events events;

    private AlertDialog.Builder dialog;

    private final int REQUEST_CODE_CHOOSE=0;

    private List<Uri> mSelected;

    private ImageView mImageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zoom_data);

        initViews();

        getDays();

        refresh();

    }

    @Override
    protected void onRestart() {

        super.onRestart();

        refresh();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_zoom,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:

                buildDialog();


                break;
            case R.id.update:
                Intent toUpdate=new Intent(ZoomActivity.this,UpdateDataActivity.class);
                toUpdate.putExtra(UpdateDataActivity.EVENTS_NAME,events.getEventsname());
                toUpdate.putExtra(UpdateDataActivity.EVENTS_ID,eventsIdZoom);
                toUpdate.putExtra(UpdateDataActivity.EVENTS_YEAR,events.getYear());
                toUpdate.putExtra(UpdateDataActivity.EVENTS_MONTH,events.getMonth());
                toUpdate.putExtra(UpdateDataActivity.EVENTS_DAY,events.getDay());
                startActivity(toUpdate);
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.imageChange:
                // request permission
                if(ContextCompat.checkSelfPermission(ZoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ZoomActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    showImageSelector();
                }

                break;
                default:
        }
        return true;
    }

    private void showImageSelector() {

        Matisse.from(ZoomActivity.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private long dayLeft(int y,int m,int d){


        calendar.set(y,m-1,d);

        long time2=calendar.getTimeInMillis();

        calendar.set(year,month-1,day);

        long time1=calendar.getTimeInMillis();

        long subDay=(time2-time1)/(1000*60*60*24);

        return subDay;

    }

    private void initViews() {

        intent = getIntent();

        eventsIdZoom = intent.getIntExtra(EVENTS_ID, 0);

        toolbar = (Toolbar) findViewById(R.id.toolbar_zoom);

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        eventsName = (TextView) findViewById(R.id.events_name_zoom);

        daysLeft = (TextView) findViewById(R.id.days_left_zoom);

        beforeDay = (TextView) findViewById(R.id.before_day_zoom);

        showDate = (TextView) findViewById(R.id.show_date);

        relativeLayout = (RelativeLayout) findViewById(R.id.zoom_layout);

        mImageView= (ImageView) findViewById(R.id.zoom_bg);




    }



    private void getDays(){

        calendar=Calendar.getInstance();

        year=calendar.get(Calendar.YEAR);

        month=calendar.get(Calendar.MONTH)+1;

        day=calendar.get(Calendar.DAY_OF_MONTH);

    }

    private void refresh(){

        events = DataSupport.find(Events.class, eventsIdZoom);

        showDate.setText(String.valueOf(events.getYear())+"-"+String.valueOf(events.getMonth())+"-"+String.valueOf(events.getDay()));

        eventsName.setText(events.getEventsname());

        long days=dayLeft(events.getYear(),events.getMonth(),events.getDay());

        if(days>0){
            beforeDay.setText(R.string.still_text);
            daysLeft.setText(String.valueOf(days));
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        }else if(days==0){
            beforeDay.setText(R.string.is_text);
            daysLeft.setText(R.string.zoom_today_text);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }else{
            beforeDay.setText(R.string.past_text);
            daysLeft.setText(String.valueOf(0-days));
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.orange));
        }

        SharedPreferences preferences=getSharedPreferences("data",MODE_PRIVATE);

        String imageUriString=preferences.getString("imageUri",null);



        if(imageUriString!=null){
            Uri imageUri=Uri.parse(imageUriString);
            mImageView.setImageURI(imageUri);
        }else{
            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.image));
        }






//        mImageView.setImageURI(events.getImageUri());

//        if(events.getImageUri()==null){
//            Uri uri = Uri.parse("android.resource://com.weihaozhao.bigdays/drawable/image");
//            mImageView.setImageURI(uri);
//
//        }else{
//            mImageView.setImageURI(events.getImageUri());
//
//
//        }


    }

    private void buildDialog(){
        dialog=new AlertDialog.Builder(ZoomActivity.this);
        dialog.setTitle(R.string.ok_text);
        dialog.setMessage(R.string.sure_detele_text);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DataSupport.delete(Events.class, eventsIdZoom);

                Toast.makeText(ZoomActivity.this, R.string.delete_successfully, Toast.LENGTH_SHORT).show();

                finish();

            }
        });
        dialog.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        dialog.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);

            mImageView.setImageURI(mSelected.get(0));

            SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putString("imageUri",mSelected.get(0).toString());
            editor.apply();

            Toast.makeText(this, "Set up successfully", Toast.LENGTH_SHORT).show();

        }else if(requestCode!=RESULT_OK&&requestCode!=RESULT_CANCELED){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    showImageSelector();
                }else{
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
        }
    }


}
