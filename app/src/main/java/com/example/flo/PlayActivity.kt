package com.example.flo

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlayActivity : AppCompatActivity() {

    lateinit var mContext: Context
    lateinit var songUrl: String
    var mediaPlayer: MediaPlayer? = null

    lateinit var songSinger: TextView
    lateinit var songAlbum: TextView
    lateinit var songTitle: TextView
    lateinit var albumImg: ImageView
    lateinit var lyrics: TextView
    lateinit var playBtn: ImageView
    lateinit var pauseBtn: ImageView
    lateinit var seekBar: IndicatorSeekBar
    lateinit var playingTime: TextView
    lateinit var songTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        mContext = this

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        songSinger = findViewById(R.id.song_singer)
        songAlbum = findViewById(R.id.song_album)
        songTitle = findViewById(R.id.song_title)
        albumImg = findViewById(R.id.album_img)
        lyrics = findViewById(R.id.lyrics_text_view)
        playBtn = findViewById(R.id.play_btn)
        pauseBtn = findViewById(R.id.pause_btn)
        seekBar = findViewById(R.id.seek_bar)
        playingTime = findViewById(R.id.playing_time)
        songTime = findViewById(R.id.song_time)


        lyrics.movementMethod = ScrollingMovementMethod() // 가사 스크롤

        fun mSec(mSec: Long): String {
            val hours: Long = mSec / 1000 / 60 / 60 % 24
            val minutes: Long = mSec / 1000 / 60 % 60
            val seconds: Long = mSec / 1000 % 60

            return if (mSec < 3600000) {
                String.format("%02d:%02d", minutes, seconds)
            } else {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }


        // 쓰레드로 seekBar이동
        class MyThread : Thread() {
            override fun run() {
                while (mediaPlayer!!.isPlaying) {
                    SystemClock.sleep(500)
                    runOnUiThread {
                        seekBar.setProgress((mediaPlayer!!.currentPosition).toFloat())
                        playingTime.text = mSec(mediaPlayer!!.currentPosition.toLong())
                    }
                }
            }
        }


        fun playSong() {
            playBtn.visibility = GONE
            pauseBtn.visibility = VISIBLE

            // 노래 재생
            mediaPlayer!!.start()
            MyThread().start()
        }

        fun pauseSong() {
            pauseBtn.visibility = GONE
            playBtn.visibility = VISIBLE

            // 노래 일시정지
            mediaPlayer!!.pause()
            MyThread().interrupt()
        }

        mediaPlayer!!.setOnCompletionListener {
            pauseSong()
        }


        seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            //            val TAG = "seekBar_LOG"
            override fun onSeeking(seekParams: SeekParams) {
                //                Log.i(TAG, seekParams.seekBar.toString())
                //                Log.i(TAG, seekParams.progress.toString())
                //                Log.i(TAG, seekParams.progressFloat.toString())
                //                Log.i(TAG, seekParams.fromUser.toString())

                if (seekParams.fromUser) {
                    mediaPlayer!!.seekTo(seekParams.progress)
                    playingTime.text = mSec(seekParams.progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
                mediaPlayer!!.pause()
            }
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                mediaPlayer!!.start()
            }
        }




        playBtn.setOnClickListener {
            playSong()
        }

        pauseBtn.setOnClickListener {
            pauseSong()
        }



        Retrofit2.SongService.requestSong()
            .enqueue(object : Callback<Song> {
                override fun onFailure(call: Call<Song>, t: Throwable) {
                    Log.d("retrofit2 :: ", "연결실패 $t")
                }

                override fun onResponse(
                    call: Call<Song>,
                    response: Response<Song>
                ) {
                    var data: Song? = response?.body()
                    Log.d("retrofit2 ::", response.code().toString() + response.body().toString())
                    when (response.code()) {
                        200 -> {
                            songUrl = data!!.file
                            songSinger.text = data!!.singer
                            songTitle.text = data!!.title
                            songAlbum.text = data!!.album
                            lyrics.text = data!!.lyrics
                            Glide.with(mContext).load(data!!.image).into(albumImg)

                            mediaPlayer!!.apply {
                                setDataSource(songUrl)
                                prepare()
                                playSong()
                            }

                            seekBar.max = (mediaPlayer!!.duration).toFloat()
                            var mSec: Long = (mediaPlayer!!.duration).toLong()
                            songTime.text = mSec(mSec)
                        }
                    }
                }
            }
            )


    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}


