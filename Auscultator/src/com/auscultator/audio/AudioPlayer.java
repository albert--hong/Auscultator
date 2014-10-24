package com.auscultator.audio;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class AudioPlayer {
    static private AudioPlayer instance;
	private MediaPlayer audio_player;
	
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
        }
    }
	
	public void stop() {
		if (this.audio_player.isPlaying()) {
			this.audio_player.stop();
		}
	}
	
	public boolean isPlaying() {
		return this.audio_player.isPlaying();
	}

}
