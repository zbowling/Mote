/*
 * WARNING. Thar be copy pasta coding below. This is hackathon quality crap. 
 * 
 */

package com.apportable.demo.mote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.neurosky.thinkgear.TGDevice;
import com.zeemote.zc.Controller;
import com.zeemote.zc.event.BatteryEvent;
import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.ControllerEvent;
import com.zeemote.zc.event.DisconnectEvent;
import com.zeemote.zc.event.IButtonListener;
import com.zeemote.zc.event.IJoystickListener;
import com.zeemote.zc.event.IStatusListener;
import com.zeemote.zc.event.JoystickEvent;
import com.zeemote.zc.ui.android.ControllerAndroidUi;

public class Remote extends Activity implements IStatusListener,
IJoystickListener, IButtonListener {
	private static final int MENU_CONTROLLER = 1;
	private static final int MENU_HEADSET = 2;
	private Socket socket = null;
	private Controller controller;
	private ControllerAndroidUi controllerUi;
	private boolean keepConnection;
	private boolean isMovingJoystick = false;
	BluetoothAdapter bluetoothAdapter;
	private TGDevice tgDevice;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        
		controller = new Controller(1, Controller.TYPE_GP1);
		controller.addStatusListener(this);
		controller.addButtonListener(this);
		controller.addJoystickListener(this);
		
		controllerUi = new ControllerAndroidUi(this, controller);
		keepConnection = false;
    }
    
	@Override
	public void onResume() {
		super.onResume();
		if (!keepConnection) {
			// Start the establishing a connection.
			// If the auto connect is ON, this shows the ControllerAndroidUi activity.
			// otherwise, this is no effect.
			controllerUi.startConnectionProcess();
			keepConnection = true;
		} else {
			// The ControllerAndroidUi activity was hidden.
			keepConnection = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (!keepConnection) {
			// If the ControllerAndroidUi activity is not shown,
			// disconnect the Zeemote controller.
			try {
				controller.disconnect();
			} catch (Exception e) {
			}
		}
	}
    
    public void connect() {
    	new Thread(new Runnable() {
		    public void run() {
		    	try {
		        	if (socket != null) {
		        		if (socket.isConnected())
		        			return;
		        		socket.close();
		        	}
					socket = new Socket("192.168.15.216",51627);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		  }).start();
    }
    
    public void disconnect() {
    	  new Thread(new Runnable() {
    		    public void run() {
    		    	if (socket != null) {
    		    		try {
    						socket.close();
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    		    	}
    		    	socket = null;
    		    }
		  }).start();
    }
    
    public void connectionHeadset() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
        	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	return;
        }else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);
        }  
    }
    
    
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                case TGDevice.STATE_IDLE:
	                    break;
	                case TGDevice.STATE_CONNECTING:		                	
	                	Log.d("MSG_STATE_CHANGE","STATE_CONNECTING");
	                	break;		                    
	                case TGDevice.STATE_CONNECTED:
	                	Log.d("MSG_STATE_CHANGE","STATE_CONNECTED");
	                	tgDevice.start();
	                    break;
	                case TGDevice.STATE_NOT_FOUND:
	                	Log.d("MSG_STATE_CHANGE","STATE_NOT_FOUND");
	                	break;
	                case TGDevice.STATE_NOT_PAIRED:
	                	Log.d("MSG_STATE_CHANGE","STATE_NOT_PAIRED");
	                	break;
	                case TGDevice.STATE_DISCONNECTED:
	                	Log.d("MSG_STATE_CHANGE","STATE_DISCONNECTED");
                }

                break;
            case TGDevice.MSG_POOR_SIGNAL:
            		Log.d("MSG_POOR_SIGNAL",msg.arg1 + "\n");
                break;
            case TGDevice.MSG_RAW_DATA:	  
            		//raw1 = msg.arg1;
            		//tv.append("Got raw: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_HEART_RATE:
            		Log.d("MSG_HEART_RATE",msg.arg1 + "\n");
            		break;
            case TGDevice.MSG_ATTENTION:
            	try {
            		int att = msg.arg1;
            		if (att > 60) {
            			if (socket != null && socket.isConnected()) {
	            			int xrand = 0 + (int)(Math.random() * ((200 - 0) + 1));
							int yrand = 0 + (int)(Math.random() * ((200 - 0) + 1));
							
							String output = "";
							final JSONObject jsonobj1 = new JSONObject();
							JSONArray touches1 = new JSONArray();
							jsonobj1.put("message","began");
					        JSONObject touch1 = new JSONObject();
					        touch1.put("x", xrand);
					        touch1.put("y", yrand);
					        touch1.put("index", 0);
					        touches1.put(touch1);
							jsonobj1.put("touches",touches1);
							jsonobj1.put("width",200);
							jsonobj1.put("height",200);
							output = jsonobj1.toString() + "\n";
							
							final JSONObject jsonobj2 = new JSONObject();
							JSONArray touches2 = new JSONArray();
							jsonobj2.put("message","moved");
					        JSONObject touch2 = new JSONObject();
					        touch2.put("x", xrand);
					        touch2.put("y", yrand);
					        touch2.put("index", 0);
					        touches2.put(touch2);
							jsonobj2.put("touches",touches2);
							jsonobj2.put("width",200);
							jsonobj2.put("height",200);
							output = output + jsonobj2.toString() + "\n";
							
							jsonobj2.put("message","ended");
							output = output + jsonobj2.toString() + "\n";
							final String finalOutput = output;
							new Thread(new Runnable() {
							    public void run() {
									try {
										socket.getOutputStream().write(
												finalOutput.getBytes("UTF-8"));
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							    }
					    	}).start();
            			}
            		}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            		//att = msg.arg1;
            		Log.d("MSG_ATTENTION",msg.arg1 + "\n");
            		//Log.v("HelloA", "Attention: " + att + "\n");
            	break;
            case TGDevice.MSG_MEDITATION:
            		Log.d("MSG_BLINK",msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_BLINK:
            		Log.d("MSG_BLINK",msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_RAW_COUNT:
            		//tv.append("Raw Count: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_RAW_MULTI:
            		Log.d("MSG_RAW_MULTI",msg.arg1 + "\n");
            		break;
            	//tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
            default:
            	break;
        }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_remote, menu);
        menu.add(0, MENU_CONTROLLER, 0, "Controller");
        menu.add(0, MENU_HEADSET , 1, "NeuroSky");
        return true;
    }
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        	case R.id.menu_connect:
        		this.connect();
        		break;
        	case MENU_CONTROLLER:
    			// Show the controller menu.
    			// It shows the ControllerAndroidUi activity.
    			controllerUi.showControllerMenu();
    			keepConnection = true;
    			return true;
        	case MENU_HEADSET:
        		connectionHeadset();
        		if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
        			tgDevice.connect(false);
        		break;
        }
        return true;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (socket == null || !socket.isConnected())
			return true;
		
        try 
        {
        	View rootView = findViewById(android.R.id.content).getRootView();
			final JSONObject jsonobj = new JSONObject();
			JSONArray touches = new JSONArray();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
		        case MotionEvent.ACTION_POINTER_DOWN: {
		            final int indexPointer = event.getActionIndex();
		            final int idPointer = event.getPointerId(indexPointer);
		            final float xPointer = event.getX(indexPointer);
		            final float yPointer = event.getY(indexPointer);
		            jsonobj.put("message","began");
		            JSONObject touch = new JSONObject();
		            touch.put("x", xPointer);
		            touch.put("y", yPointer);
		            touch.put("index", idPointer);
		            touches.put(touch);
		            break;
		        }
		        case MotionEvent.ACTION_DOWN: {
		            // there are only one finger on the screen
		            final int idDown = event.getPointerId(0);
		            final float xDown = event.getX();
		            final float yDown = event.getY();
		            jsonobj.put("message","began");
		            JSONObject touch = new JSONObject();
		            touch.put("x", xDown);
		            touch.put("y", yDown);
		            touch.put("index", idDown);
		            touches.put(touch);
		            break;
		        }
		        case MotionEvent.ACTION_MOVE: {
		            final int pointerNumber = event.getPointerCount();
		            final int[] ids = new int[pointerNumber];
		            final float[] xs = new float[pointerNumber];
		            final float[] ys = new float[pointerNumber];
		
		            jsonobj.put("message","moved");
		            for (int i = 0; i < pointerNumber; i++) {
		                ids[i] = event.getPointerId(i);
		                xs[i] = event.getX(i);
		                ys[i] = event.getY(i);
		                
		                JSONObject touch = new JSONObject();
		                touch.put("x", xs[i]);
		                touch.put("y", ys[i]);
		                touch.put("index", ids[i]);
		                touches.put(touch);
		            }
		         
		            break;
		        }
		        case MotionEvent.ACTION_POINTER_UP: {
		            final int indexPointer = event.getActionIndex();
		            final int idPointer = event.getPointerId(indexPointer);
		            final float xPointer = event.getX(indexPointer);
		            final float yPointer = event.getY(indexPointer);
		            
		            jsonobj.put("message","ended");
		            JSONObject touch = new JSONObject();
		            touch.put("x", xPointer);
		            touch.put("y", yPointer);
		            touch.put("index",idPointer);
		            touches.put(touch);
		            break;
		        }
		        case MotionEvent.ACTION_UP: {
		            // there are only one finger on the screen
		            final int idUp = event.getPointerId(0);
		            final float xUp = event.getX();
		            final float yUp = event.getY();
		            jsonobj.put("message","ended");
		            JSONObject touch = new JSONObject();
		            touch.put("x", xUp);
		            touch.put("y", yUp);
		            touch.put("index",idUp);
		            touches.put(touch);
		            break;
		        }
		        case MotionEvent.ACTION_CANCEL: {
		            final int idCancel = event.getPointerId(0);
		            final float xCancel = event.getX();
		            final float yCancel = event.getY();
		            jsonobj.put("message","cancelled");
		            JSONObject touch = new JSONObject();
		            touch.put("x", xCancel);
		            touch.put("y", yCancel);
		            touch.put("index",idCancel);
		            touches.put(touch);
		            break;
		        }
		        default: {
		            Log.d("MotionEvent", "Other:"+event.getAction());
		        }
			}
			jsonobj.put("touches",touches);
			jsonobj.put("width",rootView.getWidth());
			jsonobj.put("height",rootView.getHeight());
			Log.d("MotionEvent", "JSON:"+jsonobj.toString());
	    	new Thread(new Runnable() {
			    public void run() {
					try {
						socket.getOutputStream().write((jsonobj.toString() + "\n").getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
	    	}).start();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return true;
	}

	@Override
	public void batteryUpdate(BatteryEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connected(ControllerEvent arg0) {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnected(DisconnectEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buttonPressed(ButtonEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buttonReleased(ButtonEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void joystickMoved(JoystickEvent e) {
		if (socket == null || !socket.isConnected())
			return;
		try {
			String output = "";
			int x = e.getScaledX(-100, 100);
			int y = e.getScaledY(-100, 100);
			 
			int clampedX = 100;
			if (x > 20 || x < -20)
				clampedX = x < 0?0:200;
			
			int clampedY = 100; 
			if (y > 20 || y < -20)
				clampedY = y < 0?0:200;
			
			if (!isMovingJoystick) {
				if (x > 20 || x < -20 || y > 20 || y < -20) {
					final JSONObject jsonobj1 = new JSONObject();
					JSONArray touches1 = new JSONArray();
					jsonobj1.put("message","began");
			        JSONObject touch1 = new JSONObject();
			        touch1.put("x", (float)clampedX);
			        touch1.put("y", (float)clampedY);
			        touch1.put("index", 0);
			        touches1.put(touch1);
					jsonobj1.put("touches",touches1);
					jsonobj1.put("width",200);
					jsonobj1.put("height",200);
					output = output + jsonobj1.toString() + "\n";
					isMovingJoystick = true;
				}
			}
			else {
				if (x == 0 && y == 0) {
					final JSONObject jsonobj2 = new JSONObject();
					JSONArray touches2 = new JSONArray();
					jsonobj2.put("message","ended");
			        JSONObject touch2 = new JSONObject();
			        touch2.put("x", (float)clampedX);
			        touch2.put("y", (float)clampedY);
			        touch2.put("index", 0);
			        touches2.put(touch2);
					jsonobj2.put("touches",touches2);
					jsonobj2.put("width",200);
					jsonobj2.put("height",200);
					output = output + jsonobj2.toString() + "\n";
					isMovingJoystick = false;
				}
				else {
					final JSONObject jsonobj2 = new JSONObject();
					JSONArray touches2 = new JSONArray();
					jsonobj2.put("message","moved");
			        JSONObject touch2 = new JSONObject();
			        touch2.put("x", (float)clampedX);
			        touch2.put("y", (float)clampedY);
			        touch2.put("index", 0);
			        touches2.put(touch2);
					jsonobj2.put("touches",touches2);
					jsonobj2.put("width",200);
					jsonobj2.put("height",200);
					output = output + jsonobj2.toString() + "\n";
				}
			}
			
			if (output.isEmpty()) 
				return;
			final String finalOutput = output;
			Log.d("MotionEvent", "JSON:" + output);
	    	new Thread(new Runnable() {
			    public void run() {
					try {
						socket.getOutputStream().write(
								finalOutput.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
	    	}).start();
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		
	}       
}
   
