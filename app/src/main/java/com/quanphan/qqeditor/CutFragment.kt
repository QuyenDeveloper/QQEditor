package com.quanphan.qqeditor

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.google.android.material.slider.RangeSlider


class CutFragment : Fragment() {
    var videoView:VideoView?=null
    var seekBar: RangeSlider? =null
    var tvRight:TextView?=null
    var tvLeft:TextView?=null
    var startS:Int = 0
    var endS:Int = 0
    lateinit var dataPasser: OnDataPass


    private var selectedUri: Uri? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment

        var rootview = inflater.inflate(R.layout.fragment_cut, container, false)

        return rootview
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView = view.findViewById<VideoView>(R.id.vv2)
        tvRight = view.findViewById<TextView>(R.id.tvRight)
        tvLeft = view.findViewById<TextView>(R.id.tvLeft)
        seekBar = view.findViewById<RangeSlider>(R.id.seekbar)


        arguments?.let {
            selectedUri = Uri.parse(it.getString("contentURI"))
        }

        Log.d("New file",selectedUri.toString())
        setRightDuration(selectedUri)

        seekBar?.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being started
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: RangeSlider) {
                val array = slider.values
                startS = array[0].toInt()
                endS = array[1].toInt()
                passData(startS,endS)
                tvLeft!!.setText(getTime(array[0].toInt() * 1000))
                tvRight!!.setText(getTime(array[1].toInt() * 1000))
            }
        })
    }

    fun passData(data: Int, endS: Int){
        dataPasser.startS(data)
        dataPasser.endS(endS)
    }

//    set right time text duration
    fun setRightDuration(contentURI: Uri?) {
        if (isAdded()){

            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(requireActivity().applicationContext, contentURI!!)
                prepare()
            }

            var duration:String = getTime(mediaPlayer.duration).toString()
            seekBar?.setValues(mutableListOf(0f,((mediaPlayer.duration / 1000).toFloat())))
            seekBar?.valueFrom = 0f
            seekBar?.valueTo = ((mediaPlayer.duration / 1000).toFloat())

            mediaPlayer.stop()

            tvRight?.setText(duration)

        }
    }


//    calculate video duration
    fun getTime(m: Int): String? {
        var finalString:String="00"
        var minutesString:String="00"
        var secondsString:String="00"

        var hour = m / (1000*60*60)
        var minutes = (m % (1000*60*60)) / (1000*60)
        var seconds = (m % (1000*60*60)) % (1000*60) / 1000

        if(hour > 0){
            finalString = hour.toString()
        }
        if (minutes > 0){
            if (minutes > 9){
                minutesString = minutes.toString()
            }else{
                minutesString = "0" + minutes.toString()
            }
        }

        if (seconds > 0){
            if (seconds > 9){
                secondsString = seconds.toString()
            }else{
                secondsString = "0" + seconds.toString()
            }
        }


        finalString = finalString + ":" +minutesString + ":" + secondsString

        return finalString
    }

    interface OnDataPass{
        fun  startS(data: Int)
        fun endS(data: Int)
    }
}