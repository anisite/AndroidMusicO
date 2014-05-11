package com.infologique.o973;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
//import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infologique.o973.StreamPlay.LocalBinder;
import com.infologique.o973.StreamPlay.RequestTask;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

public class MainActivity extends Activity {

	public static MainActivity instance;
	
	/*Pub*/
	private static AdView adView;
	//private static final String MY_AD_UNIT_ID = "b39fc68b3b4847ae";
	//private static boolean isPlaying;
	private static ImageButton btnPlay;
	private String TAG = getClass().getSimpleName();
	//private static MediaPlayer mp = null;
	private static TextView songTitleLabel;
	private static TextView songArtistLabel;
	@SuppressWarnings("unused")
	private static ImageView image;

	@SuppressWarnings("unused")
	private static boolean inback = false;
	
	boolean isBinded = false;
	
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
    	if (SPservice!=null) outState.putBoolean("isPlaying", SPservice.isPlaying());
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;
		setContentView(R.layout.activity_main);
		SetActionBar();

		/*DECLARATION DE ÉLÉMENTS À UTILISER*/
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songArtistLabel = (TextView) findViewById(R.id.songArtist);
		image = (ImageView) findViewById(R.id.adele);

		
		/*REMISE DE L'ÉTAT DE LA FENETRE*/
		 if (savedInstanceState != null) {
			//isPlaying = 
		    if (savedInstanceState.getBoolean("isPlaying")) SetPlay(true);
			 
	    }
		 
		/*CAST ET DECLARATIONS DE LA FENETRE*/
        Intent intent = new Intent(this, StreamPlay.class);
        bindService(intent, SPserviceConnection, 0);
        startService(intent);
		
		//playbackServiceIntent = new Intent(this, StreamPlay.class);
		
		
        /*startService(playbackServiceIntent);
        bindService(playbackServiceIntent, SPserviceConnection, Context.BIND_AUTO_CREATE);*/
		
		//mp = new MediaPlayer();

		/*receive = new HeadsetConnectionReceiver();
		this.registerReceiver(receive, 
                new IntentFilter(Intent.ACTION_HEADSET_PLUG));*/
		
		/*lastSong = "empty";*/
		
		/*Pub start*/
	    // Create the adView
	    adView = (AdView)this.findViewById(R.id.pub);
	    //adView = new AdView(this);
	    //adView.setAdUnitId(MY_AD_UNIT_ID);
	    //adView.setAdSize(AdSize.BANNER);
	    // Lookup your LinearLayout assuming it's been given
	    // the attribute android:id="@+id/mainLayout"
	     

	    // Add the adView to it
	    //layout.addView(adView);

	    Bundle bundle = new Bundle();
	    bundle.putString("color_bg", "00AAAAFF");
	    bundle.putString("color_text", "FFFFFF");
	    
	    AdMobExtras extras = new AdMobExtras(bundle);
	    AdRequest request = new AdRequest.Builder()
	    .addNetworkExtras(extras)
	    .build();
	    
	    // Initiate a generic request to load it with an ad
	    adView.loadAd(request);
		/*Pub end*/
		
	    //RCStartRemote();
		
		// All player buttons



		//startPlaybackButton = (Button) this.findViewById(R.id.btnPlay);
	    /*stopPlaybackButton = (Button) this.findViewById(R.id.btn);*/
		
		btnPlay.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// check for already playing
					if (SPservice.isPlaying()){
						//SetPlay(false);
						//btnPlay.setImageResource(R.drawable.btn_play);
						SPservice.stop();
						//stopService(playbackServiceIntent);
					}else{
						//SetPlay(true);
						//btnPlay.setImageResource(R.drawable.btn_pause);
						SPservice.play();
						//SetInfos();
						/*if (SPservice!=null && SPservice.bitmapART!=null){
							SPservice.RCChangeCover(SPservice.bitmapART);
						}*/
						//GererBitmap(SPservice.bitmapART);
						//startService(playbackServiceIntent);
					}
					//startService(playbackServiceIntent);
					/*if(startService(playbackServiceIntent) != null) { 
					    Toast.makeText(getBaseContext(), "Service is already running", Toast.LENGTH_SHORT).show();
					    stopService(playbackServiceIntent);
					}else {
					    Toast.makeText(getBaseContext(), "There is no service running, starting service..", Toast.LENGTH_SHORT).show();
					}*/
					//startService(playbackServiceIntent);
				   /*if (v == startPlaybackButton) {
					      startService(playbackServiceIntent);
					      finish();
					    } else if (v == stopPlaybackButton) {
					      stopService(playbackServiceIntent);
					      finish();
					}*/
					/*finish();*/
					/*if(mp!=null){
						
						if(mp.isPlaying()){
								stop();
						}else{
								play();
						}
					}else{
						play();
					}*/
				}
			});
		

		  //new RequestTask().execute("http://www.o973.com/EnOnde.aspx", "DIRECT");
		  //new RequestTask().execute(API_URL, "API");
		  //new RequestTask().execute("http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=la%20lune%20pleure%20okoum%C3%A9","GSEARCH","");
		  //mHandler.postDelayed(mUpdateTimeTask, delayUpdate); //30000
		  
		  
		  /* NOTIFIER */
		  
		// Prepare intent which is triggered if the
		// notification is selected

		/*Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// Build notification
		// Actions are just fake
		Notification noti = new Notification.Builder(this)
		        .setContentTitle("New mail from " + "test@gmail.com")
		        .setContentText("Subject").setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(pIntent)
		        .addAction(R.drawable.btn_play, "Call", pIntent)
		        .addAction(R.drawable.btn_pause, "More", pIntent)
		        .addAction(R.drawable.btn_play, "And more", pIntent).build();
		    
		  
		NotificationManager notificationManager = 
		  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti); */
		  
		  
		   
		   //mp.start();
		  	
	}
	
	  @Override
	    protected void onStart() {
	        super.onStart();
	        // Bind to LocalService

	    }
	
	/*public boolean isPlaying(){
		return mp.isPlaying();
	}*/
	
	/*public class HeadsetConnectionReceiver extends BroadcastReceiver {

	    public void onReceive(Context context, Intent intent) {
	        Log.w(TAG, "ACTION_HEADSET_PLUG Intent received");
	        Boolean headsetConnected =true;
	        if (intent.hasExtra("state")){
	        	   if (headsetConnected && intent.getIntExtra("state", 0) == 0){
	        	    headsetConnected = false;
	        	    if (mp.isPlaying()){
	        	     //intent.removeExtra("state");
	        	     stop();
	        	    }
	        	   }
	        	   else if (!headsetConnected && intent.getIntExtra("state", 0) == 1){
	        	    headsetConnected = true;
	        	   }
	        }
	    }
	}*/
	


/*	public void play() {
		btnPlay.setImageResource(R.drawable.btn_pause);
		
		mp.reset();
		
		Uri myUri = Uri.parse("http://xlnetwork.info:5400/;"); //5400 // 8020
		  
		try {
			mp.setDataSource(this, myUri);
			RCChangeState(true);
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Go to Initialized state
		   mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		   mp.setOnPreparedListener(this);
		   mp.setOnBufferingUpdateListener(this);

		   mp.setOnErrorListener(this);
		   mp.prepareAsync();

	 }*/

	/*public void stop() {
		btnPlay.setImageResource(R.drawable.btn_play);
		mp.stop();
		RCChangeState(false);
	}*/
	
	
	/*@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		  Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
	}*/

	/*@Override
	 public boolean onError(MediaPlayer mp, int what, int extra) {
		btnPlay.setImageResource(R.drawable.btn_play);
		
		Dialog dialog = new Dialog(this);
		//TextView txt = (TextView)dialog.findViewById(R.id..textbox);

		//txt.setText(getString(R.string.message));
		dialog.setTitle("Serveur indisponible.");

		dialog.show();
		
		  StringBuilder sb = new StringBuilder();
		  sb.append("Media Player Error: ");
		  switch (what) {
		  case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
		   sb.append("Not Valid for Progressive Playback");
		   break;
		  case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
		   sb.append("Server Died");
		   break;
		  case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		   sb.append("Unknown");
		   break;
		  default:
		   sb.append(" Non standard (");
		   sb.append(what);
		   sb.append(")");
		  }
		  sb.append(" (" + what + ") ");
		  sb.append(extra);
		  Log.e(TAG, sb.toString());
		  return true;
	}*/

	/*@Override
	public void onPrepared(MediaPlayer mp) {
		  Log.d(TAG, "Stream is prepared");
		  btnPlay.setImageResource(R.drawable.btn_pause);
		  mp.start();
		 }*/
	
	@Override
	public void onResume() {
		//if (notificationManager != null){
		//	notificationManager.cancel(NotifyID);
		inback = false;
		//}
		if (SPservice!=null) SetPlay(SPservice.isPlaying());
		
		super.onResume();
		adView.resume();
	}
	
	private void SetPlay(boolean isPlaying)
	{
		if (isPlaying){
			btnPlay.setImageResource(R.drawable.btn_pause);
		}else{
			btnPlay.setImageResource(R.drawable.btn_play);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  
    {
         /*if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
         {
        	 //setNotify(true);
        	 inback = true;
            this.moveTaskToBack(true);
            return true;
         }*/
		
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
       	   //setNotify(true);
       	   inback = true;
           this.moveTaskToBack(true);
           return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	
	/*@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setTitle("Really Exit?")
	        .setMessage("Are you sure you want to exit?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {

	        	@Override
	            public void onClick(DialogInterface arg0, int arg1) {
	                MainActivity.super.onBackPressed();
	            }

	        }).create().show();
	}*/
	/*@Override
	public void onBackPressed() {
	   Log.d("CDA", "onBackPressed Called");
	   Intent setIntent = new Intent(Intent.ACTION_MAIN);
	   setIntent.addCategory(Intent.CATEGORY_HOME);
	   setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   startActivity(setIntent);
	}*/
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
	        Log.d("CDA", "onKeyDown Called");

	        return true;
	        //onBackPressed();
	    }
	    return super.onKeyDown(keyCode, event);
	}*/

	/*@Override
	public void onCompletion(MediaPlayer mp) {
		  stop();
		 }*/
	
	@Override
	 public void onDestroy() {

		adView.destroy();
		
		/*if (mp != null ){
			mp.stop();
			mp.release();
		}*/
		
		if (adView != null) {
		    adView.destroy();
		}
		
		 //mp =null;

		 
		 
		 //unregisterReceiver(receive);
		 

		 

		unbindService(SPserviceConnection);
		SPserviceConnection=null;
		
		if (OnClosing){
			
	    	//Intent intent = new Intent(this, StreamPlay.class);
	    	/*unbindService(SPserviceConnection);
	    	SPserviceConnection=null;*/
	    	SPservice.stopSelf();
	    	//stopService(intent);
	    	//intent=null;
	    	
			 /*System.runFinalization();
			 setResult(0);
			 finish();*/
			//finish();
	    	//System.exit(0);
		}

		 
		 super.onDestroy();
	 }
	
	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_quitter:
	        	//mp.release();
	        	OnClosing = true;
	        	finish();
	            return true;
	     }
		return false;
	}
	
boolean OnClosing = false;
	

public void SetInfos() {
	Log.d(TAG, "SetInfos - DEBUT");
	if (SPservice!=null && SPservice.apiSayIsPlaying){
	    songTitleLabel.setText(SPservice.Titre);
	    songArtistLabel.setText(SPservice.Artiste);
	}else{
		SetDefault();
	}
	Log.d(TAG, "SetInfos - FIN");
}

public void SetDefault()
{
    songTitleLabel.setText(R.string.loading);
    songArtistLabel.setText(R.string.please);
}

public void SetNoArt()
{
	Log.d(TAG, "SetNoArt - DEBUT");
	if (!isBinded) {
		Log.d(TAG, "SetNoArt - FIN - not binded");
		return;
	}
	((ImageView) findViewById(R.id.adele)).setImageResource(R.drawable.adeleo);
   	
    //TODO:Changer le call
	//SPservice.RCChangeCover(BitmapFactory.decodeResource(MainActivity.this.getApplicationContext().getResources(),R.drawable.adeleo));
	SetBlurBackgroundKitkat(null);
	Log.d(TAG, "SetNoArt - FIN");
}

//show The Image


private void GererBitmap()
{
	if (SPservice==null || SPservice.bitmapART == null || !SPservice.apiSayIsPlaying){
		SetNoArt();
	}else if (!SPservice.bitmapART.isRecycled()){
		//SPservice.RCChangeCover(tempBit);
		((ImageView) findViewById(R.id.adele)).setImageBitmap(SPservice.bitmapART);
		SetBlurBackgroundKitkat(SPservice.bitmapART);
	}
	//imageBitmap = tempBit;
}


@SuppressLint("NewApi")
public void SetBlurBackgroundKitkat(Bitmap IMG)
{
	  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		 RelativeLayout Rl = (RelativeLayout) findViewById(R.id.appLayout);
		 if (IMG != null){
			 Rl.setBackground( new BackgroundBitmapDrawable(getResources(), Blur.fastblur(MainActivity.this, IMG, 25)));
		 }else{
			 Rl.setBackground(null);
		 }
	  }
}


//@SuppressLint("InlinedApi")
//RemoteControlClient mRemoteControlClient;

/*@SuppressLint("InlinedApi")
public void RCChangeState(boolean playing){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		if (playing){
			//TODO:Changer le call
			SPservice.RC.updateState(true);
			//mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}else{
			//TODO:Changer le call
			SPservice.RC.updateState(false);
			//mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		}
    }
}*/

//@SuppressLint("InlinedApi")
//RemoteController RC;
/*@SuppressLint("InlinedApi")
AudioManager audioManager;*/
//private final Context context;




public static StreamPlay SPservice;



private ServiceConnection SPserviceConnection = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder service) {
		//SPservice = ((LocalBinder) binder).getService();
		// We've bound to LocalService, cast the IBinder and get LocalService instance
        LocalBinder binder = (LocalBinder) service;
        SPservice = binder.getService();
       
        isBinded = true;
        
      
		SPservice.registerCallback(myCallback);
        //SPservice.reloadSettings();
        
        //if (isPlaying){
        	GererBitmap();
        	SetInfos();
        	//GererBitmap(SPservice.bitmapART);
	    	//SetInfos();
        //}
		
	}
	
	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		isBinded = false;
	}
};





/*private boolean isServiceRunning() {
	  ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
	  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    if ("StreamPlay".equals(service.service.getClassName())) {
	      return true;
	    }
	  }
	  return false;
	}*/

/*@SuppressLint("InlinedApi")
public void RCStartRemote()
{
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	

        
    	ComponentName myEventReceiver = new ComponentName(this, MediaButtonReceiver.class);
    	SPservice.audioManager.registerMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
    	Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
	    mediaButtonIntent.setComponent(myEventReceiver);
    	
	  //TODO:Changer les  2 callss
    	//RC = new RemoteController();
		//RC.register(MainActivity.this.getApplicationContext(), mediaButtonIntent, SPservice.audioManager);
    	
	   /* AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,  AudioManager.AUDIOFOCUS_GAIN);
        //Bitmap AlbumArt=BitmapFactory.decodeResource(getResources(), image);
        ComponentName mIslahReceiverComponent = new ComponentName(this,MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(mIslahReceiverComponent);
        Intent mediaButtonIntent=new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mIslahReceiverComponent);
        PendingIntent mediaPendingIntent=PendingIntent.getBroadcast(getApplicationContext(), 0,mediaButtonIntent,0);
        mRemoteControlClient=new RemoteControlClient(mediaPendingIntent);
        mRemoteControlClient.editMetadata(true)
        .putString(MediaMetadataRetriever.METADATA_KEY_TITLE,"test")
        .putBitmap(100,BitmapFactory.decodeResource(MainActivity.this.getApplicationContext().getResources(),R.drawable.adele))
        .apply();
        mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        mRemoteControlClient.setTransportControlFlags(
        		RemoteControlClient.FLAG_KEY_MEDIA_STOP|
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY);
        audioManager.registerRemoteControlClient(mRemoteControlClient);
    }
}*/

/*@SuppressLint("InlinedApi")
public void RCChangeCover(Bitmap cover)
{
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	RC.updateCover(cover);
    }
}*/

/*@SuppressLint("InlinedApi")
public void RCChangeInfos(String Artiste, String Album, String Show)
{
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	RC.updateMetadata(Artiste,Album, Show);
    }
}*/

	@SuppressLint("NewApi")
	private void SetActionBar()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
			TextView abTitle = (TextView) findViewById(titleId);
			int color = 0xFF000000;
			abTitle.setShadowLayer(3, 0, 0, color );
		}
	
	}
	
	private StreamPlay.ICallback myCallback = new StreamPlay.ICallback() {
	    public void sendValue(StreamPlay.SEND value) {
	    	//if (){
	        switch (value) {
	            case TEXTDATA:
					SetInfos();
					break;
	            case ALBUMART:
	            	GererBitmap();
	            	break;
	            case NOALBUMART:
	            	SetNoArt();
	            	break;
	            case StartPlaying:
	            	SetPlay(true);
	            	break;
	            case StopPlaying:
	            	SetPlay(false);
	            	break;
			default:
				break;
	        }
	    }
	    
		public void errorReport(String Text) {
		    	//if (){
			pendingErrorText = Text;
			mHandler.postDelayed(mShowError, 1); //30000
			
	    	//}
	       // myHandler.sendMessage(myHandler.obtainMessage(1, value, 0));
	    }

	    /*@SuppressLint("HandlerLeak")
		private Handler myHandler = new Handler() {
		    @Override 
		    public void handleMessage(Message msg) {
		        switch (msg.what) {
		                 case 1:
		                	 int receivedValue = (int)msg.arg1;
		                	 break;
	            }
	          }
	    };*/
	};
	
	
	private Runnable mShowError = new Runnable() {
		   public void run() {
			   Toast to = Toast.makeText(getBaseContext(), "   " + pendingErrorText + "   ", Toast.LENGTH_SHORT);
			   //to.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM, 10, 20);
			   to.show();
		   }
		};
	
	private String pendingErrorText;
	private Handler mHandler = new Handler();
}
	




