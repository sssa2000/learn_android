package com.fancytech.testusbhost;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {

	private Handler mhandler;
	final int MSG_ID=0x13579;
	final int MSG_UPDATE_TIMERUI=0x135790;
	final String Tag = "TestUsbHost";
	
	private static final String ACTION_USB_PERMISSION ="com.fancytech.USB_PERMISSION";

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		   public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (ACTION_USB_PERMISSION.equals(action)) {
		            synchronized (this) {
		                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		                    if(device != null){
		                        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);		                       
		                        UsbDeviceConnection connection = mUsbManager.openDevice(device); 
		            			connection.claimInterface(device.getInterface(0), true);		            			
		            			int fd = connection.getFileDescriptor();
		            			Log.d(Tag, "UsbDevice fd = " + fd);
		            			Toast.makeText(getApplicationContext(), "UsbDevice fd = " + fd, 0).show(); 
		                        }
		                } 
		                else {
		                    Log.d(Tag, "permission denied for device " + device);
		                }
		            }
		        }
		        else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
			           UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			           String deviceName = device.getDeviceName();
			           Toast.makeText(getApplicationContext(), "UsbDevice attached ="+deviceName, 0).show();  
			           
                       PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                       UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                       mUsbManager.requestPermission(device, mPermissionIntent);
                       Log.d(Tag, "after requestPermission");
                       
//                       
//                    UsbDeviceConnection connection = mUsbManager.openDevice(device); 
//           			connection.claimInterface(device.getInterface(0), true);		            			
//           			int fd = connection.getFileDescriptor();
//           			Log.d(Tag, "UsbDevice fd = " + fd);
//           			Toast.makeText(getApplicationContext(), "UsbDevice fd = " + fd, 0).show(); 
           			
           			

			    } 
			    else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
			           UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			           String deviceName = device.getDeviceName();
			           Toast.makeText(getApplicationContext(), "UsbDevice de attached ="+deviceName, 0).show();  
			    }		        
		    }
		};

		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		final TextView timer_tv=(TextView)findViewById(R.id.textView3);
        final TextView usb_tv=(TextView)findViewById(R.id.textView2);
        
        mhandler=new Handler(){
        	@Override
        	public void handleMessage(Message msg)
        	{
        		if(msg.what==MSG_ID)
        		{

        			//Log.d(Tag,"handleMessage,msg=MSG_ID,arg1="+msg.arg1);
        			timer_tv.setText("计时器执行次数="+msg.arg1);
        			usb_tv.setText(msg.obj.toString());
        		}
        		else if(msg.what==MSG_UPDATE_TIMERUI)
        		{
        			//Log.d(Tag,"handleMessage,msg=MSG_UPDATE_TIMERUI,arg1="+msg.arg1);
        			timer_tv.setText("计时器执行次数="+msg.arg1);
        		}
        		
        		
        	}
        };
        
        //500ms 扫描一次
        new Timer().schedule(new TimerTask(){
        	@Override
        	public void run(){        		
        		scanUsbDevice();
        	}},0,500);
        

        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause()
    {
    	Log.d(Tag,"On Pause");
    	super.onPause();
    	
    }
    @Override
    protected void onRestart()
    {
    	Log.d(Tag,"onRestart");
    	super.onPause();
    	
    }
    @Override
    protected void onResume()
    {
    	Log.d(Tag,"onResume");
    	super.onPause();
    	
    }
    @Override
    protected void onStart()
    {
    	Log.d(Tag,"onStart");
    	super.onPause();
    	
    }
    @Override
    protected void onDestroy()
    {
    	Log.d(Tag,"onDestroy");
    	super.onDestroy();
    }
    @Override
    protected void onStop()
    {
    	Log.d(Tag,"onStop");
    	super.onStop();
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public static int scanCount=0;
    public void scanUsbDevice()
    {
    	scanCount++;
    	UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if(deviceList.isEmpty())
        {
        	Message msg=new Message();        	
        	msg.what=MSG_UPDATE_TIMERUI;
        	msg.arg1=scanCount;
        	mhandler.sendMessage(msg);
        }
        else
        {
	        Iterator<UsbDevice> deviceIterator=deviceList.values().iterator();
	        while(deviceIterator.hasNext()){
	        	UsbDevice device=deviceIterator.next();
	        	int usbVid=device.getVendorId();
	        	int usbPid=device.getProductId();
	        	String usbName=device.getDeviceName();
	        	String deviceString=usbName+" VendorId="+usbVid+" ProductId="+usbPid;
	        	Message msg=new Message();        	
	        	msg.what=MSG_ID;
	        	msg.arg1=scanCount;
	        	msg.obj=deviceString;
	        	mhandler.sendMessage(msg);
	            
	           
	        }
        }
        
    }
}
