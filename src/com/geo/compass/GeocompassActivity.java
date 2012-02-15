package com.geo.compass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
	private TextView gpstxt;
	private double latitudine;
    private double longitudine;
    private long old_time;
    private boolean gps_fix;
	private ImageView img_gps;


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
			 Bitmap bitmapOrg = null;

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
 
            if (chkbox.isChecked()==(true)) bitmapOrg = rovescia;
            if (chkbox.isChecked()==(false)) bitmapOrg = dritta;
 
            /*
            if ((chkbox.isChecked() == true) && (ang_maxpendenza>5) && (ang_maxpendenza<80)) bitmapOrg = rovescia;
            if ((chkbox.isChecked() == false) && (ang_maxpendenza>5) && (ang_maxpendenza<80)) bitmapOrg = dritta;
        	*/
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
	        	txt_strike.setText("Dir: "+df.format(dir_maxpendenza1)+"-"+df.format(dir_maxpendenza2));
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
    
    
	 public class MyLocationListener implements LocationListener
	    {

		@Override
	    public void onLocationChanged(Location loc)
	    {
	    latitudine = loc.getLatitude();
	    longitudine = loc.getLongitude();
	    long tempo = loc.getTime();
	    if ((tempo-old_time < 5000) && (old_time > 0))
	    {
	    	gps_fix = true;
	    	img_gps.setImageResource(R.drawable.pallino_verde);
	    	gpstxt.setTextColor(Color.GREEN);
	    	
	    }
	    old_time = tempo;
	    }


	    @Override
	    public void onProviderDisabled(String provider)
	    {
	    gps_fix = false;
	    //img_gps.setImageResource(R.drawable.pallino_rosso);
	    Toast.makeText( getApplicationContext(),R.string.gps_disabled,Toast.LENGTH_SHORT ).show();
	    gpstxt.setTextColor(Color.RED	);
	    }


	    @Override
	    public void onProviderEnabled(String provider)
	    {
	    Toast.makeText( getApplicationContext(),R.string.gps_enabled,Toast.LENGTH_SHORT).show();
	    }
	    
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras)
	    {

	    }
	  }



	private Bitmap dritta;
	private Bitmap rovescia;
	private Bitmap orizzontale;
	private Bitmap verticale;
	private Matrix matrix;
	private ImageView imgview;
	private CheckBox chkbox;
	private Button btn_save;
	private Date dataora;
	private LocationManager mlocManager;
	private MyLocationListener mlocListener;
	private Spinner for_spinner;
	private ArrayAdapter<String> adapter;


	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        /* Use the LocationManager class to obtain GPS locations */
        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        // check if GPS is enabled, if false go to configuration menu on Android Settings
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        
        //turn screen always on without changing permission
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        txt_strike = (TextView)findViewById(R.id.txtstrike);
        txt_dip = (TextView)findViewById(R.id.txtdip);
        imgview = (ImageView)findViewById(R.id.imageView1);
        chkbox = (CheckBox)findViewById(R.id.chkrovescia);
        gpstxt = (TextView) findViewById(R.id.gps);
        dritta = BitmapFactory.decodeResource(getResources(), R.drawable.dritta);
        rovescia = BitmapFactory.decodeResource(getResources(), R.drawable.rovescia);
        orizzontale = BitmapFactory.decodeResource(getResources(), R.drawable.orizzontale);
        verticale = BitmapFactory.decodeResource(getResources(), R.drawable.verticale);
        btn_save = (Button)findViewById(R.id.btnsave);
        img_gps = (ImageView)findViewById(R.id.imggps);
        for_spinner = (Spinner) findViewById(R.id.spinner1);
        btn_save.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){ 
        							salva(); 
        							}
		});
        txt_strike.setText("0");
        txt_dip.setText("0");
        gpstxt.setTextColor(Color.RED);
        old_time = 0;
        
        StringBuilder stringa  = new StringBuilder();
		try {
			File sdcard= Environment.getExternalStorageDirectory();
			File formazioni = new File(sdcard,"formazioni.txt");
			BufferedReader br = new BufferedReader(new FileReader(formazioni));
			String line;
			while ((line=br.readLine()) != null)
			{
				stringa.append(line);
				//stringa.append('\n');
			}
			Log.d(TAG,stringa.toString());
		} catch (FileNotFoundException e) {
			stringa.append(R.string.litologie);
			Log.d(TAG,stringa.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] item = stringa.toString().split(";");
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for_spinner.setAdapter(adapter); 
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
				    
				    String formazione = (String) for_spinner.getItemAtPosition(for_spinner.getSelectedItemPosition());
					
				    if (gps_fix)
				    	out.write(stringa_tempo+";"+Double.toString(latitudine)+";"+Double.toString(longitudine)+";"+txt_strike.getText()+";"+txt_dip.getText()+";"+misura+";"+formazione+"\n");
				    else
				    	out.write(stringa_tempo+";0;0;"+txt_strike.getText()+";"+txt_dip.getText()+";"+misura+";"+formazione+"\n");
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
        mlocManager.removeUpdates(mlocListener);
        super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
        if (Config.LOGD) Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(mListener);
        mlocManager.removeUpdates(mlocListener);
        super.onStop();
    }
  
    public boolean onCreateOptionsMenu(Menu menu)
    {
		menu.add(1,1,0,R.string.cancella).setIcon(R.drawable.delete_item);
		menu.add(1,2,1,R.string.calibra).setIcon(R.drawable.tool);
		menu.add(1,3,2,R.string.help).setIcon(R.drawable.help);
		return true;
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    		{
    	case 1:
        	
        	AlertDialog.Builder conferma_canc = new AlertDialog.Builder(this);
        	conferma_canc.setTitle(R.string.conferma);
        	conferma_canc.setMessage(R.string.delete_data);
        	conferma_canc.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
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
        	    	   	
           	conferma_canc.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
    
   
    
    // fire this when GPS is found disabled on start
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.gps_off)
               .setCancelable(false)
               .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   }
               })
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
    


