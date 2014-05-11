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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.infologique.o973.RemoteController;

public class StreamPlay extends Service implements
										MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
										MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, OnAudioFocusChangeListener  
{

	//----Public
	public Bitmap bitmapART;
	public String Artiste;
	public String Titre;
	public String Album;
	public String Emission;
	public String Pochette;
	public boolean apiSayIsPlaying;
	
	//----Private
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
	
	private final IBinder localBinder = new LocalBinder();	
	private String TAG = getClass().getSimpleName();
	private static MediaPlayer mp;
	private BroadcastReceiver receive;

	public enum SEND{
		 TEXTDATA,
		 NOALBUMART,
		 ALBUMART,
		 StartPlaying,
		 StopPlaying
	}
	
	@Override
	public void onDestroy() {

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

		Uri myUri = Uri.parse("http://xlnetwork.info:5400/;");
		mp = MediaPlayer.create(this, myUri);
	}

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
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

		lastSong ="";
		
		new RequestTask().execute(API_URL, "API");
		
		if (mp!=null){
			mHandler.postDelayed(mUpdateTimeTask, delayUpdate);

			RCStartRemote();
	
			Uri myUri = Uri.parse("http://xlnetwork.info:5400/;"); //5400 // 8020
		
			try {
				mp.reset();
				mp.setDataSource(this, myUri);
				RCChangeState(true);
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
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

	public void stop() 
	{
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

		RCChangeState(false);
	}
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		  //Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
	}

	@Override
	 public boolean onError(MediaPlayer mp, int what, int extra) {
		
		  StringBuilder sb = new StringBuilder();
		  sb.append("Erreur de lecture: ");
		  switch (what) {
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			   sb.append("Not Valid for Progressive Playback");
			   break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			   sb.append("Le serveur est mort!");
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
		  mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		  stop();
	}


	@SuppressLint("InlinedApi")
	public void RCChangeState(boolean playing){
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (playing){
				RC.updateState(true);
			}else{
				RC.updateState(false);
			}
	    }
	}
	
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
	public void onAudioFocusChange (int focusChange) 
	{
        switch (focusChange) 
       {
              case AudioManager.AUDIOFOCUS_GAIN:
                   play();
                   break;
              case AudioManager.AUDIOFOCUS_LOSS:
                   stop();
                   break;
              case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                   stop();
                   break;
              case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            	  break;
        }
	}

	@Override
	public IBinder onBind(Intent arg0) {
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
		            e.printStackTrace();
		            return "";
		        } 
		        
		       //Workaround for android < 4.0
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
		    	        	myCallback.errorReport("Erreur de connexion au serveur audio");         
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
			            
			            if (lastSong == null) lastSong = "";
			            
			            if (!lastSong.equals(result)){
				        	
				        	lastSong = result;
				        	
				        	setValues(SEND.TEXTDATA);

					        if (Pochette.equals("")){

					        	bitmapART = null;
					        	setValues(SEND.NOALBUMART);

					        	setNotify();
					        }else{
					        	new DownloadImageTask().execute(URLDecoder.decode(Pochette, "UTF-8"));

					        }

					    }
			            
			        } catch(Exception e){
			        	setValues(SEND.NOALBUMART);
			            e.printStackTrace();
			        }
		        }
		    }
		}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		
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
			 }else{
				 
			 }
		 }
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

			CharSequence ticket = Titre + " - " + Artiste;

			Intent notificationIntent = new Intent(context, MainActivity.class);
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				
				Bitmap imageBitmapAnother = null;
				if (bitmapART!=null){
					imageBitmapAnother = scaleToFill(bitmapART,128,128);
				}

				NotificationCompat.Builder noti = new NotificationCompat.Builder(context)
						 .setTicker(ticket)
				         .setContentTitle(Titre)

				         .setContentText(Artiste)
				         .setSmallIcon(R.drawable.ic_launcher)
				         .setLargeIcon(imageBitmapAnother)
				         
				         .setContentIntent(contentIntent)
				         .setOngoing(true)
				         .setWhen(System.currentTimeMillis());
				        		
			     if (Album!=null && !Album.equals("")){
			    	 noti.setSubText(Album);
			     }

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



