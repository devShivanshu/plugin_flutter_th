package com.example.plugin_flutter_th;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_OK;



/**
 * PluginFlutterThPlugin
 */
public class PluginFlutterThPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;
    private Context context;
    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private int mScreenDensity;
    private static int DISPLAY_WIDHT = 720;
    private static int DISPLAY_HEIGHT = 1200;
    private String videoUri = "";
    private RtcEngine mRtcEngine;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaRecorder mediaRecorder;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "plugin_flutter_th");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initRecorder() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            mediaRecorder.setVideoSize(DISPLAY_WIDHT, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mediaRecorder.setVideoFrameRate(60);

            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +
                    new StringBuilder("/EMPARDIGM").append(new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss")
                            .format(new Date())).append(".mp4").toString();
            mediaRecorder.setOutputFile(videoUri);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();

            recordScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void recordScreen() {
        if (mediaProjection == null) {
            activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {

        return mediaProjection.createVirtualDisplay("MainActivity", DISPLAY_WIDHT, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);

    }


    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugin_flutter_th");
        channel.setMethodCallHandler(new PluginFlutterThPlugin());
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("switchOnScreenShare")) {
//      mRtcEngine = (RtcEngine) call.argument("engine");
//            if (mRtcEngine == null) {
//                Log.d("ENPARADIGM", "6" + call.argument("engine"));
//            } else {
//                Log.d("ENPARADIGM", "7");
//            }
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mScreenDensity = metrics.densityDpi;
            DISPLAY_HEIGHT = metrics.heightPixels;
            DISPLAY_WIDHT = metrics.widthPixels;
            mediaRecorder = new MediaRecorder();
            mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            initRecorder();
        }
        else if(call.method.equals("switchOffScreenShare")){
          destroyMediaProjection();
          stopRecordScreen();

        }
        else {
            result.notImplemented();
        }
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }
        if (resultCode != RESULT_OK) {
            return false;
        }
        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
        //initAgoraEngineAndJoinChannel();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            super.onStop();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaProjection = null;
            stopRecordScreen();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void stopRecordScreen() {
        if (virtualDisplay == null) return;
        virtualDisplay.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            destroyMediaProjection();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        binding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("ENPARADIGM", "5");
            } else {


            }
        }
        return false;
    }
  private void initAgoraEngineAndJoinChannel() {
    initializeAgoraEngine();
    setupVideoProfile();
    setupLocalVideo();
    joinChannel();
  }
  private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

    @Override
    public void onUserOffline(int uid, int reason) {
      Log.d("ENPARADIGM", "onUserOffline: " + uid + " reason: " + reason);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
      Log.d("ENPARADIGM", "onJoinChannelSuccess: " + channel + " " + elapsed);
    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
      Log.d("ENPARADIGM", "onUserJoined: " + (uid&0xFFFFFFL));
        setupRemoteView(uid);
      
    }
  };

  private void initializeAgoraEngine() {
    try {
      mRtcEngine = RtcEngine.create(context.getApplicationContext(), "4383ba50c858415e8feb5b03c6426fe4", mRtcEventHandler);
    } catch (Exception e) {
      Log.e("ENPARADIGM", Log.getStackTraceString(e));

      throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
    }
  }

  private void setupVideoProfile() {
    mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
    mRtcEngine.enableVideo();
    //mRtcEngine.setVideoEncoderConfiguration(mVEC);
    mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
  }
//
  private void setupLocalVideo() {
    SurfaceView camV = RtcEngine.CreateRendererView(context.getApplicationContext());
    camV.setZOrderOnTop(true);
    camV.setZOrderMediaOverlay(true);
  //  mFlCam.addView(camV, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      
    mRtcEngine.setupLocalVideo(new VideoCanvas(camV, VideoCanvas.RENDER_MODE_FIT,0));
    mRtcEngine.enableLocalVideo(false);
  }

  private void setupRemoteView(int uid) {
    SurfaceView ssV = RtcEngine.CreateRendererView(context.getApplicationContext());
    ssV.setZOrderOnTop(true);
    ssV.setZOrderMediaOverlay(true);
    //mFlSS.addView(ssV, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    mRtcEngine.setupRemoteVideo(new VideoCanvas(ssV, VideoCanvas.RENDER_MODE_FIT, uid));
  }

  private void joinChannel() {
    mRtcEngine.joinChannel("0064383ba50c858415e8feb5b03c6426fe4IABdc+Q2BhqqCBDDrUv0lLnwtKXfLc2Rdos8mRho0LmpE/coDtQAAAAAEAARj/Pi4QOuXwEAAQDgA65f", "shi_123","Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
  }

  private void leaveChannel() {
    mRtcEngine.leaveChannel();
  }

}
