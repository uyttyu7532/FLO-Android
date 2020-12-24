package com.example.flo.viewmodel

//data class Lyric(val time: Int, val lyric: String, var colors: Int)



import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.flo.BR

class Lyric : BaseObservable() {
    @get:Bindable
    var time: Int = 0
        set(t) {
            field = t
            notifyPropertyChanged(BR.time)
        }

    @get:Bindable
    var lyric: String = ""
        set(t) {
            field = t
            notifyPropertyChanged(BR.lyric)
        }

    @get:Bindable
    var colors: Int = 0
        set(t) {
            field = t
            notifyPropertyChanged(BR.colors)
        }
}