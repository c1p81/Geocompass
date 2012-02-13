package com.geo.compass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.GpsStatus;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
//    private float[] mValues;
    private double azimuth;
    private double pitch;
    private double roll;
	private TextView txt_strike;
	private TextView txt_dip;

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
			 Bitmap bitmapOrg;

            azimuth = event.values[0];
            pitch = converti2rad(event.values[1]);
            roll = converti2rad(event.values[2]);

            cosalfa = Math.cos(roll-(Math.PI/2));
            cosbeta = Math.cos(pitch-(Math.PI/2));

            dir_maxpendenza = -converti2deg(Math.atan(cosalfa/cosbeta));    
            if ((converti2deg(pitch) < 0) && (converti2deg(roll)<=0)) dir_maxpendenza =  dir_maxpendenza + 180;
            if ((converti2deg(pitch) <= 0) && (converti2deg(roll)>0)) dir_maxpendenza =  dir_maxpendenza + 180;
            if ((converti2deg(pitch) > 0) && (converti2deg(roll)>0)) dir_maxpendenza =  dir_maxpendenza + 360;
            if ((converti2deg(pitch) == 0) && (converti2deg(roll)>0)) dir_maxpendenza =  270;
            if ((converti2deg(pitch) > 0) && (converti2deg(roll)==0)) dir_maxpendenza =  0;
            double dir_maxpendenza_reale = (azimuth+dir_maxpendenza)%360;
 
            ang_maxpendenza = 90-converti2deg(Math.acos(Math.sqrt((cosalfa*cosalfa)+(cosbeta*cosbeta))));
            if (Double.isNaN(ang_maxpendenza)) ang_maxpendenza = 90;
            

            DecimalFormat df = new DecimalFormat("##");
        	txt_strike.setText(df.format(dir_maxpendenza_reale));
        	txt_dip.setText(df.format(ang_maxpendenza));
        	
            if (chkbox.isChecked() == true) 
            	{
            	bitmapOrg = rovescia;
            	}
            else
            	{
            	bitmapOrg = dritta;
            	}
        	if (ang_maxpendenza <= 5) 
        					{
        					bitmapOrg = orizzontale;
        					txt_strike.setText(R.string.orizzontale);
        		        	txt_dip.setText("0");
        					}
            if (ang_maxpendenza >= 80)
            	{
            	bitmapOrg = verticale;
	        	double dir_maxpendenza1 = (dir_maxpendenza_reale + 270)%360;
	        	double dir_maxpendenza2 = (dir_maxpendenza_reale + 90)%360;
	        
	        	
	        	txt_strike.setText("Direction: "+df.format(dir_maxpendenza1)+"-"+df.format(dir_maxpendenza2));
	        	txt_dip.setText("90");
            	}
        	
                        
        	int width = bitmapOrg.getWidth();
            int height = bitmapOrg.getHeight();
            int newWidth = 50;
            int newHeight = 50;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            if ((ang_maxpendenza > 5) && (ang_maxpendenza <80)) matrix.postRotate((float) dir_maxpendenza);
            if (ang_maxpendenza > 80) 	matrix.postRotate((float) dir_maxpendenza+90);
            if (ang_maxpendenza <= 5) 	matrix.postRotate(0);
        	resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true); 
            BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
            imgview.setImageDrawable(bmd);
            imgview.setScaleType(ScaleType.CENTER);
 
            }
        

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


	private Bitmap dritta;
	private Bitmap rovescia;
	private Bitmap orizzontale;
	private Bitmap verticale;
	private Matrix matrix;
	private ImageView imgview;
	private CheckBox chkbox;
	private Button btn_save;

	private Writer datafilewriter;

	private Date dataora;

	private int option_menu;

	private long mLastLocationMillis;

	private Location mLastLocation;

	protected boolean isGPSFix;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
            private Object mLastLocation;
			private boolean isGPSFix;


			public void onGpsStatusChanged(int event) {
            	if( event ==  GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    if (mLastLocation != null)
                        isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;

                    if (isGPSFix) { // A fix has been acquired.
                          Toast.makeText( getApplicationContext(),"Gps Fix",Toast.LENGTH_SHORT ).show();
                    } else { // The fix has been lost.
                    	Toast.makeText( getApplicationContext(),"Gps Fix",Toast.LENGTH_SHORT ).show();
                    }
            	}

                if( event == GpsStatus.GPS_EVENT_FIRST_FIX){
    	            Toast.makeText( getApplicationContext(),"First Fix",Toast.LENGTH_SHORT ).show();

                }
            }
     };
        //turn screen always on without changing permission
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        txt_strike = (TextView)findViewById(R.id.txtstrike);
        txt_dip = (TextView)findViewById(R.id.txtdip);
        imgview = (ImageView)findViewById(R.id.imageView1);
        chkbox = (CheckBox)findViewById(R.id.chkrovescia);
        dritta = BitmapFactory.decodeResource(getResources(), R.drawable.dritta);
        rovescia = BitmapFactory.decodeResource(getResources(), R.drawable.rovescia);
        orizzontale = BitmapFactory.decodeResource(getResources(), R.drawable.orizzontale);
        verticale = BitmapFactory.decodeResource(getResources(), R.drawable.verticale);
        btn_save = (Button)findViewById(R.id.btnsave);
        btn_save.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){ 
        							salva(); 
        							}
		});
        txt_strike.setText("0");
        txt_dip.setText("0");
    }

    private void salva()
    {
    	String misura;
    	 try {
    	        File root = Environment.getExternalStorageDirectory();
    	        if (root.canWrite()){
    	            File data_file = new File(root, "geocompass.txt");
    	            FileWriter data_file_writer= new FileWriter(data_file,true);
    	            BufferedWriter out = new BufferedWriter(data_file_writer);
    	            
					if (chkbox.isChecked() == true) 
    	            	misura = "R";
    	            else
    	            	misura = "N";
    	            
					dataora = new Date();     
				    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
				    String stringa_tempo = formatter.format(dataora);
					
    	            out.write(stringa_tempo+";"+txt_strike.getText()+";"+txt_dip.getText()+";"+misura+"\n");
    	            Toast.makeText( getApplicationContext(),"Misura acquisita",Toast.LENGTH_SHORT ).show();

    	            out.close();	
    	            // suona per conferma
    	            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    	            r.play();
    	        }
    	    } catch (IOException e) {
    	    }
    	
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
  
    public boolean onCreateOptionsMenu(Menu menu)
    {
		menu.add(1,1,0,"Delete").setIcon(R.drawable.delete_item);
		menu.add(1,2,1,"Calibrate").setIcon(R.drawable.tool);
		menu.add(1,3,2,"Help").setIcon(R.drawable.help);
		return true;
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    		{
    	case 1:
        	
        	AlertDialog.Builder conferma_canc = new AlertDialog.Builder(this);
        	conferma_canc.setTitle("Please Confirm");
        	conferma_canc.setMessage("Delete data file?");
        	conferma_canc.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int id) {
        		   try{
                   	   File root = Environment.getExternalStorageDirectory();
                	   File data_file = new File(root, "geocompass.txt");
                       boolean deleted = data_file.delete();        			   
        		   }
        		  finally
        		  {
        			  
        		  }
        		  
        	  }
        	});
        	    	   	
           	conferma_canc.setNegativeButton("No", new DialogInterface.OnClickListener() {
          	  public void onClick(DialogInterface dialog, int id) {
        	    
          	  }
          	});
        AlertDialog alert = conferma_canc.create();
        alert.show();
        return true;
    	case 2:
    	    //Toast.makeText( getApplicationContext(),"tools",Toast.LENGTH_SHORT ).show();
    		return true;
    	case 3:
    		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
   		    dialog.setTitle(R.string.help_title);
   		    dialog.setMessage(R.string.help_text);
   		    dialog.show();
     		return true;
    	
    	}
    	return false;
    }
    
    public void onLocationChanged(Location location) {
        if (location == null) return;

        mLastLocationMillis = SystemClock.elapsedRealtime();

        // Do something.

        mLastLocation = location;
    }
}

