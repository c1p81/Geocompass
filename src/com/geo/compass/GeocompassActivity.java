package com.geo.compass;

import java.text.DecimalFormat;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GeocompassActivity extends Activity {
	private static final String TAG = "Compass";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mValues;
    private double azimuth;
    private double pitch;
    private double roll;

    private final SensorEventListener mListener = new SensorEventListener() {
        private double cosalfa;
		private double cosbeta;
		private double dir_maxpendenza;
		private double ang_maxpendenza;
		private Bitmap resizedBitmap;
		
		public double converti2deg(double angolo) {
			return angolo*(180/Math.PI);
		}
		
		public double converti2rad(double angolo) {
			return angolo*(Math.PI/180);
		}

		
		public void onSensorChanged(SensorEvent event) {
            if (Config.LOGD) Log.d(TAG,"sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            mValues = event.values;
            azimuth = converti2rad(event.values[0]);
            pitch = converti2rad(event.values[1]);
            roll = converti2rad(event.values[2]);

            cosalfa = Math.cos(roll-(Math.PI/2));
            cosbeta = Math.cos(pitch-(Math.PI/2));
            dir_maxpendenza = converti2deg(Math.atan(cosalfa/cosbeta));    
        	
            if (converti2deg(pitch) > 0) dir_maxpendenza =  dir_maxpendenza + 90;
            //if ((converti2deg(pitch) > 0) && (converti2deg(roll)<0)) dir_maxpendenza =  dir_maxpendenza + 180;
            
            //dir_maxpendenza = (converti2deg(azimuth)+dir_maxpendenza)%360;
            
            ang_maxpendenza = 90-converti2deg(Math.acos(Math.sqrt((cosalfa*cosalfa)+(cosbeta*cosbeta))));
            if (Double.isNaN(ang_maxpendenza)) ang_maxpendenza = 90;
            

            DecimalFormat df = new DecimalFormat("##");
        	txt_strike.setText(df.format(dir_maxpendenza));
        	txt_dip.setText(df.format(ang_maxpendenza));
        	
        	int width = bitmapOrg.getWidth();
            int height = bitmapOrg.getHeight();
            int newWidth = 50;
            int newHeight = 50;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            matrix.postRotate((float) dir_maxpendenza);
        	resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true); 
            BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
            imgview.setImageDrawable(bmd);
            imgview.setScaleType(ScaleType.CENTER);


            if (Config.LOGD) Log.d(TAG,"Strike " + dir_maxpendenza + ",Dip  "+ang_maxpendenza);
            if (Config.LOGD) Log.d(TAG,"Pitch " + converti2deg(pitch) + ", Roll "+converti2deg(roll));
            }
        

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

	private TextView txt_strike;
	private TextView txt_dip;

	private Bitmap bitmapOrg;

	private Matrix matrix;

	private ImageView imgview;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        txt_strike = (TextView)findViewById(R.id.txtstrike);
        txt_dip = (TextView)findViewById(R.id.txtdip);
        imgview = (ImageView)findViewById(R.id.imageView1);
        bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.dritta);
    }

    @Override
    protected void onResume()
    {
        if (Config.LOGD) Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop()
    {
        if (Config.LOGD) Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }
   
}