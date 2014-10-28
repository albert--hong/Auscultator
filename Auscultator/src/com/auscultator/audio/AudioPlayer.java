package com.auscultator.audio;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class AudioPlayer {
    static private AudioPlayer instance;
    private int ST_PLAYING = 0x00010001;
    private int ST_PAUSE = 0x00010002;
    private int ST_STOP = 0x00010003;
    private int ST_READY = 0x00010000;

	private MediaPlayer audio_player;
    private boolean isPauseing = false;

	private AudioPlayer() {
		this.audio_player = new MediaPlayer();
	}

    static public AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

	public void play(String file, MediaPlayer.OnCompletionListener listener) {
		if (this.audio_player.isPlaying()) {
			this.audio_player.stop();
		}
		this.audio_player.reset();
		try {
			audio_player.setOnCompletionListener(listener);
			audio_player.setDataSource(file);
			audio_player.prepare();
			audio_player.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            this.isPauseing = false;
        }
    }

    public void play(AssetFileDescriptor fd, MediaPlayer.OnCompletionListener listener) {
        if (this.audio_player.isPlaying()) {
            this.audio_player.stop();
        }
        this.audio_player.reset();
        try {
            audio_player.setOnCompletionListener(listener);
            audio_player.setDataSource(fd.getFileDescriptor(),
                    fd.getStartOffset(), fd.getLength());
            audio_player.prepare();
            audio_player.start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
          this.isPauseing = false;
        }
    }

	public void pause() {
        if (this.audio_player.isPlaying()) {
            this.audio_player.pause();
            this.isPauseing = true;
        }
    }

    public void playOrPause() {
        if (this.audio_player.isPlaying()) {
            this.audio_player.pause();
        } else {
            this.audio_player.start();
        }
    }

	public void reset() {
        this.audio_player.reset();
	}
	
	public boolean isPlaying() {
		return this.audio_player.isPlaying();
	}

}
