package com.infologique.o973;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
//import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
//import android.content.Intent;
import android.util.Log;
//import android.view.*;

import com.infologique.o973.RemoteController;




public class StreamPlay extends Service implements
										MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
										MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, OnAudioFocusChangeListener  
{

	/*PUBLIC*/
	public Bitmap bitmapART;
	public String Artiste;
	public String Titre;
	public String Album;
	public String Emission;
	public String Pochette;
	public boolean apiSayIsPlaying;
	/*PUBLIC*/
	
	private static  NotificationManager notificationManager;
	private static int NotifyID;
	
	private static String  API_URL = "http://infologique.net/api/album/getO973.php";
	private static int delayUpdate = 10000;
	private String lastSong;
	
	private Handler mHandler = new Handler();
	static final String ACTION_PLAY = "com.infologique.o973.action.PLAY";
	
	@SuppressLint("InlinedApi")
	RemoteController RC;
	@SuppressLint("InlinedApi")
	AudioManager audioManager;
	
	public final IBinder localBinder = new LocalBinder();
	
	//public static StreamPlay instance;
	
	private String TAG = getClass().getSimpleName();
	private static MediaPlayer mp;
	private BroadcastReceiver receive;

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receive);
		if (mp!=null){
			mp.stop();
			mp.reset();
			mp=null;
		}
		
		if (RC !=null ){
			RC.stop();
			RC.release();
		}

		 mHandler.removeCallbacks(mUpdateTimeTask);
		 mHandler = null;
		 
		if (notificationManager != null){
			notificationManager.cancel(NotifyID);
		}
		
		super.onDestroy();
	}
	
	  @Override
	  public void onCreate() {
		  
		receive = new HeadsetConnectionReceiver();
		this.registerReceiver(receive, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

		//instance = this;
		//audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//AudioManager audioManager = (AudioManager) getSystemService("StreamPlay");
		//audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,  AudioManager.AUDIOFOCUS_GAIN);
		    
		Uri myUri = Uri.parse("http://xlnetwork.info:5400/;"); //5400 // 8020
	    mp = MediaPlayer.create(this, myUri);// raw/s.mp3
	    //mp.setOnCompletionListener(this);
	  }
	
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    /*if (!isPlaying()) {
	      //play();
	    	setValues(SEND.RESET);
	    }*/
	    return START_STICKY;
	  }
	
	public boolean isPlaying(){
		if (mp!=null) return mp.isPlaying();
		return false;
	}
	
	public class HeadsetConnectionReceiver extends BroadcastReceiver {

	    public void onReceive(Context context, Intent intent) {
	        Log.w(TAG, "ACTION_HEADSET_PLUG Intent received");
	        Boolean headsetConnected =true;
	        if (intent.hasExtra("state")){
	        	   if (headsetConnected && intent.getIntExtra("state", 0) == 0){
	        	    headsetConnected = false;
	        	    if (mp!=null && mp.isPlaying()){
	        	     //intent.removeExtra("state");
	        	     stop();
	        	    }
	        	   }
	        	   else if (!headsetConnected && intent.getIntExtra("state", 0) == 1){
	        	    headsetConnected = true;
	        	   }
	        }
	    }

	}
	
	public void play() {
		Log.d("StreamPlay","DEBUT play");
		
		
		/*setValues(SEND.TEXTDATA);
		setValues(SEND.ALBUMART);*/
		lastSong ="";
		
		new RequestTask().execute(API_URL, "API");
		
		if (mp!=null){
			mHandler.postDelayed(mUpdateTimeTask, delayUpdate); //30000

		
			RCStartRemote();
			/*btnPlay.setImageResource(R.drawable.btn_pause);*/
	
			Uri myUri = Uri.parse("http://xlnetwork.info:5400/;"); //5400 // 8020
	
		
			try {
				mp.reset();
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			   mp.setOnPreparedListener(this);
			   mp.setOnBufferingUpdateListener(this);
	
			   mp.setOnErrorListener(this);
			   mp.prepareAsync();
			   
			   myCallback.sendValue(SEND.StartPlaying);
		}else{
			stopSelf();
		}
		   
		   Log.d("StreamPlay","FIN play");
	 }

	public void stop() {
		/*btnPlay.setImageResource(R.drawable.btn_play);*/
		mp.stop();
		mp.reset();
		
		apiSayIsPlaying = false;
		setValues(SEND.TEXTDATA);
		setValues(SEND.NOALBUMART);
		
		 mHandler.removeCallbacks(mUpdateTimeTask);
		 
		if (notificationManager != null){
			notificationManager.cancel(NotifyID);
		}
		myCallback.sendValue(SEND.StopPlaying);
		
		//MainActivity.SetPlay(false);
		RCChangeState(false);
	}
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		  //Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
	}

	@Override
	 public boolean onError(MediaPlayer mp, int what, int extra) {
		/*btnPlay.setImageResource(R.drawable.btn_play);*/
		
		//Dialog dialog = new Dialog(this);
		//TextView txt = (TextView)dialog.findViewById(R.id..textbox);

		//txt.setText(getString(R.string.message));
		//dialog.setTitle("Serveur indisponible.");

		//dialog.show();
		
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
		  myCallback.errorReport(sb.toString());
		  Log.e(TAG, sb.toString());
		  return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		  Log.d(TAG, "Stream is prepared");
		  /*btnPlay.setImageResource(R.drawable.btn_pause);*/
		  mp.start();
	}
	
    /*static public Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);  
    }*/
	
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

	@Override
	public void onCompletion(MediaPlayer mp) {
		  stop();
	}


//@SuppressLint("InlinedApi")
//RemoteControlClient mRemoteControlClient;

@SuppressLint("InlinedApi")
public void RCChangeState(boolean playing){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		if (playing){
			RC.updateState(true);
			//mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}else{
			RC.updateState(false);
			//mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		}
    }
}


//private final Context context;

@SuppressLint("InlinedApi")
public void RCStartRemote()
{
	Log.d("StreamPlay","DEBUT RCStartRemote");
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	
    	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,  AudioManager.AUDIOFOCUS_GAIN);
        
    	ComponentName myEventReceiver = new ComponentName(this, MediaButtonReceiver.class);
    	audioManager.registerMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
    	Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
	    mediaButtonIntent.setComponent(myEventReceiver);
    	
    	RC = new RemoteController();
		RC.register(getApplicationContext(), mediaButtonIntent, audioManager);

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
        audioManager.registerRemoteControlClient(mRemoteControlClient);*/
    }
    Log.d("StreamPlay","FIN RCStartRemote");
}

@SuppressLint("InlinedApi")
public void RCChangeCover()
{
	Log.d("StreamPlay","DEBUT RCChangeCover");
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	if (RC!=null) RC.updateCover(bitmapART);
    }
    Log.d("StreamPlay","FIN RCChangeCover");
}

@SuppressLint("InlinedApi")
public void RCChangeInfos()
{
	Log.d("StreamPlay","DEBUT RCChangeInfos");
    //register buttons from interface 
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    	if (RC!=null) RC.updateMetadata(Artiste, Titre, Emission);
    }
    Log.d("StreamPlay","FIN RCChangeInfos");
}


	@Override
	public void onAudioFocusChange(int arg0) {
		// TODO Auto-generated method stub
		long i=0;
		if (i==1){
			
		}
	}


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return localBinder;
	}
	
	public class LocalBinder extends Binder {
	    StreamPlay getService() {
	        return StreamPlay.this;
	    }
	}

    public interface ICallback {
        public void sendValue(SEND value);
        public void errorReport(String Text);
    }
    
    private void setValues(SEND value)
    {
    	
        switch (value) {
	        case TEXTDATA:
	        	RCChangeInfos();
				break;
	        case ALBUMART:
	        	RCChangeCover();
	        	break;
	        case NOALBUMART:
	        	RCChangeCover();
	        	break;
	        /*case RESET:
	        	if (notificationManager!=null){
	        		notificationManager.cancelAll();
	        	}
	        	if(mHandler!=null && mUpdateTimeTask!=null){
	        		mHandler.removeCallbacks(mUpdateTimeTask);
	        	}
	        	lastSong="";
	        	bitmapART=null;
	        	Artiste="";
	        	Titre="";
	        	Album="";
	        	Emission="";
	        	break;*/
			default:
				break;
	    }

    	//Callback
        if (myCallback != null){
        	myCallback.sendValue(value);
        }else{
        	stopSelf();
        }
    }
 
    private ICallback myCallback;
 
    public void registerCallback(ICallback cb) {
        myCallback = cb;
    }
	
	class RequestTask extends AsyncTask<String, String, String>{

		//private static final int IO_BUFFER_SIZE = 0;
		private String typea; 
		
		    @Override
		    protected String doInBackground(String... uri) {
		        int BUFFER_SIZE = 2000;
		        InputStream in = null;
		        try {
		        	typea = uri[1];
		            in = OpenHttpConnection(uri[0]);
		            if (in==null){
		            	return "";
		            }
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            e1.printStackTrace();
		            return "";
		        }
		         
		        InputStreamReader isr = null;
				isr = new InputStreamReader(in);
		        int charRead;
		        String str = "";
		        char[] inputBuffer = new char[BUFFER_SIZE];          
		        try {
		            while ((charRead = isr.read(inputBuffer))>0)
		            {                    
		                //---convert the chars to a String---
		                String readString = String.copyValueOf(inputBuffer, 0, charRead);
		                str += readString;
		                inputBuffer = new char[BUFFER_SIZE];
		            }
		            in.close();
		        } catch (IOException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		            return "";
		        } 
		        
		       byte[] bt = str.getBytes(UTF8_CHARSET);
		       return new String(Arrays.copyOfRange(bt,3,bt.length),iso8859_CHARSET);
		    }
		    
		    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
		    private final Charset iso8859_CHARSET = Charset.forName("iso8859-1");
		    
		    private InputStream OpenHttpConnection(String urlString) 
		    	    throws IOException
		    	    {
		    	        InputStream in = null;
		    	        int response = -1;
		    	                
		    	        URL url = new URL(urlString); 
		    	        URLConnection conn = url.openConnection();
		    	                  
		    	        if (!(conn instanceof HttpURLConnection))                     
		    	            throw new IOException("Not an HTTP connection");
		    	         
		    	        try{
		    	        	
		    	            HttpURLConnection httpConn = (HttpURLConnection) conn;
		    	            httpConn.setAllowUserInteraction(false);
		    	            httpConn.setInstanceFollowRedirects(true);
		    	            httpConn.setRequestMethod("GET");
		    	            httpConn.setUseCaches (false);
		    	            
		    	            httpConn.connect(); 
		    	 
		    	            response = httpConn.getResponseCode();                 
		    	            if (response == HttpURLConnection.HTTP_OK) {
		    	                in = httpConn.getInputStream();                                 
		    	            }         
		    	            
		    	        }
		    	        catch (Exception ex)
		    	        {
		    	        	myCallback.errorReport("Error connecting");
		    	        	//Toast.makeText(getApplicationContext(), "Error connecting", Toast.LENGTH_SHORT).show();
		    	            //throw new IOException("Error connecting");            
		    	        }
		    	        return in;     
		    	    }

		    @Override
		    protected void onPostExecute(String result) {
		        super.onPostExecute(result);
		        //Do anything with response..

		        if (typea == "DIRECT") {

		        }else if (typea == "API") {
			        try{
			        	Log.i("RESULT-973", result);
			        	//result = "{\"show\":\"\",\"artiste\":\"Pearl Jam\",\"titre\":\"Sirens\",\"pochette\":\"http:\\/\\/infologique.net\\/api\\/album\\/pochette\\/0f13df64fa8b342873973debf844dc52.jpg\"}";
			            JSONObject json = new JSONObject(result);
			            Emission = json.getString("show");
			            Artiste = json.getString("artiste");
			            Titre = json.getString("titre");
			            Album = json.getString("albumName");
			            Pochette = json.getString("pochette");
			            apiSayIsPlaying = json.getBoolean("playing");
			            //SetInfos();
			            //GererBitmap(bitmapART);
			            
			            if (lastSong == null) lastSong = "";
			            
			            if (!lastSong.equals(result)){
				        	
				        	lastSong = result;
				        	
				        	setValues(SEND.TEXTDATA);

					        if (Pochette.equals("")){
					        	//((ImageView) findViewById(R.id.adele)).setImageResource(R.drawable.logotrans);
					        	//imageBitmap = null;
					        	//RCChangeCover(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.adeleo));
					        	//RCChangeCover(null);
					        	bitmapART = null;
					        	setValues(SEND.NOALBUMART);
					        	//SetNoArt();
					        	//setNotify(false);
					        	setNotify();
					        }else{
					        	new DownloadImageTask().execute(URLDecoder.decode(Pochette, "UTF-8"));
					        	//myCallback.sendValue(SEND.ALBUMART);
					        }

					    }
			            
			        } catch(Exception e){
			        	//SetInfos();
			        	//imageBitmap = null;
			        	//setNotify(false);
			        	//SetNoArt();
			        	setValues(SEND.NOALBUMART);
			            e.printStackTrace();
			        }
		        }
		    }
		}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		 //ImageView bmImage;
		
		 public DownloadImageTask() {
		     //this.bmImage = bmImage;
		 }
		
		 protected Bitmap doInBackground(String... urls) {
		     String urldisplay = urls[0];
		     Bitmap tempIMG = null;
		     try {
		         InputStream in = new java.net.URL(urldisplay).openStream();
		         tempIMG = BitmapFactory.decodeStream(in);
		     } catch (Exception e) {
		         Log.e("Error", e.getMessage());
		         e.printStackTrace();
		     }
		     return tempIMG;
		 }
		
		 protected void onPostExecute(Bitmap result) {
			 
			 if (result != null)
			 {
				 bitmapART = result;
				 setNotify();
				 setValues(SEND.ALBUMART);
				 //bmImage.setImageBitmap(Bitmap.createBitmap(result));
			 }else{
				 
			 }
			 //RCChangeCover(result);
			 //GererBitmap(result);
			 //setNotify(false);
		 }
		}
	
	 public enum SEND{
		 TEXTDATA,
		 NOALBUMART,
		 ALBUMART,
		 StartPlaying,
		 StopPlaying
	 }

	
	
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   Log.d(TAG,"PING!!!!!!!!!!!!!");
			   new RequestTask().execute(API_URL, "API");
			   mHandler.postDelayed(mUpdateTimeTask, delayUpdate); //30000
		   }
		};


		private void setNotify()
		{
	    	Context context = getApplicationContext();
			notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

			//Notification updateComplete = new Notification();
			//updateComplete.icon =R.drawable.ic_launcher;
			//updateComplete.largeIcon = imageBitmap;
			//updateComplete. = imageBitmap;
			
			//CharSequence contentTitle = Titre;
			//CharSequence contentText = songArtistLabel.getText();
			
			CharSequence ticket = Titre + " - " + Artiste;
			/*if (first){
				ticket = "O97,3 - Le meilleur de la musique";*/
			//}else{
				
			//}
			
			//updateComplete.tickerText = ticket;
			//updateComplete.when = System.currentTimeMillis();
			
			Intent notificationIntent = new Intent(context, MainActivity.class);
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				

				/*updateComplete.setLatestEventInfo(context, contentTitle, contentText, contentIntent);*/
					
				/*updateComplete.flags+=Notification.FLAG_NO_CLEAR;*/
					
				/*notificationManager.cancelAll();
				notificationManager.notify(NotifyID, updateComplete);*/
				
				Bitmap imageBitmapAnother = null;
				if (bitmapART!=null){
					//imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 128, 128, false);
					imageBitmapAnother = scaleToFill(bitmapART,128,128);
				}

				NotificationCompat.Builder noti = new NotificationCompat.Builder(context)
						 .setTicker(ticket)
				         .setContentTitle(Titre)

				         .setContentText(Artiste)
				         .setSmallIcon(R.drawable.ic_launcher)
				         .setLargeIcon(imageBitmapAnother)
				         
				         //.addAction(R.drawable.ic_launcher, "Stop", contentIntent)
				         
				         /*.setStyle(new NotificationCompat.InboxStyle()
				         .addLine("ligne 1 ")
				         .addLine("Ligne 2 ")
				         .setSummaryText("+3 more"))*/
				         
				         .setContentIntent(contentIntent)
				         .setOngoing(true)
				         .setWhen(System.currentTimeMillis());
				        		
				        	       
		         
			     /*if (emission!=null && !emission.equals("")){
			    	 noti.setSubText(emission);
			     }*/
				
			     if (Album!=null && !Album.equals("")){
			    	 noti.setSubText(Album);
			     }

	
				//NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				notificationManager.notify(NotifyID, noti.build());
		}
		
	    static public Bitmap scaleToFill(Bitmap b, int width, int height) {
	        float factorH = height / (float) b.getWidth();
	        float factorW = width / (float) b.getWidth();
	        float factorToUse = (factorH > factorW) ? factorW : factorH;
	        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);  
	    }
}



