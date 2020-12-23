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
import kotlinx.android.synthetic.main.activity_play.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlayActivity : AppCompatActivity() {

    lateinit var mContext: Context
    lateinit var songUrl: String
    var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        mContext = this
        var isMovable = false

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }


        // 쓰레드로 seekBar이동
        class MyThread : Thread() {
            override fun run() {
                while (mediaPlayer!!.isPlaying) {
                    SystemClock.sleep(500)
                    runOnUiThread {
                        seek_bar.setProgress((mediaPlayer!!.currentPosition).toFloat())
                        playing_time.text = mSec(mediaPlayer!!.currentPosition.toLong())
                    }
                }
            }
        }


        fun playSong() {
            play_btn.visibility = GONE
            pause_btn.visibility = VISIBLE

            // 노래 재생
            mediaPlayer!!.start()
            MyThread().start()
        }

        fun pauseSong() {
            pause_btn.visibility = GONE
            play_btn.visibility = VISIBLE

            // 노래 일시정지
            mediaPlayer!!.pause()
            MyThread().interrupt()
        }

        mediaPlayer!!.setOnCompletionListener {
            pauseSong()
        }


        seek_bar.onSeekChangeListener = object : OnSeekChangeListener {
            //            val TAG = "seekBar_LOG"
            var tempSeekParams: Int? = null
            override fun onSeeking(seekParams: SeekParams) {
                //                Log.i(TAG, seekParams.seekBar.toString())
                //                Log.i(TAG, seekParams.progress.toString())
                //                Log.i(TAG, seekParams.progressFloat.toString())
                //                Log.i(TAG, seekParams.fromUser.toString())

                if (seekParams.fromUser) {
                    playing_time.text = mSec(seekParams.progress.toLong())
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


        play_btn.setOnClickListener {
            playSong()
        }

        pause_btn.setOnClickListener {
            pauseSong()
        }

        lyrics_layout.setOnClickListener {
            Log.d("albumImgCardView", album_img_card_view.visibility.toString())
            if (album_img_card_view.visibility == 0) { // VISIBLE
                album_img_card_view.visibility = GONE
                lyrics_close_button.visibility = VISIBLE
                lyrics_toggle.visibility = VISIBLE
            } else if (album_img_card_view.visibility == 8) { // GONE
                if (!isMovable) {
                    album_img_card_view.visibility = VISIBLE
                    lyrics_close_button.visibility = INVISIBLE
                    lyrics_toggle.visibility = INVISIBLE
                    lyrics_text_view.movementMethod = null
                } else {
                    lyrics_text_view.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                }
            }
        }

        lyrics_close_button.setOnClickListener {
            lyrics_text_view.movementMethod = null
            album_img_card_view.visibility = VISIBLE
            lyrics_close_button.visibility = INVISIBLE
            lyrics_toggle.visibility = INVISIBLE
        }

        lyrics_toggle.setOnClickListener {
            isMovable = !isMovable
            if (isMovable) {
                lyrics_text_view.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                lyrics_toggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                lyrics_text_view.movementMethod = null
                lyrics_toggle.setColorFilter(
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
                                song_singer.text = data!!.singer
                                song_title.text = data!!.title
                                song_album.text = data!!.album
                                lyrics_text_view.text = data!!.lyrics
                                Glide.with(mContext).load(data!!.image).into(album_img)
                            }

                            mediaPlayer!!.apply {
                                setDataSource(songUrl)
                                prepare()
                                playSong()
                            }

                            seek_bar.max = (mediaPlayer!!.duration).toFloat()
                            var mSec: Long = (mediaPlayer!!.duration).toLong()
                            song_time.text = mSec(mSec)
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


