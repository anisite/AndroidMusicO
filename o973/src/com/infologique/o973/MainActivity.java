package com.infologique.o973;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.infologique.o973.StreamPlay.LocalBinder;
import com.google.android.gms.ads.*;

public class MainActivity extends Activity {

	public static MainActivity instance;
	private static AdView adView;
	private static ImageButton btnPlay;
	private String TAG = getClass().getSimpleName();
	private static TextView songTitleLabel;
	private static TextView songArtistLabel;
	@SuppressWarnings("unused")
	private static ImageView image;
	@SuppressWarnings("unused")
	private static boolean inback = false;
	boolean isBinded = false;
	boolean OnClosing = false;
	
	private String pendingErrorText;
	private Handler mHandler = new Handler();
	
	public static StreamPlay SPservice;
	boolean hasNavigationBar = false;

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
    	if (SPservice!=null) outState.putBoolean("isPlaying", SPservice.isPlaying());
    }

    @SuppressLint("NewApi")
	private void CheckHasKeysOnScreen()
    {
    	//On met Kitkat même si le clavier onScreen peut exister depuis android 14.
    	//car c'est pour mieux paraître avec la transparence!
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
		    hasNavigationBar = !ViewConfiguration.get(getBaseContext()).hasPermanentMenuKey();
		    Log.d("ads - hasNavigationBar", "API 19 et plus - hasNavigationBar " + hasNavigationBar);
		}
		else 
		{
		    hasNavigationBar = false;
		    Log.d("ads - hasNavigationBar", "REJECT - hasNavigationBar " + hasNavigationBar);
		}
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;
		
		setContentView(R.layout.activity_main);
		SetActionBar();
		CheckHasKeysOnScreen();
		
		adView = new AdView(this);
		LinearLayout layout = (LinearLayout)this.findViewById(R.id.pub);
		layout.addView(adView);
		adView.setAdUnitId("b39fc68b3b4847ae");
	    if (hasNavigationBar){
	    	adView.setAdSize(AdSize.BANNER);
	    }else{
	    	adView.setAdSize(AdSize.SMART_BANNER);
	    }
	    AdRequest request = new AdRequest.Builder().build();
	    adView.loadAd(request);
	
		/*DECLARATION DE ÉLÉMENTS À UTILISER*/
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songArtistLabel = (TextView) findViewById(R.id.songArtist);
		image = (ImageView) findViewById(R.id.adele);

		/*REMISE DE L'ÉTAT DE LA FENETRE*/
		 if (savedInstanceState != null) {
		    if (savedInstanceState.getBoolean("isPlaying")) SetPlay(true);
	    }
		 
		/*CAST ET DECLARATIONS DE LA FENETRE*/
        Intent intent = new Intent(this, StreamPlay.class);
        bindService(intent, SPserviceConnection, 0);
        startService(intent);

		btnPlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// check for already playing
					if (SPservice!= null && SPservice.isPlaying()){
						SPservice.stop();
					}else{
						SPservice.play();
					}
				}
			});
	}

	@Override
	protected void onStart() {
	    super.onStart();
	}
	
	@Override
	public void onResume() {
		inback = false;
		
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
       	   inback = true;
           this.moveTaskToBack(true);
           return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	 public void onDestroy() {

		adView.destroy();
		
		if (adView != null) {
		    adView.destroy();
		}
 
		unbindService(SPserviceConnection);
		SPserviceConnection=null;
		
		if (OnClosing){
	    	SPservice.stopSelf();
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
	        	OnClosing = true;
	        	finish();
	            return true;
	     }
		return false;
	}
	
public void SetInfos() {
	Log.d(TAG, "SetInfos - DEBUT");
	//if (SPservice!=null && SPservice.apiSayIsPlaying){
	if (SPservice!=null)
	{
	    songTitleLabel.setText(SPservice.GetTitre());
	    songArtistLabel.setText(SPservice.GetArtiste());
	}else{
		SetDefault();
	}
	Log.d(TAG, "SetInfos - FIN");
}

public void SetDefault()
{
    songTitleLabel.setText(R.string.DefaultSong);
    songArtistLabel.setText(R.string.DefaultArtist);
}

public void SetNoArt()
{
	Log.d(TAG, "SetNoArt - DEBUT");
	if (!isBinded) {
		Log.d(TAG, "SetNoArt - FIN - not binded");
		return;
	}
	((ImageView) findViewById(R.id.adele)).setImageResource(R.drawable.adeleo);
   	
	SetBlurBackgroundKitkat(null);
	Log.d(TAG, "SetNoArt - FIN");
}

private void GererBitmap()
{
	if (SPservice==null ) // || !SPservice.apiSayIsPlaying){
	{
		SetNoArt();
	}
	else
	{
		((ImageView) findViewById(R.id.adele)).setImageBitmap(SPservice.GetAlbumArt(false));
		SetBlurBackgroundKitkat(SPservice.GetAlbumArt(true));
	}
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



private ServiceConnection SPserviceConnection = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder service) {
		//SPservice = ((LocalBinder) binder).getService();
		// We've bound to LocalService, cast the IBinder and get LocalService instance
        LocalBinder binder = (LocalBinder) service;
        SPservice = binder.getService();
       
        isBinded = true;
        
		SPservice.registerCallback(myCallback);
		
    	GererBitmap();
    	SetInfos();
		
	}
	
	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		isBinded = false;
	}
};

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
			pendingErrorText = Text;
			mHandler.postDelayed(mShowError, 1);
	    }
	};

	private Runnable mShowError = new Runnable() {
		   public void run() {
			   Toast to = Toast.makeText(getBaseContext(), "   " + pendingErrorText + "   ", Toast.LENGTH_SHORT);
			   to.show();
		   }
		};
}
	




