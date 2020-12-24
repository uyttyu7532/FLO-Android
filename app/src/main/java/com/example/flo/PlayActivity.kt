package com.example.flo

import LyricsAdapter
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flo.databinding.ActivityPlayBinding
import com.example.flo.viewmodel.Lyric
import com.example.flo.viewmodel.Song
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.activity_play.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlayActivity : AppCompatActivity() {

    var lyricList: MutableList<Lyric> = mutableListOf()
    lateinit var mContext: Context
    lateinit var songUrl: String
    var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityPlayBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_play)


        mContext = this
        var isMovable = false

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        val adapter = LyricsAdapter(mContext, lyricList)



        lyrics_recycler_view.layoutManager = LinearLayoutManager(mContext)
        adapter.itemClick = object : LyricsAdapter.ItemClick {
            override fun onClick(view: View, lyric_time: Int) {
                if (isMovable && album_img_card_view.visibility == 8) { // 가사 이동 모드
                    playing_time.text = mSec2Time(lyric_time / 1000.toLong())
                    mediaPlayer!!.seekTo(lyric_time)
                } else {
//                    lyrics_recycler_view.suppressLayout(true)
                    if (album_img_card_view.visibility == 0) { // play -> lyric
                        album_img_card_view.visibility = GONE
                        lyrics_close_button.visibility = VISIBLE
                        lyrics_toggle.visibility = VISIBLE
                    } else if (album_img_card_view.visibility == 8) { // lyric -> play
                        album_img_card_view.visibility = VISIBLE
                        lyrics_close_button.visibility = INVISIBLE
                        lyrics_toggle.visibility = INVISIBLE
                    }
                }
            }
        }
        lyrics_recycler_view.adapter = adapter



        // 쓰레드로 seekBar이동
        class MyThread : Thread() {
            override fun run() {
                var nowIndex = -1
                while (mediaPlayer!!.isPlaying) {
                    SystemClock.sleep(200)
                    runOnUiThread {
                        seek_bar.setProgress((mediaPlayer!!.currentPosition).toFloat())
                        playing_time.text = mSec2Time(mediaPlayer!!.currentPosition / 1000.toLong())
                        var tmpIndex = findLowerBound(lyricList, (mediaPlayer!!.currentPosition))
                        if (nowIndex != tmpIndex) {
                            lyricList[tmpIndex].colors = Color.parseColor("#ff0000")
                            if (nowIndex >= 0) {
                                lyricList[nowIndex].colors = Color.parseColor("#757575")
                            }
                            nowIndex = tmpIndex

                            var centerOfScreen = lyrics_recycler_view.height / 3
                            (lyrics_recycler_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                                tmpIndex,
                                centerOfScreen
                            );
                        }


                        Log.d("time", tmpIndex.toString() + "" + nowIndex)
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
                if (seekParams.fromUser) {
                    playing_time.text = mSec2Time(seekParams.progress / 1000.toLong())
                    tempSeekParams = seekParams.progress
                    seek_bar.setProgress(seekParams.progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
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
            if (album_img_card_view.visibility == 0) { // VISIBLE
                album_img_card_view.visibility = GONE
                lyrics_close_button.visibility = VISIBLE
                lyrics_toggle.visibility = VISIBLE
            } else if (album_img_card_view.visibility == 8) { // GONE
                if (!isMovable) {
                    album_img_card_view.visibility = VISIBLE
                    lyrics_close_button.visibility = INVISIBLE
                    lyrics_toggle.visibility = INVISIBLE
//                    lyrics_text_view.movementMethod = null
                } else {
//                    lyrics_text_view.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                }
            }
        }

        lyrics_close_button.setOnClickListener {
//            lyrics_text_view.movementMethod = null
            album_img_card_view.visibility = VISIBLE
            lyrics_close_button.visibility = INVISIBLE
            lyrics_toggle.visibility = INVISIBLE
        }

        lyrics_toggle.setOnClickListener {
            isMovable = !isMovable
            if (isMovable) {
//                lyrics_text_view.movementMethod = ScrollingMovementMethod() // 가사 스크롤
                lyrics_toggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
//                lyrics_text_view.movementMethod = null
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
                                //lyrics_text_view.text = data!!.lyrics
//                                Glide.with(mContext).load(data!!.image).into(album_img)
                                binding.song = Song(
                                    data.singer,
                                    data.album,
                                    data.title,
                                    data.duration,
                                    data.image,
                                    data.file,
                                    data.lyrics
                                )

                                mediaPlayer!!.apply {
                                    setDataSource(songUrl)
                                    prepare()
                                    playSong()
                                }

                                for (it in data.lyrics.split("\n")) {
                                    var tmpLy = Lyric()
                                    tmpLy.time = time2mSec(it)
                                    tmpLy.lyric = it.split("]")[1]
                                    tmpLy.colors = Color.parseColor("#757575")
                                    lyricList.add(tmpLy)
                                }

                                Log.d("lyrics", lyricList.toString())

//                                val adapter = LyricsAdapter(mContext, lyricList)
//
//                                lyrics_recycler_view.layoutManager = LinearLayoutManager(mContext)
//                                adapter.itemClick = object : LyricsAdapter.ItemClick {
//                                    override fun onClick(view: View, lyric_time: Int) {
//                                        playing_time.text = mSec2Time(lyric_time / 1000.toLong())
//                                        mediaPlayer!!.seekTo(lyric_time)
//                                    }
//                                }
//                                lyrics_recycler_view.adapter = adapter
                                seek_bar.max = (mediaPlayer!!.duration).toFloat()
                                song_time.text = mSec2Time((mediaPlayer!!.duration).toLong())
                            }
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

fun mSec2Time(mSec: Long): String {
    val hours: Long = mSec / 60 / 60 % 24
    val minutes: Long = mSec / 60 % 60
    val seconds: Long = mSec % 60

    return if (mSec < 3600000) {
        String.format("%02d:%02d", minutes, seconds)
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

fun time2mSec(time: String): Int {
    val timeList = time.split("[")[1].split("]")[0].split(":")
    val min = timeList[0].toInt()
    val sec = timeList[1].toInt()
    val msec = timeList[2].toInt()

    return min * 60000 + sec * 1000 + msec
}

fun findLowerBound(lyrics: List<Lyric>, target: Int): Int {
    var start = 0
    var mid = 0
    var end = lyrics.size - 1

    if (target >= lyrics[end].time) {
        return end
    }

    while (end > start) { // end가 start보다 같거나 작아지면 종료
        mid = (start + end) / 2
        if (lyrics[mid].time >= target) {
            end = mid
        } else {
            start = mid + 1
        }
    }

    return if (end == 0) 0 else end - 1
}


