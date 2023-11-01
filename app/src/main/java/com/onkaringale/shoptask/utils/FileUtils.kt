package com.onkaringale.shoptask.utils

import android.content.Context
import android.net.Uri
import android.os.FileUtils.copy
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.onkaringale.shoptask.ui.customer.registration.RegistrationActivity
import id.zelory.compressor.Compressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.util.UUID


class FileUtils(val mcontext: Context)
{

    fun deleteall()
    {
        mcontext.filesDir.deleteRecursively()
    }

    fun createTempFile(filename: String): File
    {
        val outputDir: File = mcontext.cacheDir // context being the Activity pointer
        return File.createTempFile(filename, null, outputDir)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun compress(activity: RegistrationActivity, FileFromContentUri: File)
    {
        GlobalScope.launch {
            val file = Compressor.compress(activity, FileFromContentUri)
            activity.runOnUiThread {
                if (file.canRead())
                    activity.SetImage(file)
                else
                    Toast.makeText(activity, "File Error", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @Throws(IOException::class)
    private fun copyFileUsingStream(source: File, dest: File)
    {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try
        {
            `is` = FileInputStream(source)
            os = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length: Int
            while (`is`.read(buffer).also { length = it } > 0)
            {
                os.write(buffer, 0, length)
            }
        }
        finally
        {
            `is`?.close()
            os?.close()
        }
    }

    fun saveFile(filename: String,file: File)
    {
        val dest= createFileInternal(filename)
        copyFileUsingStream(file,dest)
    }

    fun createFileInternal(filename: String): File
    {
        return File(mcontext.filesDir, filename)
    }

    fun deleteFile(file:File)
    {
        file.deleteRecursively()
    }
    fun getFile(filename: String): File
    {
        return File(mcontext.filesDir,filename)
    }


 fun getFilePathFromUri(context: Context, uri: Uri): String  =
    if (uri.path?.contains("file://") == true) uri.path!!
    else getFileFromContentUri(context, uri).path

 fun getFileFromContentUri(context: Context, contentUri: Uri): File {
    // Preparing Temp file name
    val fileExtension = getFileExtension(context, contentUri)
     val uuid=UUID.randomUUID().toString()
    val fileName = uuid + if (fileExtension != null) ".$fileExtension" else ""
    // Creating Temp file
    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()
    //Initialize streams
    var oStream: FileOutputStream? = null
    var inputStream: InputStream? = null

    try {
        oStream = FileOutputStream(tempFile)
        inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let { copy(inputStream, oStream) }
        oStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        //Close streams
        inputStream?.close()
        oStream?.close()
    }

    return tempFile
}

private fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}
}