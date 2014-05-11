package com.infologique.o973;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController.MetadataEditor;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class RemoteController { 
	  private RemoteControlClient remoteControlClient;
	 
	  /** 
	   * Register the remote control at the audio manager. 
	   */ 
	  public void register(Context context,Intent mediaButtonIntent, AudioManager audioManager) {
	    if (remoteControlClient == null) {
	      //ComponentName myEventReceiver = new ComponentName(context, MediaButtonReceiver.class);
	      //audioManager.registerMediaButtonEventReceiver(myEventReceiver);
	    	
	      // build the PendingIntent for the remote control client 
	     // Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
	      //mediaButtonIntent.setComponent(myEventReceiver);
	      // create and register the remote control client 
	      PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0,
	          mediaButtonIntent, 0);
	      remoteControlClient = new RemoteControlClient(mediaPendingIntent);
	      remoteControlClient
	          .setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
	      audioManager.registerRemoteControlClient(remoteControlClient);
	    } 
	  } 
	 
	  /** 
	   * Update the state of the remote control. 
	   */ 
	  public void updateState(boolean isPlaying) {
	    if (remoteControlClient != null) {
	      if (isPlaying) {
	        remoteControlClient
	            .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
	      } else { 
	        remoteControlClient
	            .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
	      } 
	    } 
	  } 
	 
	  /** 
	   * Updates the state of the remote control to "stopped". 
	   */ 
	  public void stop() { 
	    if (remoteControlClient != null) {
	      remoteControlClient
	          .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
	    } 
	  } 
	 
	  /** 
	   * Set the metadata of this episode according to the episode. 
	   */ 
	  public void updateMetadata(String Artiste, String Titre, String Show) {
	    if (remoteControlClient != null) {
	      android.media.RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(false);
	      //DBFeed feed = episode.getFeed();
	 
	      //ImageCache imageCache = App.get().getImageCache();
	      //String imgUrl = episode.getImgUrl();
	      /*BitmapDrawable episodeIcon = imageCache.getResource(imgUrl);
	      if (episodeIcon.equals(imageCache.getDefaultIcon())) {
	        episodeIcon = imageCache.getResource(feed.getImgUrl());
	      } 
	      editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK,
	          episodeIcon.getBitmap());*/
	 
	      //editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);
	 
	      editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, Artiste);
	      editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, Titre);
	      editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, Show);
	      editor.apply();
	    } 
	  } 
	  
	  public void updateCover(Bitmap bit) {
		    if (remoteControlClient != null) {
		      android.media.RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(false);
		      //DBFeed feed = episode.getFeed();
		 
		      //ImageCache imageCache = App.get().getImageCache();
		      //String imgUrl = episode.getImgUrl();
		      /*BitmapDrawable episodeIcon = imageCache.getResource(imgUrl);
		      if (episodeIcon.equals(imageCache.getDefaultIcon())) {
		        episodeIcon = imageCache.getResource(feed.getImgUrl());
		      } */
		      editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, bit);
		 
		      //editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);
		 
		      //editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, Artiste);
		      //editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, Titre);
		      editor.apply();
		    } 
		  } 
	 
	  /** 
	   * Release the remote control. 
	   */ 
	  public void release() { 
	    remoteControlClient = null;
	  } 
	} 