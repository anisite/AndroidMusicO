package com.infologique.o973;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
      final KeyEvent event = (KeyEvent) intent
          .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
      if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
        	if (MainActivity.SPservice.isPlaying()){
        		MainActivity.SPservice.stop();
        	}else{
        		MainActivity.SPservice.play();
        	}
        }
      }
    }
  }

}