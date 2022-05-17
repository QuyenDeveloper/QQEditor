package com.quanphan.qqeditor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.activity_menu.*
import java.io.File

class MenuActivity : AppCompatActivity(), CutFragment.OnDataPass,MusicFragment.OnDataPass {

    val fragmentManager = supportFragmentManager
    val GALLARY = 1


    var videoView: VideoView? = null
    var textViewLeft: TextView?= null
    var textViewRight: TextView?= null
    var seekBar: RangeSlider?=null
    var btnSave:AppCompatButton?=null


    var Cut = CutFragment()
    var MusicFun:Int?=null
    var contentURI:Uri?=null
    var c:Int?=0
    var uri:String?=null
    var startS:Int = 0
    var endS:Int = 0

    var selectedMusicUri:Uri?=null
    var fileDir:String?=null
    var fileName:String?=null
    var folder: File?= null
    var fileExt:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        videoView = findViewById<VideoView>(R.id.vv2)
        textViewLeft = findViewById<TextView>(R.id.tvLeft)
        textViewRight = findViewById<TextView>(R.id.tvRight)
        seekBar = findViewById<RangeSlider>(R.id.seekbar)
        btnSave = findViewById<AppCompatButton>(R.id.btnsave)


        pickVideo()


        //        run base on chosed funtion
        btnSave!!.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            if (c == 0){
                builder.setMessage("Pick a funtion first")
                builder.show()
            }else if(c == 1){
                getFileName(this)
            }else if(c == 2){
                builder.setMessage("This funtion is un-useable")
                builder.show()
            }else if(c == 3){
                getFileName(this)
            }
        }
    }




//    pick video from internal storage
    private fun pickVideo() {
        MediaScannerConnection.scanFile(
            this@MenuActivity,
            arrayOf(Environment.getExternalStorageDirectory().toString()),
            null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, 1)
    }



//    start video in videoview
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 1){
        Log.d("requestCode",requestCode.toString())
        if (data!!.data == null){
            Log.d("Data:","null")
        }else{

            contentURI = data!!.data
            if (requestCode == GALLARY) {
                val mediaController = MediaController(this)
                mediaController.setAnchorView(videoView!!)
                videoView!!.setMediaController(mediaController)
                videoView!!.setVideoURI(contentURI)
                videoView!!.requestFocus()
                videoView!!.start()
            }
        }
    }
    }
//    display fragment
    fun onClickCut(view: View) {
        c = 1
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("contentURI", contentURI.toString())
        val Cut = CutFragment()
        Cut.setArguments(bundle)
        Cut.retainInstance = true
        fragmentTransaction.add(R.id.frameLayout, Cut)
        fragmentTransaction.commit()
    }
    fun onClickCrop(view: View) {
        c =2
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.frameLayout, CropFragment())
        fragmentTransaction.commit()
    }

    fun onClickMusic(view: View) {
        c = 3
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("contentURI", contentURI.toString())
        val Music = MusicFragment()
        Music.setArguments(bundle)
        Music.retainInstance = true
        fragmentTransaction.add(R.id.frameLayout, Music)
        fragmentTransaction.commit()
    }
    fun onClickSticker(view: View) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.frameLayout, StickerFragment())
        fragmentTransaction.commit()
    }


//    get time to cut
    fun getFileName(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Change video name")
        val input = EditText(context)
        input.setHint("Enter Name")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setNegativeButton("cancel", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
        })
        builder.setPositiveButton("submit", DialogInterface.OnClickListener { dialogInterface, i ->
            fileName = input.text.toString()
            processVideo(startS*1000,endS*1000, fileName!!)
            videoView?.stopPlayback()
            videoView?.setVideoURI(Uri.parse(uri))
            videoView?.requestFocus()
            videoView?.start()
        })
        builder.setMessage("Set video name?")
        builder.show()
    }


//    cut video
    fun processVideo(startTime: Int, endTime: Int, fileName: String) {
        if (c == 1){
            fileDir = "/TrimVideo"
        }else if(c == 3){
            fileDir = "/AudioChange"
        }
            folder = File(Environment.getExternalStorageDirectory().path + fileDir)
            if (!folder!!.exists()){
                folder!!.mkdirs()
            }
            fileExt = ".mp4"
            val dest: File = File(folder.toString(),fileName + fileExt)
            if(selectedMusicUri != null){
                selectedMusicUri = Uri.parse(getRealPathFromURIAPI19(this,selectedMusicUri!!))
            }
            var command:String?=null


            if (c == 1){
                command = "-i \"${getRealPathFromURIAPI19(this,contentURI!!)}\" -ss ${Cut.getTime(startTime)} -to ${Cut.getTime(endTime)} -c:v copy $dest"
                FFmpeg.execute(command)
                uri = Uri.parse(dest.toString()).toString()
                contentURI = Uri.parse(uri)
                onClickCut(btnCut)
                Log.d("Command",command)
            }else if(c == 3){
                if (MusicFun == 0){
                    command = "-i \"${getRealPathFromURIAPI19(this, contentURI!!)}\" -af \"volume=enable='between(t,${startTime/1000},${endTime/1000})':volume=0\" $dest"
                }else if (MusicFun == 1){
                    command = "-i \"${getRealPathFromURIAPI19(this, contentURI!!)}\" -i \"$selectedMusicUri\" -map 0:v -map 1:a -c:v copy -shortest $dest"
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Chose a funtion")
                    builder.show()
                }
                FFmpeg.execute(command)
                uri = Uri.parse(dest.toString()).toString()
                contentURI = Uri.parse(uri)
                onClickMusic(btnMusic)
                Log.d("Command",command!!)
            }
    }


//    get file real path
    @SuppressLint("NewApi")
    private fun getRealPathFromURIAPI19(context: Context, uri: Uri): String? {
    if(uri != null){
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                    cursor!!.moveToNext()
                    val fileName = cursor.getString(0)
                    val path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                    if (!TextUtils.isEmpty(path)) {
                        return path
                    }
                } finally {
                    cursor?.close()
                }
                val id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads"), java.lang.Long.valueOf(id))

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null

    }
    return null
    }
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    override fun startS(data: Int) {
        startS = data
    }

    override fun endS(data: Int) {
        endS = data
    }

    override fun funtion(data: Int) {
        MusicFun = data
    }

    override fun musicUri(data: String) {
        selectedMusicUri = Uri.parse(data)
    }

}