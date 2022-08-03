# Fetch All PDFs and Folders Containing PDFs in Android
## Fast, Simple, Easy to Use
Fetch all pdfs along with folders containing pdfs in Android device

## Import

### Add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
  ...
  maven { url 'https://jitpack.io' }
  }
}
```

### Add the dependency

```
dependencies {
  implementation 'com.github.Irfan-Karim:Fetch-All-PDFs-Android:1.0.0'
}
```

## Disclaimer
Get Read Write permission or Manage all storage permission before initialization else fetched folder and pdfs will be null

## Fetch All PDFs

### Create Instance of pdfFetcher

```
val pdfFetcher = PdfFetcher(context)
```

### Call fetchAllPDFs to get all pdf files in device

```
CoroutineScope(Dispathers.IO).launch {
  pdfFetcher.fetchAllPDFs { file ->
    Log.i("TAG", "getPdf: ${file?.size}")
  }
}
```

### Sort The PDFs

use PDFSortOrder._ as followed

```
CoroutineScope(Dispathers.IO).launch {
  pdfFetcher.fetchAllPDFs(PDFSortOrder.LastModifiedAscending) { file ->
    Log.i("TAG", "getPdf: ${file?.size}")
  }
}
```

## Fetch All Folders Containing PDFs

### Create Instance of PdfFetcher

```
val pdfFetcher = PdfFetcher(context)
```

### Call getDataAndFolders to get all video folders in device

```
imageFetcher.getDataAndFolders { folder ->
  Log.i("TAG", "getPdfs: ${folder?.size}")
}
```

Folder will contain the name of the folder and all the pdf contained in that folder

```
folder.foreach { it ->
  log.i("TAG", ${it.name})
  log.i("TAG", ${it.data.size})
}
```

### Sort the Folders

Use FolderSortOrder._ as followed

```
CoroutineScope(Dispatchers.IO).launch {
  pdfFetcher.getDataAndFolders(null,FolderSortOrder.LengthAscending) { folder ->
    Log.i("TAG", "getPdf: ${folder?.size}")
  }
}
```

### Sort both Folders and PDFs

Use PDFSortOrder._ and FolderSortOrder._ as followed

```
CoroutineScope(Dispatchers.IO).launch {
  videoFetcher.getDataAndFolders(PDFSortOrder.LastModifiedAscending,FolderSortOrder.LengthAscending) { folder ->
    Log.i("TAG", "getPdf: ${folder?.size}")
  }
}
```
