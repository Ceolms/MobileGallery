package com.uqac.mobile.gallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class Gallery extends View {

    private static String TAG = "View Gallery";
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private GestureDetector mGestureListener;
    private int firstIndex = 0;
    private float distanceScroll;
    private boolean isScrolling;
    private int maxPerLine = 7;
    private int maxPerColumn = 11;
    Activity activity;
    ArrayList<String> listImages;
    Paint p;

    public Gallery(Context context) {
        super(context);
        activity = (Activity) context;
        listImages = scanDeviceForImages();
        p = new Paint();
		//zoom listener
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		//scroll listener
        mGestureListener = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                distanceScroll = distanceY;
                isScrolling = true;

                return true;
            }
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        DisplayMetrics metrics = new DisplayMetrics();
            //get display information
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthScreen=metrics.widthPixels ;
        int heightScreen=metrics.heightPixels + 400;

        p.setDither(true);
        int x = 0;
        int y = 0;

        int widthImage = widthScreen / maxPerLine;
        int heightImage = heightScreen / maxPerColumn;


        for(int i = firstIndex ; i <= maxPerLine*maxPerColumn + firstIndex; i ++)
        {
            String s = listImages.get(i);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			//optimisation
            if(maxPerLine >5)
            bmOptions.inSampleSize = 32;
            else if(maxPerLine >3) bmOptions.inSampleSize = 16;
            else bmOptions.inSampleSize = 8;
            Bitmap bd = BitmapFactory.decodeFile(s,bmOptions);

            if(bd != null)
            {
                Bitmap bdSized = Bitmap.createScaledBitmap(bd, widthImage, heightImage,false);


                canvas.drawBitmap(bdSized,x,y,p);

                x += widthImage;

                if(x >= widthImage)
                {
                    x = 0;
                    y += heightImage;
                }
            }

        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.

        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(isScrolling ) {
                isScrolling  = false;
                setScroll();
            }
        }else
        {
            mGestureListener.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
        }


        return true;
    }

	//change the view  : the first index of the image to be shown is the first index of the list + number of images per line
    private void setScroll()
    {
        if(distanceScroll > 0)
        {
            firstIndex += maxPerLine; //scroll to bottom
        }
        else
        {
            firstIndex -= maxPerLine;
            if(firstIndex <0) firstIndex = 0; // scroll to top
        }
        this.invalidate();
    }
	
	//scan images in Gallery , DCIM and other public folders 
    private ArrayList<String> scanDeviceForImages()
    {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

	//scale Listener reduce the number of image per line , so they can be in larger size
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mScaleFactor *= detector.getScaleFactor();

                // Don't let the object get too small or too large.
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

                Log.d(TAG,"---scaleFactor : " + mScaleFactor);
                if(mScaleFactor > 1 && maxPerLine>1) {maxPerLine -= 1;maxPerColumn -= 1;}
                else if(maxPerLine < 7){maxPerLine += 1;maxPerColumn += 1;}
                invalidate();
                return true;
            }
    }
}
