package com.example.flo

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
    lateinit var lyricsLayout: RelativeLayout
    lateinit var lyricsCardView: CardView
    lateinit var albumImgCardView: CardView
    lateinit var lyricsCloseButton: ImageView
    lateinit var lyricsToggle: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        mContext = this
        var isMovable = false

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
        lyricsLayout = findViewById(R.id.lyrics_layout)
        albumImgCardView = findViewById(R.id.album_img_card_view)
        lyricsCardView = findViewById(R.id.lyrics_card_view)
        lyricsCloseButton = findViewById(R.id.lyrics_close_button)
        lyricsToggle = findViewById(R.id.lyrics_toggle)

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
            var tempSeekParams: Int? = null
            override fun onSeeking(seekParams: SeekParams) {
                //                Log.i(TAG, seekParams.seekBar.toString())
                //                Log.i(TAG, seekParams.progress.toString())
                //                Log.i(TAG, seekParams.progressFloat.toString())
                //                Log.i(TAG, seekParams.fromUser.toString())

                if (seekParams.fromUser) {
                    playingTime.text = mSec(seekParams.progress.toLong())
                    tempSeekParams = seekParams.progress
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
                // TODO seekBar를 움직이는 동안 디자인을 바꿨다가
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                // TODO seekBar 움직임이 끝나면 원상복귀해도 괜찮을듯
                mediaPlayer!!.seekTo(tempSeekParams!!)
            }
        }




        playBtn.setOnClickListener {
            playSong()
        }

        pauseBtn.setOnClickListener {
            pauseSong()
        }

        lyricsLayout.setOnClickListener {
            Log.d("albumImgCardView", albumImgCardView.visibility.toString())
            if (albumImgCardView.visibility == 0) { // VISIBLE
                albumImgCardView.visibility = GONE
                lyricsCloseButton.visibility = VISIBLE
                lyricsToggle.visibility = VISIBLE
            } else if (albumImgCardView.visibility == 8) { // GONE
                if (!isMovable) {
                    albumImgCardView.visibility = VISIBLE
                    lyricsCloseButton.visibility = INVISIBLE
                    lyricsToggle.visibility = INVISIBLE
                    lyrics.movementMethod = null
                }
                else {
                    lyrics.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                }
            }
        }

        lyricsCloseButton.setOnClickListener {
            lyrics.movementMethod = null
            albumImgCardView.visibility = VISIBLE
            lyricsCloseButton.visibility = INVISIBLE
            lyricsToggle.visibility = INVISIBLE
        }

        lyricsToggle.setOnClickListener {
            isMovable = !isMovable
            if (isMovable) {
                lyrics.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                lyricsToggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                lyrics.movementMethod = null
                lyricsToggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.colorDefault),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
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
                            if (data != null) {
                                songUrl = data.file
                                songSinger.text = data!!.singer
                                songTitle.text = data!!.title
                                songAlbum.text = data!!.album
                                lyrics.text = data!!.lyrics
                                Glide.with(mContext).load(data!!.image).into(albumImg)
                            }

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


