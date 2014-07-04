package com.auscultator.audio;

import android.os.Environment;

import com.auscultator.app.ErrorCode;

import java.io.File;
import java.io.IOException;

/**
 * Created by HongYan on 2014/6/25.
 */
public class AudioFileManager {
    private static AudioFileManager mInstance;

    private static String TMP_DIR_PATH = "/Ausculation/tmp/";
    private static String AUDIO_DIR_PATH = "/Ausculation/audio/";
    private File tmp_dir;
    private File audio_dir;

    private AudioFileManager() {
    }
    public synchronized static AudioFileManager getInstance() {
        if (mInstance == null) {
            mInstance = new AudioFileManager();
        }
        return mInstance;
    }

    /**
     * If the SD card exists in target phone.
     *
     * @return boolean
     */
    public boolean is_sdcard_exist() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * Initialize the directory for audio files.
     * @return
     */
    public int initialize() {
        int res = ErrorCode.SUCCESS;
        if (this.is_sdcard_exist()) {
            String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
            String tmp_dir_path = sdcard_path + this.TMP_DIR_PATH;
            String audio_dir_path = sdcard_path + this.AUDIO_DIR_PATH;
            this.tmp_dir = new File(tmp_dir_path);
            this.audio_dir = new File(audio_dir_path);
            if (!this.tmp_dir.exists()) {
                if (!this.tmp_dir.mkdirs()) {
                    res = ErrorCode.ERROR_CREATE_FILE;
                }
            }
            if (!this.audio_dir.exists()) {
                if (!this.audio_dir.mkdirs()) {
                    res = ErrorCode.ERROR_CREATE_FILE;
                }
            }
        } else {
           res = ErrorCode.ERR_NOSDCARD;
        }

        return res;
    }

    /**
     * Create a cache file in temp directory.
     *
     * @return File
     */
    public File[] create_cache_file() throws IOException {
        File files[] = new File[2];
//        files[0] = new File("rec_temp.dat");
//        files[1] = new File("rec_temp.wav");
        files[0] = File.createTempFile("rec", ".dat", this.tmp_dir);
        files[1] = File.createTempFile("rec", ".wav", this.tmp_dir);
        return files;
    }
//    public
}
