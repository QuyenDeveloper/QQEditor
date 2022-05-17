package com.quanphan.qqeditor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.MediaController
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.fragment_music.*
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [MusicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class MusicFragment : Fragment() {
    // TODO: Rename and change types of parameters
    var selectedUri: Uri? = null
    var seekBarMusic:RangeSlider?=null
    var btMute:AppCompatButton?=null
    var btPickMusic:AppCompatButton?=null
    var tvMusicLeft:TextView?=null
    var tvMusicRight:TextView?=null
    var choseStartPoint:Int?=null
    var choseEndPoint:Int?=null
    var musicUri:Uri?=null
    var maxDuration:Float?= null
    var funtion:Int = 0

    lateinit var dataPasser: OnDataPass
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBarMusic = view.findViewById<RangeSlider>(R.id.seekBarMusic)
        btMute = view.findViewById<AppCompatButton>(R.id.btnMute)
        btPickMusic = view.findViewById<AppCompatButton>(R.id.btnPickMusic)
        tvMusicLeft = view.findViewById<TextView>(R.id.tvMusicLeft)
        tvMusicRight = view.findViewById<TextView>(R.id.tvMusicRight)

        arguments?.let {
            selectedUri = Uri.parse(it.getString("contentURI"))
        }

        setDuration(selectedUri)

        passFun(funtion)
        btMute!!.setOnClickListener {
            funtion = 0
            passFun(funtion!!)
        }
        btPickMusic!!.setOnClickListener {
            funtion = 1
            passFun(funtion!!)
            pickMusic()
        }

        seekBarMusic!!.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: RangeSlider) {

            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: RangeSlider) {
                val array = slider.values
                choseStartPoint = array[0].toInt()
                choseEndPoint = array[1].toInt()
                passData(choseStartPoint!!, choseEndPoint!!)
                tvMusicLeft!!.setText(getTime(array[0].toInt() * 1000))
                tvMusicRight!!.setText(getTime(array[1].toInt() * 1000))
            }
        })

    }

    private fun pickMusic() {
        MediaScannerConnection.scanFile(
            activity,
            arrayOf(Environment.getExternalStorageDirectory().toString()),
            null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2){
            Log.d("requestCodeMusic",requestCode.toString())
            if (data!!.data == null && resultCode == 0){
                Log.d("Data:","null")
            }else{
                musicUri = data!!.data
                passMusicUri(musicUri.toString())
                Log.d("musicUri",musicUri.toString())
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    fun setDuration(contentURI: Uri?) {
        if (isAdded()){

            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(requireContext().applicationContext, contentURI!!)
                prepare()
            }

            var duration:String = getTime(mediaPlayer.duration).toString()

            seekBarMusic?.setValues(0f,(mediaPlayer.duration / 1000).toFloat())
            seekBarMusic?.valueFrom = 0f
            maxDuration = ((mediaPlayer.duration / 1000).toFloat())
            seekBarMusic?.valueTo = maxDuration as Float

            mediaPlayer.stop()

            tvMusicRight?.setText(duration)

        }
    }
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

    fun passData(data: Int, endS: Int){
        dataPasser.startS(data)
        dataPasser.endS(endS)
    }

    fun passFun(data: Int){
        dataPasser.funtion(data)
    }
    fun passMusicUri(data: String){
        dataPasser.musicUri(data)
    }
    interface OnDataPass{
        fun startS(data: Int)
        fun endS(data: Int)
        fun funtion(data: Int)
        fun musicUri(data: String)
    }
}