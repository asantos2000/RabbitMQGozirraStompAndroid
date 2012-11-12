package com.origoconsult.rabbitmq_on_android;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.login.LoginException;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatAppActivity extends Activity {
	private static final String TAG = ChatAppActivity.class.toString();	
	private TextView mOutput;
	private String QUEUE_NAME = "/topic/test";	
	private String message = "";
	private String name = "";
	private Client con;
	final Handler myHandler = new Handler();
	private int counterSend = 0;
	private int counterReceive = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate()");
		setContentView(R.layout.activity_chat_app);
		Toast.makeText(ChatAppActivity.this, "RabbitMQ Stomp Chat Service!",
				Toast.LENGTH_LONG).show();

		final EditText etv1 = (EditText) findViewById(R.id.out3);
		etv1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					name = etv1.getText().toString();
					etv1.setText("");
					etv1.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});

		final EditText etv = (EditText) findViewById(R.id.out2);
		etv.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					Log.d(TAG,"onKey() - Enter detected");
					// Perform action on key press
					message = name + ": " + etv.getText().toString();
					con.send(QUEUE_NAME, message);
					Log.d(TAG,"message to send is " + message);
					mOutput.append("\n("+ counterSend +")--> " + message);
					etv.setText("");
					counterSend++;
					return true;
				}
				return false;
			}
		});

		// The output TextView we'll use to display messages
		mOutput = (TextView) findViewById(R.id.output);
 
		new ConnectQueueServer().execute();
	}
	
	private class ConnectQueueServer extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... Text) {
			// Create the consumer
			try {
				con = new Client("10.0.1.6", 61613, "guest", "guest");

				// register for messages
				con.subscribe(QUEUE_NAME, new Listener() {
					@Override 
					public void message(Map header, String body ) {
						Log.d(TAG,"onMessage()");
					    Log.d(TAG,"message is " + body);
					    message = "\n("+ counterReceive +")<-- " + body;
					    myHandler.post(myRunnable);
					    counterReceive++;
					}
				});
				
				con.addErrorListener(new Listener() {
					@Override
					public void message(Map header, String message) {
						// TODO Auto-generated method stub
				    	Log.d(TAG,"onError()");
				    	Log.d(TAG,"onError() - Error message is " + message);
				    	//Log.d(TAG,"onError() - Error content is " + errorMsg.getContentAsString());					
					}
				});
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Auto-generated method stub			
			return null;
		}
	}
	
	final Runnable myRunnable = new Runnable() {
		public void run() {
			Log.d(TAG,"run() - message is " + message);
			if (message != null) {
				mOutput.append(message);
			} else {
				Log.d(TAG,"message is null");
			}
		}
	};	

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume()");
		reconnectQueueServer();
	}
	
	private void reconnectQueueServer() {
		Log.d(TAG,"reconnectQueueServer()");		
		new ConnectQueueServer().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG,"onPause()");

		if (con != null) {
			if (con.isConnected()) {
				Log.d(TAG,"disconnecting...");
				con.disconnect();
			} else {
				Log.d(TAG,"Already diconnected!!");
			}
		} else {
			Log.d(TAG,"need to connectQueueServer() first.");
		}

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }	
}
