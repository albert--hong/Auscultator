package com.auscultator.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.auscultator.app.ErrorCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The Audio Recorder class for recording the auscultation sounds.
 * 
 * Created by HongYan on 2014/6/24.
 */
public class AudioRecorder {
	private final String TAG = "AudioRecorder";
	private static AudioRecorder mInstance;

	public final static int BREATH_SOUNDS = 0x10;
	public final static int HEART_SOUNDS = 0x12;

	private final static int ST_NOREADY = -1; // The STATE of audio record
												// device have no ready.
	private final static int ST_READY = 0; // The STATE of audio record device
											// ready.
	private final static int ST_RECORDING = 1; // The STATE of recording audio.
	private final static int ST_RECORDED = 2; // The STATE of audio recorded.
	private final static int ST_PLAYING = 4; // The STATE of playing recorded
												// audio.
	private final static int ST_STOPPED = 5; // The STATE of stop playing
												// recorded audio

	/**
	 * The buffer size for audio file.
	 */
	private int bufferSizeInBytes;

	/**
	 * The reference for audio file manager.
	 */
	private AudioFileManager file_mgr;
	/**
	 * a data file for audio data.
	 */
	private File cache_data_file = null;
	/**
	 * a cache audio file for recording.
	 */
	private File cache_audio_file = null;
	/**
	 * The reference for Audio Recorder API
	 */
	private AudioRecord audio_record;
	/**
	 * The reference of Audio Player
	 */
	private MediaPlayer audio_player;
	/**
	 * The status for the audio recorder.
	 * 
	 * 0 initialized and the cache file prepared 1 recording 2 record complete
	 * and cache the file.
	 */
	private int status;

	private AudioRecorder() {
		this.status = this.ST_NOREADY;
		// prepare for the File Manager
		this.file_mgr = AudioFileManager.getInstance();
		// prepare for the play device
		this.audio_player = new MediaPlayer();
	}

	public synchronized static AudioRecorder getInstance() {
		if (mInstance == null) {
			mInstance = new AudioRecorder();
		}
		return mInstance;
	}

	/**
	 * Initialize the Audio Recorder. 1. initialize the cache files.
	 * 
	 * @return
	 */
	public int initialize() {
		int res = ErrorCode.SUCCESS;
		switch (status) {
		case ST_READY:
			// no thing
			break;
		case ST_RECORDING:
			res = ErrorCode.ERR_STATE_RECODING;
			break;
		case ST_NOREADY:
			res = file_mgr.initialize();
			this.status = ST_READY;
			break;
		}
		return res;
	}

	/**
	 * Reset the Audio Recorder.
	 * 
	 * @return {int} error_code
	 */
	public int reset() {
		int res = ErrorCode.SUCCESS;
		switch (status) {
		case ST_RECORDING:
			return ErrorCode.ERR_STATE_RECODING;
		case ST_PLAYING:
			return ErrorCode.ERR_STATE_PLAYING;
		default:
			this.status = ST_READY;
			break;
		}
		return res;
	}

	/**
	 * Start recording.
	 * 
	 * @return error_code
	 */
	public int startRecording() {
		int res = ErrorCode.SUCCESS;
		// If the audio file or media recorder not prepare, cannot start
		// recording.
		if (this.status != 0) {
			if ((res = initialize()) != ErrorCode.SUCCESS) {
				return res;
			}
		}
		try {
			// prepare for the record device
			this.audio_record = this.get_audio_recorder();
			// create cache files.
			File files[] = file_mgr.create_cache_file();
			if (files.length != 2) {
				return ErrorCode.ERROR_CREATE_FILE;
			}
			this.cache_data_file = files[0];
			this.cache_audio_file = files[1];
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCode.ERROR_CREATE_FILE;
		}
		// starting the recording
		if (this.audio_player == null) {
			return ErrorCode.ERR_RECORD_DEVICE;
		}
		if (this.audio_record.getState() == this.audio_record.STATE_INITIALIZED) {
			this.audio_record.startRecording();
			AudioRecordThread thread = new AudioRecordThread();
			new Thread(thread).start();
			this.status = this.ST_RECORDING;
		} else {
			res = ErrorCode.ERR_STATE_RECODING;
		}
		// change the status
		return res;
	};

	/**
	 * Get a proper audio recorder for device
	 * 
	 * @return AudioRecord andio recorder.
	 */
	private AudioRecord get_audio_recorder() {
		int buf_size;
		int sample_rate_list[] = new int[] { 44100, 22050, 16000, 11025, 8000 };
		int encoding_list[] = new int[] { AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.ENCODING_PCM_8BIT };
		int i, j, sample_rate, encoding;
		for (i = 0; i < sample_rate_list.length; i++) {
			sample_rate = sample_rate_list[i];
			for (j = 0; j < encoding_list.length; j++) {
				encoding = encoding_list[j];
				buf_size = AudioRecord.getMinBufferSize(sample_rate,
						AudioFormat.CHANNEL_IN_MONO, encoding);
				if (buf_size != AudioRecord.ERROR_BAD_VALUE) {
					AudioRecord audio_recorder = new AudioRecord(
							MediaRecorder.AudioSource.MIC, sample_rate,
							AudioFormat.CHANNEL_IN_MONO, encoding, buf_size);
					if (audio_recorder.getState() == audio_recorder.STATE_INITIALIZED) {
						this.bufferSizeInBytes = buf_size;
						return audio_recorder;
					} else {
						audio_recorder.release();
					}
				}

			}
		}
		return null;
	};

	/**
	 * Stop the recording.
	 * 
	 * @return error_code
	 */
	public int stopRecording() {
		if (this.audio_record != null) {
			this.status = this.ST_RECORDED;
			this.audio_record.stop();
			this.audio_record.release();
			return ErrorCode.SUCCESS;
		} else {
			return ErrorCode.ERR_UNKOWN;
		}

	};

	/**
	 * Play the audio just recorded.
	 * 
	 * @return int error_code
	 */
	public int play_recored_audio(MediaPlayer.OnCompletionListener listener) {
		int res = ErrorCode.SUCCESS;
		Log.d(TAG, "The status of the AudioRecorder = " + this.status);
		if (this.status != this.ST_RECORDED) {
			return ErrorCode.ERR_STATE_PLAYING;

		}

		if (this.audio_player == null) {
			return ErrorCode.ERR_RECORD_DEVICE;
		}

		try {
			this.audio_player.setOnCompletionListener(listener);
			this.audio_player.setDataSource(this.cache_audio_file.getPath());
			this.audio_player.prepare();
			this.audio_player.start();

			this.status = this.ST_PLAYING;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * Stop playing the recored audio
	 * 
	 * @return int error_code
	 */
	public int stop_play() {
		int res = ErrorCode.SUCCESS;

		if (this.audio_player == null) {
			return ErrorCode.ERR_PLAY_DEVICE;
		}

		if (this.status != this.ST_PLAYING) {
			return ErrorCode.ERR_STATE_PLAYING;
		}

		if (this.audio_player.isPlaying()) {
			this.audio_player.stop();
		}
		this.audio_player.reset();
		this.status = this.ST_RECORDED;

		return res;
	}

	/**
	 * Save the cache sounds to the audio's directory. 
	 * 
	 * @param type
	 * @return
	 */
	public String save(int type) {
		// save sounds file and remove the cache file.
		String path = "";
		if (type == this.BREATH_SOUNDS) {
			path = "/breath_";
		} else if (type == this.HEART_SOUNDS) {
			path = "/heart_";
		} else {
			return path;
		}
		File dir = file_mgr.get_sounds_dir();
		path = dir.getAbsolutePath() + path;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmm", Locale.ENGLISH);
		Date date = new Date();
		path += dateFormat.format(date);
		
		File file = new File(path);
		this.cache_audio_file.renameTo(file);
		return path;
	}

	public boolean clear() {
		return false;
	}

	class AudioRecordThread implements Runnable {

		@Override
		public void run() {
			writeAudio2File();
			copyWaveFile();
		}
	}

	/**
	 * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
	 * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
	 * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
	 */
	private void writeAudio2File() {
		byte[] audio_data = new byte[this.bufferSizeInBytes];
		FileOutputStream fos = null;
		int read_size = 0;

		try {
			// get the file reader of cache data
			fos = new FileOutputStream(this.cache_data_file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (this.status == this.ST_RECORDING) {
			// read the cache data
			read_size = this.audio_record
					.read(audio_data, 0, bufferSizeInBytes);
			if (AudioRecord.ERROR_INVALID_OPERATION != read_size && fos != null) {
				try {
					// write the cache data to data file.
					fos.write(audio_data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			if (fos != null)
				fos.close();// 关闭写入流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这里得到可播放的音频文件
	 */
	private void copyWaveFile() {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = this.audio_record.getSampleRate();
		int channels = this.audio_record.getChannelCount();
		int encoding_rate = this.audio_record.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT ? 8
				: 16;
		long byteRate = 16 * longSampleRate * channels / encoding_rate;
		byte[] data = new byte[bufferSizeInBytes];

		try {
			in = new FileInputStream(this.cache_data_file);
			out = new FileOutputStream(this.cache_audio_file);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
			this.cache_data_file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 自己特有的头文件。
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
}
