package com.laser.ui.layers;


import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.vlc.WeakHandler;

import com.laser.utils.LaserConstants;
import com.laser.utils.LaserSettings;
import com.laser.utils.LaserConstants.UnderlayModes;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.Window;
import android.widget.FrameLayout;

public class PlayerLayer implements IVideoPlayer {

    private final String TAG = PlayerLayer.class.getSimpleName();	
	private Context context;
	private LaserSettings settings;
    
	private FrameLayout playerContainer;    //400
    private FrameLayout playerFrame;		//401	
    private SurfaceView player;				//402
    
    private SurfaceHolder surfacePlayerHolder;
    private LibVLC mLibVLC;
    private boolean bPlayerStarted = false;
    private String mLocation;
    
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;    
    private static final int SURFACE_SIZE = 3;  
    
    private int savedIndexPosition = -1;	// Playlist
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;
    //private AudioManager mAudioManager;	// Volume
        
    public interface PlayerLayerListener{
		public void OnSetVolumeControlStream(int stream);
		public Window GetWindow();
	}
	
	private PlayerLayerListener listener;
	public void setListener(PlayerLayerListener listener)
	{
		this.listener = listener;
        listener.OnSetVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
    
    public PlayerLayer(Context context, LaserSettings settings)
    {
    	this.context = context;
    	this.settings = settings;
    	
    	createPlayer();
    }
    
    public View getPlayerLayer()
    {
    	return playerContainer;
    }

    private void createPlayer()
	{
		playerContainer = new FrameLayout(context);
		playerFrame = new FrameLayout(context);
		player = new SurfaceView(context);
		
		playerContainer.setId(400);
		playerFrame.setId(401);
		player.setId(402);
		
		FrameLayout.LayoutParams lpSurface = new FrameLayout.LayoutParams(1, 1);
		lpSurface.gravity = Gravity.CENTER_VERTICAL;
		FrameLayout.LayoutParams lpFrame = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		lpFrame.gravity = Gravity.CENTER;
		FrameLayout.LayoutParams lpContainer = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

		playerContainer.setLayoutParams(lpContainer);
		playerFrame.setLayoutParams(lpFrame);
		player.setLayoutParams(lpSurface);
		
		playerFrame.addView(player);
		playerContainer.addView(playerFrame);
		//baseLayout.addView(playerContainer);
		
		//playerContainer = (FrameLayout)findViewById(R.id.player_surface_container);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		/*        if(LibVlcUtil.isICSOrLater())
		        	getWindow().getDecorView().findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(
		                    new OnSystemUiVisibilityChangeListener() {
		                        @Override
		                        public void onSystemUiVisibilityChange(int visibility) {
		                            setSurfaceSize(mVideoWidth, mVideoHeight, mSarNum, mSarDen);
		                        }
		                    }
		            );
		*/		       
        //mSurfacePlayer = (SurfaceView) findViewById(R.id.player_surface);
		surfacePlayerHolder = player.getHolder();
        //mSurfacePlayerFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
        
        String chroma = pref.getString("chroma_format", "");
        if(LibVlcUtil.isGingerbreadOrLater() && chroma.equals("YV12")) {
        	surfacePlayerHolder.setFormat(ImageFormat.YV12);
        } else if (chroma.equals("RV16")) {
        	surfacePlayerHolder.setFormat(PixelFormat.RGB_565);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGB_565, info);
        } else {
        	surfacePlayerHolder.setFormat(PixelFormat.RGBX_8888);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGBX_8888, info);
        }
        surfacePlayerHolder.addCallback(mSurfaceCallback);

        //mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        try {
            mLibVLC = LibVLC.getLibVlcInstance(context.getApplicationContext());
        } catch (LibVlcException e) {
            Log.d(TAG, "LibVLC initialisation failed");
            return;
        }
	}
    
	public void onActivityResult(String prevCameraAddress,
			LaserSettings settings) 
	{
		this.settings = settings;
		if (!prevCameraAddress.equals(settings.CAMERA_ADDRESS) || settings.GOPRO_ENABLED)
		{
			if (LaserConstants.UNDERLAY_MODE == UnderlayModes.CAM)
			{
		        if (mLibVLC.isPlaying())
		        	mLibVLC.stop();
				savedIndexPosition = -1;
				startPlayer();
			}
		}
	}
    
    public void onResume()
    {    
        //AudioServiceController.getInstance().bindAudioService(this);
        if (bPlayerStarted)
        	startPlayer();
        /*
         * if the activity has been paused by pressing the power button,
         * pressing it again will show the lock screen.
         * But onResume will also be called, even if vlc-android is still in the background.
         * To workaround that, pause playback if the lockscreen is displayed
         */
        mHandlerPlayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLibVLC != null && mLibVLC.isPlaying())
                {
					KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
                    if (km.inKeyguardRestrictedInputMode())
                    {
                        mLibVLC.pause();
                        bPlayerStarted = false;
                    }
                }
            }}, 500);
    }
    
    public void onPause()
    {
		long time = mLibVLC.getTime();
        long length = mLibVLC.getLength();
        //remove saved position if in the last 5 seconds
        if (length - time < 5000)
            time = 0;
        else
            time -= 5000; // go back 5 seconds, to compensate loading time

        /*
         * Pausing here generates errors because the vout is constantly
         * trying to refresh itself every 80ms while the surface is not
         * accessible anymore.
         * To workaround that, we keep the last known position in the playlist
         * in savedIndexPosition to be able to restore it during onResume().
         */
        pausePlayer();
    }
        
    public void startPlayer() {    	
    	if (settings.GOPRO_ENABLED)
    		mLocation = "http://10.5.5.9:8080/live/amba.m3u8";
    	else
        	mLocation = settings.CAMERA_ADDRESS;    	
    	
        /* Start / resume playback */
        if (savedIndexPosition > -1) {
            mLibVLC.setMediaList();
            mLibVLC.playIndex(savedIndexPosition);
        } else if (mLocation != null && mLocation.length() > 0) {
            mLibVLC.setMediaList();
            mLibVLC.getMediaList().add(new Media(mLibVLC, mLocation));
            savedIndexPosition = mLibVLC.getMediaList().size() - 1;
            mLibVLC.playIndex(savedIndexPosition);
        }
        bPlayerStarted = true;
    }    

	public void stopPlayer() {
        if (mLibVLC.isPlaying())
        	mLibVLC.stop();
        bPlayerStarted = false;
	}
	
	public void pausePlayer() {
        if (mLibVLC.isPlaying())
        	mLibVLC.stop();
	}
    
    public void resetPlayer()
    {
    	stopPlayer();
    	savedIndexPosition = -1;
    	startPlayer();
    }
	
	public void setVisibility(int visible)
	{
		playerContainer.setVisibility(visible);
		player.setVisibility(visible);
	}
	
	/**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandlerPlayer = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends WeakHandler<PlayerLayer> {
        public VideoPlayerHandler(PlayerLayer owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	PlayerLayer owner = getOwner();
            if(owner == null) // WeakReference could be GC'ed early
                return;

            switch (msg.what) {
                case SURFACE_SIZE:
                	owner.changeSurfaceSize();
                    break;
               }
        }
    };
    
    private void changeSurfaceSize() {
        // get screen size
        int dw = listener.GetWindow().getDecorView().getWidth();
        int dh = listener.GetWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (dw > dh && isPortrait || dw < dh && !isPortrait) {
            int d = dw;
            dw = dh;
            dh = d;
        }

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        double density = (double)mSarNum / (double)mSarDen;
        if (density == 1.0) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * density;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = (double) dw / (double) dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = (int) (dw / ar);
                break;
            case SURFACE_FIT_VERTICAL:
                dw = (int) (dh * ar);
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = (int) vw;
                break;
        }

        // force surface buffer size
        surfacePlayerHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) player.getLayoutParams();
        lp.width  = dw * mVideoWidth / mVideoVisibleWidth;
        lp.height = dh * mVideoHeight / (mVideoVisibleHeight - 20);	// -20 per togliere la riga verde dalla gopro
        player.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = (FrameLayout.LayoutParams) playerFrame.getLayoutParams();
        lp.width = dw;
        lp.height = dh;
        playerFrame.setLayoutParams(lp);

        player.invalidate();
    }

	
    /**
     * attach and disattach surface to the lib
     */
    private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        	Log.d("SURFACE", "player changed");
            if(format == PixelFormat.RGBX_8888)
                Log.d(TAG, "Pixel format is RGBX_8888");
            else if(format == PixelFormat.RGB_565)
                Log.d(TAG, "Pixel format is RGB_565");
            else if(format == ImageFormat.YV12)
                Log.d(TAG, "Pixel format is YV12");
            else
                Log.d(TAG, "Pixel format is other/unknown");
            mLibVLC.attachSurface(holder.getSurface(), PlayerLayer.this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        	Log.d("SURFACE", "player created");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        	Log.d("SURFACE", "player destroyed");
        	stopPlayer();
            mLibVLC.detachSurface();
        }
    };
	
    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        if (width * height == 0)
            return;

        // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth  = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        Message msg = mHandlerPlayer.obtainMessage(SURFACE_SIZE);
        mHandlerPlayer.sendMessage(msg);
    }

	public void onConfigurationChanged() {
        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
	}

	public void removeViews() {
		surfacePlayerHolder.removeCallback(mSurfaceCallback);		
		playerFrame.removeView(player);
		playerContainer.removeView(playerFrame);
	}

	public void addViews() {
		surfacePlayerHolder.addCallback(mSurfaceCallback);			
		playerFrame.addView(player);
		playerContainer.addView(playerFrame);
	}



}
