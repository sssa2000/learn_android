package com.example.testmediaplayer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity  implements
	OnBufferingUpdateListener, 
	OnCompletionListener,
	OnPreparedListener, 
	OnVideoSizeChangedListener, 
	OnErrorListener,
	OnInfoListener,
	SurfaceHolder.Callback {

	private static final String TAG = "testmediaplayer";
	
    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private String path;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private  TextView mTv;
    private static final String stream_mp4="http://7xrxa5.com1.z0.glb.clouddn.com/recommend_tag1_1.mp4";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //text view
        mTv=(TextView)findViewById(R.id.textView1);
        mTv.setMovementMethod(new ScrollingMovementMethod());
        
        //player
        InitPlayer();
        path=stream_mp4;
        
        //button
        final Button playbtn=(Button)findViewById(R.id.button1);
        playbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	LogMsg("Play onClick!");
            	if(!mMediaPlayer.isPlaying()){
            		playVideo();
            	}
            	
            }
        });    	
        
        final Button pauseBtn=(Button)findViewById(R.id.button2);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	LogMsg("pauseBtn onClick!");
            	if(mMediaPlayer.isPlaying()){
            		pauseBtn.setText("resume");
            		mMediaPlayer.pause();
            	}
            	else{
            		pauseBtn.setText("pause");
            		mMediaPlayer.start();
            	}
            	
            }
        }); 
       
        final Button stopbtn=(Button)findViewById(R.id.button3);
        stopbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	LogMsg("stopbtn onClick!");
            	mMediaPlayer.stop();
            	
            }
        }); 
    }    
    
    private void LogMsg(String str){    	
    	Log.v(TAG,str);
    	mTv.append(str);
    	mTv.append("\n");
    }
    
    private void InitPlayer()
    {
        mPreview = (SurfaceView) findViewById(R.id.surface);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediaPlayer = new MediaPlayer();
        LogMsg("Init Player Done!");
    }
    private void playVideo() {
    	
    	doCleanUp();   
    	try{
            
    		LogMsg("before setDataSource");
            mMediaPlayer.setDataSource(this,Uri.parse(stream_mp4));
            LogMsg("setDataSource done");
            
            mMediaPlayer.setDisplay(holder);
            
            LogMsg("before prepareAsync");
            //mMediaPlayer.prepare();
            mMediaPlayer.prepareAsync(); //change to async mode
            LogMsg("after prepareAsync");
            
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	}
    	catch(Exception e){
    		LogMsg("Exception when playVideo! see logcat in debuger!");
    		LogMsg(e.getStackTrace().toString());
    		e.printStackTrace();
    	}
    }
    

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
    	LogMsg("startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }


	public boolean onError(MediaPlayer mp, int what, int extra) {

		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			LogMsg("MEDIA_ERROR_SERVER_DIED");
			mMediaPlayer.reset();// 可调用此方法重置
		} else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
			LogMsg("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
		} else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			LogMsg("MEDIA_ERROR_UNKNOWN");
		}
		return false;
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		LogMsg("onBufferingUpdate percent:" + percent);

	}

	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		// 当文件中的音频和视频数据不正确的交错时，将触发如下操作。
		// 在一个正确交错的媒体文件中，音频和视频样本依序排列，从而使得播放能够有效平稳的进行。
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			LogMsg("MEDIA_INFO_BAD_INTERLEAVING extar is :" + extra);
			break;
		// 当新的元数据可用时，将触发它
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			LogMsg("MEDIA_INFO_METADATA_UPDATE extar is :" + extra);
			break;
		// 媒体不能正确定位，意味着它可能是一个在线流
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			LogMsg("MEDIA_INFO_NOT_SEEKABLE extar is :" + extra);
			break;
		// 当无法播放视频时，可能是将要播放视频，但是视频太复杂
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			LogMsg("MEDIA_INFO_VIDEO_TRACK_LAGGING extar is :" + extra);
			break;
		case MediaPlayer.MEDIA_INFO_UNKNOWN:
			LogMsg("MEDIA_INFO_UNKNOWN extar is :" + extra);
			break;
		}
		return false;
	}
    
    
    public void onCompletion(MediaPlayer arg0) {
    	LogMsg("onCompletion called");
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    	LogMsg("onVideoSizeChanged called,w="+width+",h="+height);
        if (width == 0 || height == 0) {
        	LogMsg("invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void onPrepared(MediaPlayer mediaplayer) {
    	LogMsg("Prepared OK");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
    	LogMsg("surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    	LogMsg("surfaceDestroyed called");
    }


    public void surfaceCreated(SurfaceHolder holder) {
    	LogMsg("surfaceCreated called");
        //playVideo();


    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
}
