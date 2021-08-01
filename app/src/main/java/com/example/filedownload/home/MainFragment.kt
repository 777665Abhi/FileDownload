package com.example.filedownload.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.filedownload.R
import com.example.filedownload.databinding.FragmentMainBinding
import com.example.filedownload.downloadFile
import com.example.filedownload.network.DownloadResult
import com.example.loginviaotp.Utils
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    private lateinit var viewModel: MainViewModel

    private val PERMISSIONS = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSION_REQUEST_CODE = 1
    private val DOWNLOAD_FILE_CODE = 2
    private val APPLICATION_ID = "com.example.filedownload"

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (hasPermissions(context, PERMISSIONS)) {

                setDownloadButtonClickListener()
        } else {
            requestPermissions(PERMISSIONS.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setDownloadButtonClickListener() {
        val folder = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "Download.pdf"
        val file = File(folder, fileName)
        val uri = context?.let {
            FileProvider.getUriForFile(it, "${APPLICATION_ID}.provider", file)
        }
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri?.path)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        binding.viewButton.setOnClickListener {

                        if (TextUtils.isEmpty(binding.etUrl!!.text.toString())) {
                Utils.showToast(activity!!, "Enter url")
            } else {
                fileUrl = binding.etUrl!!.text.toString()
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.setDataAndType(uri, mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(Intent.EXTRA_TITLE, fileName)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, DOWNLOAD_FILE_CODE)
        }}
    }

    private fun downloadFile(context: Context, url: String, file: Uri) {
        val ktor = HttpClient(Android)

        viewModel.setDownloading(true)
        context.contentResolver.openOutputStream(file)?.let { outputStream ->
            CoroutineScope(Dispatchers.IO).launch {
                ktor.downloadFile(outputStream, url).collect {
                    withContext(Dispatchers.Main) {
                        when (it) {
                            is DownloadResult.Success -> {
                                viewModel.setDownloading(false)
                                binding.progressBar.progress = 0
                                viewFile(file)
                            }

                            is DownloadResult.Error -> {
                                viewModel.setDownloading(false)
                                Utils.showToast(context, "Error while downloading file")
                            }

                            is DownloadResult.Progress -> {
                                binding.progressBar.progress = it.progress
                            }
                        }
                    }
                }
            }
        }
    }

    private fun viewFile(uri: Uri) {
        context?.let { context ->
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(intent, "Open with")

            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(chooser)
            } else {
                Utils.showToast(context, "No suitable application to open file")
            }
        }
    }

    private var fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun hasPermissions(context: Context?, permissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            return permissions.all { permission ->
                ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE && hasPermissions(context, PERMISSIONS)) {
            setDownloadButtonClickListener()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DOWNLOAD_FILE_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                context?.let { context ->
                    downloadFile(context, fileUrl, uri)
                }
            }
        }
    }
}
