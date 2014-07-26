package com.auscultator.app;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.auscultator.audio.AudioRecorder;
import com.auscultator.data.DataAdapter;

public class Auscultation extends Activity {
    private final static String TAG = "AUSCULTATION";
    private final static int ST_READY = 0;
    private final static int ST_RECORDING = 1;
    private final static int ST_RECORDED = 2;
    private final static int ST_PLAYING = 4;

    private ImageView btn_recorder;
    private Button btn_save;
    private Button btn_cancel;

    private AudioRecorder audioRecorder;
    private DataAdapter dataAdapter;

    private int record_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create Auscultation View");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auscultation);
        /* Get the view in activity */
        btn_recorder = (ImageView) findViewById(R.id.btn_recorder);
        btn_save = (Button) findViewById(R.id.auscult_save);
        btn_cancel = (Button) findViewById(R.id.auscult_cancle);
        /* Set view's listeners */
        btn_recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "record button");
                // If the recorder is ready, start recording the audio.
                if (record_state == ST_READY) {
                    int res = audioRecorder.startRecording();
                    if (res != ErrorCode.SUCCESS) {
                        Toast.makeText(getApplicationContext(), ErrorCode.get_error_msg(res), Toast.LENGTH_SHORT);
                        Log.e(TAG, ErrorCode.get_error_msg(res));
                        return;
                    }
                    record_state = ST_RECORDING;
                    btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.stop));

                    return;
                }
                // if the recorder is recording, stop recording.
                else if (record_state == ST_RECORDING) {
                    int res = audioRecorder.stopRecording();
                    if (res != ErrorCode.SUCCESS) {
                    	Toast.makeText(getApplicationContext(), ErrorCode.get_error_msg(res), Toast.LENGTH_SHORT);
                        Log.e(TAG, ErrorCode.get_error_msg(res));
                        return;
                    }
                    record_state = ST_RECORDED;
                    btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.play));

                    return;
                }
                // if the recording is completed, play the audio.
                else if (record_state == ST_RECORDED) {
                    // play the recorded audio;
                    int res = audioRecorder.play_recored_audio(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            record_state = ST_RECORDED;
                            btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.play));
                            audioRecorder.stop_play();
                        }
                    });
                    if (res != ErrorCode.SUCCESS) {
                    	Toast.makeText(getApplicationContext(), ErrorCode.get_error_msg(res), Toast.LENGTH_SHORT);
                        Log.e(TAG, ErrorCode.get_error_msg(res));
                        return;
                    }

                    // change the status
                    record_state = ST_PLAYING;
                    btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.stop));

                    return;
                }
                // if the audio is playing, stop the playing,
                else if (record_state == ST_PLAYING) {
                    int res = audioRecorder.stop_play();
                    if (res != ErrorCode.SUCCESS) {
                    	Toast.makeText(getApplicationContext(), ErrorCode.get_error_msg(res), Toast.LENGTH_SHORT);
                        Log.e(TAG, ErrorCode.get_error_msg(res));
                        return;
                    }

                    record_state = ST_RECORDED;
                    btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.play));

                    return;
                }

                return;
            }
        });
        /**
         * The onClick listener for saving button.
         */
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Dialog dlg = new AlertDialog.Builder(Auscultation.this)
            		.setTitle(R.string.title_dlg_save_sounds)
            		.setMessage(R.string.content_dlg_save_sounds)
            		.setPositiveButton(R.string.heart_sounds, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// save the sounds to heart sounds records
							String sound_file = audioRecorder.save(AudioRecorder.HEART_SOUNDS);
							// open the medical records activity
							Intent intent = new Intent();
							intent.setClass(Auscultation.this, MedicalRecords.class);
							intent.putExtra("sound_type", audioRecorder.HEART_SOUNDS);
							intent.putExtra("sound_file", sound_file);
							Auscultation.this.startActivity(intent);
			                // restore the view's status. 
			                btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.record));
			                record_state = ST_READY;
						}
					})
					.setNegativeButton(R.string.breath_sounds, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// save the sounds to breath sounds records
							String sound_file = audioRecorder.save(AudioRecorder.BREATH_SOUNDS);
							// open the medical records activity
							Intent intent = new Intent();
							intent.setClass(Auscultation.this, MedicalRecords.class);
							intent.putExtra("sounds_type", audioRecorder.HEART_SOUNDS);
							intent.putExtra("sound_file", sound_file);
							Auscultation.this.startActivity(intent);
			                // restore the view's status. 
			                btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.record));
			                record_state = ST_READY;
						}
					}).create();
            	
            		dlg.show();
                
            }
        });
        /**
         * The onClick listener of cancel button.
         */
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (record_state == ST_READY) {
            		Auscultation.this.finish();
            		return;
            	}
                Dialog dlg = new AlertDialog.Builder(Auscultation.this)
                        .setTitle(R.string.title_cancel_medical_record)
                        .setMessage(R.string.cancel_medical_record)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // confirm cancel this auscultation, clear the cache file.
                                audioRecorder.reset();
                                btn_recorder.setImageDrawable(getResources().getDrawable(R.drawable.record));
                                record_state = ST_READY;
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        }).create();
                dlg.show();
            }
        });

        /*Initialize the instance*/
        audioRecorder = AudioRecorder.getInstance();
        record_state = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();


    }
}
