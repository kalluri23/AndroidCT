package com.verizon.contenttransfer.p2p.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.utils.LogUtil;

//import android.util.Log;

public class SensorService extends Service implements SensorEventListener {

	private SensorManager sensorManager;
	private long lastUpdate = System.currentTimeMillis();
	private Handler handler = new Handler();
	boolean isFall, isCall, isRecording, isBattery;
	boolean flatSensor = false;
	public static final String TAG = "SensorService";
	
	public static boolean bumpdetected = false;
	
	public static final String STOP_SENSOR_SERVICE="stop_sensor";
	public static final String START_SENSOR_SERVICE="start_sensor";
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		LogUtil.d("market", "stop the service");

		this.unregisterReceiver(mReceiver);
		if (isFall)
			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

		super.onDestroy();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			getAccelerometer(event);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {

		handleOnStart();
	}

	@Override
	public void onCreate() {
		LogUtil.d(TAG, "create Sensor Service");
		isFall = isCall = isRecording = isBattery = false;
		super.onCreate();
	}

	private void handleOnStart() {
		LogUtil.d(TAG, "start Sensor Service");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		IntentFilter filter = new IntentFilter(STOP_SENSOR_SERVICE);
		
		filter.addAction(START_SENSOR_SERVICE);
		
		
		
		registerReceiver(mReceiver, filter);

		isFall = true;

	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {

		handleOnStart();

		return START_STICKY;

	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			stopSensor(action);

		}
	};

	public void stopSensor(String action) {

		
		
		if(action.equals(STOP_SENSOR_SERVICE))
		{

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			LogUtil.d(TAG, "Stop sensor service");
		}
		else
		if(action.equals(START_SENSOR_SERVICE))
		{
			bumpdetected = false;
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			LogUtil.d(TAG, "Start sensor service");
		}
		
		
	

	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;

		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		//float accelaration = Math.abs(x * x) + Math.abs(y * y)
				//+ Math.abs(z * z);
		
		//float accelaration =  Math.abs(x * x) + Math.abs(y * y) + Math.abs(z * z);
		
		//+ Math.abs(z * z);

		// if(accelaration < 10.00)
		// {

		// LogUtil.d("Sensor accelaration ",
		// Float.toString(Math.abs(accelaration)));
		
		float accelaration = Math.abs(x);

		if (accelaration > 15.0) {
			
			//LogUtil.d(TAG,"bump is  detected");
			
			//if(lastUpdate==0)
			lastUpdate = System.currentTimeMillis();
			
			
			
			//if(!bumpdetected)
				
			if(!bumpdetected)
			{
				Intent intent = new Intent("bumpdetected");
				
				intent.putExtra("audioevent","detectedbump");
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				
				bumpdetected = true;
			}
			
			

		} else {
			
			//LogUtil.d(TAG,"bump is  not detected");
			lastUpdate = 0;
			flatSensor = true;
		}

	}

}
