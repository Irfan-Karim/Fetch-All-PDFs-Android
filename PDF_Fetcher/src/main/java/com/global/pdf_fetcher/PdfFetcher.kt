package com.global.pdf_fetcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.global.pdf_fetcher.models.Folder
import com.global.pdf_fetcher.models.FolderSortOrder
import com.global.pdf_fetcher.models.PDFSortOrder
import java.io.File
import java.text.Collator

class PdfFetcher(context: Context) {

    private var allPdfs: MutableList<File> = mutableListOf()
    private var mContext = context

    fun fetchAllPDFs(sortingOrder: PDFSortOrder? = null, callback: (list: MutableList<File>?) -> Unit) {
        if (globalPermissionCheck() || checkPermissionForReadExternalStorage()) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
            val selectionArgs = arrayOf(mimeType)
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }
            mContext.contentResolver.query(collection, projection, selection, selectionArgs, null).use { cursor ->
                assert(cursor != null)
                Log.i(TAG, "getPdfList: ${cursor?.count}")
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        allPdfs.add(File(cursor.getString(columnData)))
                        Log.d(TAG, "getPdf: " + cursor.getString(columnData))
                    } while (cursor.moveToNext())
                } else {
                    callback(null)
                    return
                }
            }
            if (allPdfs.isNotEmpty()) {
                val collator = Collator.getInstance()
                when (sortingOrder) {
                    null -> {
                        callback(allPdfs)
                    }
                    PDFSortOrder.NameAscending -> {
                        allPdfs = allPdfs.sortedWith { a1, a2 -> collator.compare(a1.nameWithoutExtension, a2.nameWithoutExtension) } as MutableList<File>
                        callback(allPdfs)
                    }
                    PDFSortOrder.NameDescending -> {
                        allPdfs = allPdfs.sortedWith { a1, a2 -> collator.compare(a2.nameWithoutExtension, a1.nameWithoutExtension) } as MutableList<File>
                        callback(allPdfs)
                    }
                    PDFSortOrder.SizeAscending -> {
                        allPdfs = allPdfs.sortedByDescending { it.length() } as MutableList<File>
                        callback(allPdfs)
                    }
                    PDFSortOrder.SizeDescending -> {
                        allPdfs = allPdfs.sortedBy { it.length() } as MutableList<File>
                        callback(allPdfs)
                    }
                    PDFSortOrder.LastModifiedAscending -> {
                        allPdfs = allPdfs.sortedByDescending { it.lastModified() } as MutableList<File>
                        callback(allPdfs)
                    }
                    PDFSortOrder.LastModifiedDescending -> {
                        allPdfs = allPdfs.sortedBy { it.lastModified() } as MutableList<File>
                        callback(allPdfs)
                    }
                }
            } else {
                callback(null)
            }
        } else {
            Log.i(TAG, "FileFetcher: Permission not found")
        }
    }

    fun getDataAndFolders(pdfSortOrder: PDFSortOrder? = null, foldersSortOrder: FolderSortOrder? = null, callback: (list: MutableList<Folder>?) -> Unit) {
        fetchAllPDFs(pdfSortOrder) { files ->
            if (files != null) {
                var grouped = allPdfs.groupBy { it.parentFile }.map {
                    it.key?.let { it1 ->
                        Folder(
                            it1.nameWithoutExtension,
                            it.value as MutableList<File>
                        )
                    }
                }
                when (foldersSortOrder) {
                    null -> {
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.NameAscending -> {
                        val collator = Collator.getInstance()
                        grouped = grouped.sortedWith { c1, c2 -> collator.compare(c1?.name, c2?.name) }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.NameDescending -> {
                        val collator = Collator.getInstance()
                        grouped = grouped.sortedWith { c1, c2 -> collator.compare(c2?.name, c1?.name) }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.LengthAscending -> {
                        grouped = grouped.sortedBy {
                            it?.folderLength
                        }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.LengthDescending -> {
                        grouped = grouped.sortedByDescending {
                            it?.folderLength
                        }
                        callback(grouped as MutableList<Folder>)
                    }
                }
            } else {
                callback(null)
            }
        }
    }


    private fun globalPermissionCheck(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (checkPermissionForReadExternalStorage()) {
                return true
            }
        } else {
            if (Environment.isExternalStorageManager()) {
                return true
            }
        }
        return false
    }

    private fun checkPermissionForReadExternalStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        } else false
    }

    companion object {
        const val TAG = "PdfFetcherTag"
    }

}