package com.android.runintest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.KeyEvent;

public class AudioTestActivity extends BaseActivity {
 
	private String TAG = "AudioTestActivity";
	private AudioTestActivity audioTestActivity;
	
	private AudioManager mAudioManager;
	private MediaPlayer mMediaPlayer;
	private SoundPool mSoundPool;
	private MediaRecorder mMediaRecorder;
	
	private TextView audioTestTv;
	
	private String mAudioFilePath;
	private boolean isRecording = false;
	
	private static final int START_TIME = 10*1000;
	private static final int END_TIME = 3*1000;
	private static final int MIC_TIME = 15*1000;
	private static final int PLAY_TIME = 10*1000 ;
	private static final int MIC_LOOPBACK = 1;
	private static final int GET_AMPLITUDE_TIMES = 12;
	private static final int STOPMEDIAPLAYERANDMAINRECORD = 9;
	private static final int MAINMICRECORDANDPLAY = 10;
	private static final int STOPMAINRECORDPLAY = 11;
	private static final int RECEIVERANDAUXILIARYMICRECORD = 12;
	private static final int STOPRECEIVERANDAUXILIARYMICRECORD= 13;
	private static final int AUXILIARYMICRECORDPLAY = 14;
	private static final int STOPAUXILIARYMICRECORDPLAY = 15;
	private static final int GOTO_VEDIO_TEST = 16;
	
	private static int mGetAmplitudeCount = 0;
	private int soundID;
	
	private boolean mTestSuccess = true;
	
	private SharedPreferences mSharedPreferences = null;
	
	ArrayList<Integer> mMaxAmplitudeList = new ArrayList<Integer>();
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			    case STOPMEDIAPLAYERANDMAINRECORD:
			        stopMediaPlayerAndMainRecord();
			        break;
			    case MAINMICRECORDANDPLAY:
			        mainMicRecordAndPlay();
			        break;
			    case STOPMAINRECORDPLAY:
			        stopMainRecordPlay();
			        break;
			    case RECEIVERANDAUXILIARYMICRECORD:
                    clearArrayList();
			        ReceiverAndauxiliaryMicRecord();
			        break;
			    case STOPRECEIVERANDAUXILIARYMICRECORD:
			        stopReceiverPlayAndAuxiliaryMicRecord();
			        break;
			    case AUXILIARYMICRECORDPLAY:
			        AuxiliaryMicRecordPlay();
			        break;
			    case STOPAUXILIARYMICRECORDPLAY:
			        stopAuxiliaryMicRecordPlay();
			        break;
				case GOTO_VEDIO_TEST:
				    LogRuningTest.printDebug(TAG, "handler GOTO_VEDIO_TEST",  AudioTestActivity.this);
				    goToVedioTest();
				    finish();
				    break;
				default:
				   break;
				}
			
		};
	};
	
	//扬声器播放并主麦录音
	private void speakerAndMainMicRecord(){
		        LogRuningTest.printDebug(TAG, "speakerAndRecord() AudioManager.MODE_NORMAL AudioSource.MIC", this);
		       //设置扬声器播放
				mAudioManager.setMode(AudioManager.MODE_NORMAL);
				//设置声音大小
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				            mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				audioTestTv.setText(getString(R.string.speaker_play_mainmic_record));
				if(mMediaPlayer == null){
					LogRuningTest.printDebug(TAG, "speakerAndRecord() mMediaPlayer is Null",this);
					mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.test1);
					mMediaPlayer.setVolume(1f, 1f);
					//对文件进行循环播放
					mMediaPlayer.setLooping(true);
					mMediaPlayer.start();
				}else{
					LogRuningTest.printDebug(TAG, "speakerAndRecord() mMediaPlayer is not Null",this);
				}
				//进行主麦录音
				inRecord(MediaRecorder.AudioSource.MIC);
				mHandler.sendEmptyMessageDelayed(9, MIC_TIME);
	}

	//停止扬声器播放和主麦录音
	private void stopMediaPlayerAndMainRecord(){
		if (mMediaPlayer.isPlaying()) {
			LogRuningTest.printDebug(TAG, "stopMediaPlayerAndMainRecord() mMediaPlayer  stop() and release()",this);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

        }
		if (isRecording && mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            LogRuningTest.printDebug(TAG, "stopMediaPlayerAndMainRecord() MediaRecorder  stop()  release()",this);
            isRecording = false;
        }
		 audioTestTv.setText(getString(R.string.end_speaker_player_mainmic_record));
         mHandler.sendEmptyMessageDelayed(10, END_TIME);
	}

	//主麦录音文件播放
	private void mainMicRecordAndPlay(){
		LogRuningTest.printDebug(TAG, "mainMicRecordAndPlay()", this);
		audioTestTv.setText(getString(R.string.mainmic_file_play));
		inPlay();
		mHandler.sendEmptyMessageDelayed(11, MIC_TIME);
	}

	//主麦录音文件播放结束
	private void stopMainRecordPlay(){
		LogRuningTest.printDebug(TAG, " stopMainRecordPlay()", this);
		if (mMediaPlayer.isPlaying()) {
			LogRuningTest.printDebug(TAG, "stopMainRecordPlay() mMediaPlayer  stop() and release()",this);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
		audioTestTv.setText(getString(R.string.end_mainmic_file_play));
		mHandler.sendEmptyMessageDelayed(12, END_TIME);
	}

	//听筒播放并副麦录音
	private void ReceiverAndauxiliaryMicRecord(){
		LogRuningTest.printDebug(TAG, "ReceiverAndauxiliaryMicRecord() AudioManager.MODE_IN_CALL", this);
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
	            mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		mSoundPool =new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
		audioTestTv.setText(getString(R.string.receiver_play_auxiliarymic_record));
		if(mSoundPool != null){
			Log.d(TAG,"receiver is not null");
			soundID = mSoundPool.load(getApplicationContext(),R.raw.test1,1);
			mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
					// TODO Auto-generated method stub
					mSoundPool.play(soundID, 1, 1, 1, -1, 1);
				}
			});
			LogRuningTest.printDebug(TAG, "receiver---------->soundID" + soundID, this);
		}
		//进行副麦录音
		inRecord(MediaRecorder.AudioSource.DEFAULT);
		mHandler.sendEmptyMessageDelayed(13, MIC_TIME);
	}
	//停止听筒播放和副麦录音
	private void stopReceiverPlayAndAuxiliaryMicRecord(){
		LogRuningTest.printDebug(TAG, "stopReceiverPlayAndAuxiliaryMicRecord()", this);
		if (mSoundPool != null) {
	         mSoundPool.stop(0);
	         mSoundPool.release();
	         LogRuningTest.printDebug(TAG, "stopReceiverPlayAndAuxiliaryMicRecord() SoundPool stop() release()", this);
	    }
		if (isRecording && mMediaRecorder != null) {
           mMediaRecorder.stop();
           mMediaRecorder.release();
           mMediaRecorder = null;
           LogRuningTest.printDebug(TAG, "stopReceiverPlayAndAuxiliaryMicRecord() MediaRecorder stop() release() ",this);
       }
		audioTestTv.setText(getString(R.string.end_receiver_play_auxiliarymic_record));
		mHandler.sendEmptyMessageDelayed(14, END_TIME);
	}
	
	//副麦录音文件播放
	private void AuxiliaryMicRecordPlay(){
		LogRuningTest.printDebug(TAG, "AuxiliaryMicRecordPlay()", this);
		audioTestTv.setText(getString(R.string.auxiliarymic_file_play));
		inPlay();
		mHandler.sendEmptyMessageDelayed(15, MIC_TIME);
	}
	//副麦录音文件播放结束
	private void stopAuxiliaryMicRecordPlay(){
		LogRuningTest.printDebug(TAG, "stopAuxiliaryMicRecordPlay()", this);
		audioTestTv.setText(getString(R.string.end_auxiliarymic_file_play));
		mHandler.sendEmptyMessageDelayed(16,END_TIME);
	}

	//清空振幅集合
	private void clearArrayList(){
		mMaxAmplitudeList.clear();
		mGetAmplitudeCount = 0;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LogRuningTest.printDebug(TAG, "onCreate start AudioTestActivity", this);
		audioTestActivity = new AudioTestActivity();
		audioTestActivity.isMonkeyRunning(TAG, "onCreate", this);
		setContentView(R.layout.audio_test);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
		mAudioManager = (AudioManager) getApplicationContext()
				.getSystemService("audio");
		audioTestTv = (TextView) findViewById(R.id.audio_test_tv);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		audioTestActivity.isMonkeyRunning(TAG, "onResume", this);
		speakerAndMainMicRecord();
	}
	

	/**
	 * goToVedioTest
	 */
	private void goToVedioTest(){
		Editor editor=mSharedPreferences.edit();
		if(!mTestSuccess){
			editor.putBoolean("audio_mic_test", mTestSuccess);
		}
		editor.commit();
		Intent intent = new Intent(TestService.ACTION_VEDIO_TEST);
		sendBroadcast(intent);
	}
	
	
	
	/**
	 * 进行播放
	 */
	public void inPlay(){
		LogRuningTest.printDebug(TAG, "inPlay()", this);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		try {
			replay();
		} catch (Exception e){
			// TODO Auto-generated catch block
			mTestSuccess = false;
		    loge(e);
		    LogRuningTest.printDebug(TAG,e+"",this);
		}
	}
	/**
	 * 播放
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private void replay() throws IllegalArgumentException, IllegalStateException,
    				  IOException{
		File file = new File(mAudioFilePath);
		FileInputStream mFileInputStream = new FileInputStream(file);
		if(mMediaPlayer != null){
			mMediaPlayer.reset();
			Log.d(TAG,"replay() mMediaPlayer != null");
		}else{
			mMediaPlayer = new MediaPlayer();
            Log.d(TAG, "replay()-----mMediaPlayer == null");
		}
		
		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer  player, int arg1, int arg2) {
				// TODO Auto-generated method stub
				if(player != null && player.isPlaying()){
					LogRuningTest.printDebug(TAG, "replay() ------->"
                           + "onError====mMediaPlayer", AudioTestActivity.this);
                    player.stop();
                    LogRuningTest.printDebug(TAG, "replay() ------->"
                                   + "onError====mMediaPlayer---stop()", AudioTestActivity.this);
                    player.release();
                    LogRuningTest.printDebug(TAG, "replay() ------->"
                                   + "onError====mMediaPlayer---release()", AudioTestActivity.this);
                    player = null;
				}
				LogRuningTest.printDebug(TAG, "replay() ------->"
                   + "------->" + "onError", AudioTestActivity.this);
				mTestSuccess = false;
				return false;
			}
		});
		
		mMediaPlayer.setDataSource(mFileInputStream.getFD());
        mMediaPlayer.prepare();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LogRuningTest.printDebug(TAG, "replay() MediaPlayer------->"
                                           + "onPrepared",AudioTestActivity.this);
                mp.start();
                LogRuningTest.printDebug(TAG, "replay() MediaPlayer------->"
                                               + "start",AudioTestActivity.this);
            }
        });
        
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {

               LogRuningTest.printDebug(TAG, "replay() MediaPlayer------->"
                                      + "onCompletion",AudioTestActivity.this);
                mPlayer.stop();
                LogRuningTest.printDebug(TAG, "replay() MediaPlayer------->"
                                  + "onCompletion===stop()",AudioTestActivity.this);
                File file = new File(mAudioFilePath);
                file.delete();
                LogRuningTest.printDebug(TAG, "replay() Delete audioFile------->"
                                                              ,AudioTestActivity.this);
            }
        });
	}
	/***
	 * 进行录音
	 * @param audioSource
	 */
	public void inRecord(int audioSource) {
		LogRuningTest.printDebug(TAG, "inRecord()", this);
		try {
			if(Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)){
				LogRuningTest.printDebug(TAG, "Environment.getExternalStorageState():"
						+Environment.getExternalStorageState(),this);
			}
			record(audioSource);
			isRecording = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mTestSuccess = false;
			LogRuningTest.printDebug(TAG, "inRecord() Exception:"+e, this);
			loge(e);
		}
     }
	/**
	 * 录音
	 * @param audioSource
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void record(int audioSource) throws IllegalStateException, IOException,
	                  InterruptedException{
			if(mMediaRecorder != null){
				Log.d(TAG, "mMediaRecorder != null");
				mMediaRecorder.reset();
			}else{
				mMediaRecorder = new MediaRecorder();
			}
			
			 mMediaRecorder.setAudioSource(audioSource);
		     mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		     mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		     mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
				
				@Override
				public void onError(MediaRecorder mediarecorder, int arg1, int arg2) {
					// TODO Auto-generated method stub
					LogRuningTest.printDebug(TAG, "record() mMediaRecorder is onError", AudioTestActivity.this);
					if(mediarecorder != null && isRecording){
						 Log.d(TAG, "mMediaRecorder is xxxxxxx");
						 mediarecorder.stop();
		                 mediarecorder.release();
		                 LogRuningTest.printDebug(TAG, "record() mMediaRecorder stop() release()", AudioTestActivity.this);
		                 mediarecorder = null;
		                 mTestSuccess = false;
					}
				}
			});
		    mMediaRecorder.setOutputFile("sdcard/testHeadset.amr");
		    mAudioFilePath = "sdcard/testHeadset.amr";
		    LogRuningTest.printDebug(TAG, "path :"
		            +Environment.getExternalStorageDirectory().toString(), AudioTestActivity.this);
		    mMediaRecorder.prepare();
		    mMediaRecorder.start();
		    
		    mGetmaxAmplitude.sendEmptyMessageDelayed(MIC_LOOPBACK, 1000);
	}
	
	
	/**
	 * 录音时每隔一秒，获取最大振幅
	 */
	   Handler mGetmaxAmplitude = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MIC_LOOPBACK:
	                Log.d(TAG, "MIC_LOOPBACK");
	                int amplitude = mMediaRecorder.getMaxAmplitude();
	                Log.d(TAG, "amplitude    " + amplitude);
	                mMaxAmplitudeList.add(amplitude);
	                mGetAmplitudeCount++;
	                if (mGetAmplitudeCount >= GET_AMPLITUDE_TIMES) {
	                    calculateAmplitudeAvg();
	                    break;
	                }
	                mGetmaxAmplitude.sendEmptyMessageDelayed(MIC_LOOPBACK, 1000);
	                break;
	            }
	            super.handleMessage(msg);
	        }
	    };
	   /**
	    * 计算振幅平均值 
	    */
	    private void calculateAmplitudeAvg() {
	        int sum = 0;
	        int size = mMaxAmplitudeList.size();
	        for (int i = 0; i < size; i++) {
	            sum += mMaxAmplitudeList.get(i);
	        }
	        LogRuningTest.printDebug(TAG, "mMaxAmplitudeList :" + mMaxAmplitudeList, this);
			if(size <= 1){
				mTestSuccess = false;
			}else{
				if(sum/(size-1) < 100){
					LogRuningTest.printDebug(TAG, "sum/(size-1) : " + (sum/(size - 1)), this);
					mTestSuccess = false;
				}else {
					mTestSuccess = true;
				}
			}
	    }
	

	
	
	private void loge(Object e){
		if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        LogRuningTest.printDebug(TAG, e + "",this);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
		
		if (mAudioManager != null) {
	         mAudioManager.setMode(AudioManager.MODE_NORMAL);
	    }
		
		if (mSoundPool != null) {
	         mSoundPool.stop(0);
	         mSoundPool.release();
	    }
		
		if (isRecording && mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Log.d(TAG, "------onStop----1-----");
        }

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
                /* < 0068265 xuyinwen 20150819 begin */
				LogRuningTest.printInfo(TAG, "User click KEYCODE_BACK. Ignore it.", this);
				return false;
			default:
				break;
                /* 0068265 xuyinwen 20150819 end > */
		}
		return super.onKeyDown(keyCode, event);
	}
}
