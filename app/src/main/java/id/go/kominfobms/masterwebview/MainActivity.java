package id.go.kominfobms.masterwebview;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageForAndroid5;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    private final static int FILECHOOSER_RESULTCODE_FROM_CAMERA_ANDROID_5 = 3;
    private final static int QUALITY_COMPRESS = 90;
    private Uri imageUri;
    public static String fileName;

    private WebView webView;
    private Toolbar toolbar;
    private Context context;
    private String URL = "https://rafly.id/";
    private String TAG = "MainActivity :: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        settingComponent();
        settingToolbar();
        settingWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void settingWebView() {
        //mengaktifkan dukungan untuk javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setUserAgentString(Locale.getDefault().getLanguage());
        webView.loadUrl(URL);

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
//            String cookies = CookieManager.getInstance().getCookie(url);
//            request.addRequestHeader("cookie", cookies);
//            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading File...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                            url, contentDisposition, mimetype));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading file..", Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Toast.makeText(context, "Mohon tunggu beberapa saat", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                b.toastLong(error.getDescription().toString());
                Log.e("TAG", "onReceivedError: " + error.getDescription().toString());
                Toast.makeText(getApplicationContext(), error.getDescription().toString(), Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebChromeClient(new CustomWebChromeClient());
    }

    protected class CustomWebChromeClient extends WebChromeClient {

        //For Android API >= 21 (5.0 OS)
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Log.e(TAG, "onShowFileChooser: For Android API >= 21 (5.0 OS)");
            mUploadMessageForAndroid5 = filePathCallback;
            openFileChooser(2);
            return true;
        }


        // For Android > 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            Log.e(TAG, "openFileChooser: For Android > 4.1");
            mUploadMessage = uploadMsg;
            openFileChooser(1);
        }

        // Andorid 3.0 +
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            Log.e(TAG, "openFileChooser: For Android > 4.1");
            mUploadMessage = uploadMsg;
            openFileChooser(1);
        }

        @SuppressLint("IntentReset")
        public void openFileChooser(int mode) {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            if (mode == 1) {
                startActivityForResult(i, FILECHOOSER_RESULTCODE);
            } else {
                startActivityForResult(i, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
            }

            //------------------

//            final String[] ACCEPT_MIME_TYPES = {
//                    "application/pdf",
//                    "image/*"
//            };
//            Intent intent = new Intent();
//            intent.setType("*/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPT_MIME_TYPES);
//            if (mode == 1) {
//                startActivityForResult(Intent.createChooser(intent, "Select a file"), FILECHOOSER_RESULTCODE);
//            } else {
//                startActivityForResult(Intent.createChooser(intent, "Select a file"), FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
//            }

        }
    }

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            if (resultCode != RESULT_OK) {
                mUploadMessage.onReceiveValue(null);
                return;
            }
            Uri result = intent.getData(); // get file
            if (result == null) {
                mUploadMessage.onReceiveValue(null);
                return;
            }

            String fPath = getPath(result);
            File file = new File(fPath);
            if (!file.exists()) {
                mUploadMessage.onReceiveValue(null);
                Toast.makeText(getApplicationContext(),
                        "Please select a local Imagefile.", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                File kompres = new Compressor(context)
                        .setQuality(QUALITY_COMPRESS)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ).getAbsolutePath())
                        .compressToFile(file);

                Log.e(TAG, "Hasil Kompresss: " + kompres.getAbsolutePath());
                Log.e(TAG, "Size Kompresss: " + kompres.length());

                Uri hasilKompres = Uri.fromFile(kompres);
                Log.e(TAG, TAG + " # result.getPath()=" + result.getPath());
                Log.e(TAG, TAG + " # kompres.getPath()=" + kompres.getPath());
                mUploadMessage.onReceiveValue(hasilKompres);
                mUploadMessage = null;
            } catch (IOException e) {
                Log.e(TAG, "onActivityResult: " + e.getMessage());
                e.printStackTrace();
            }

        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {

            if (null == mUploadMessageForAndroid5) return;
            if (resultCode != RESULT_OK) {
                mUploadMessageForAndroid5.onReceiveValue(null);
                return;
            }

            Uri result = intent.getData(); // get file
            if (result == null) {
                mUploadMessage.onReceiveValue(null);
                return;
            }
            Log.e(TAG, "onActivityResult: " + intent.getData().toString());
            String fPath = getPath(result);

            // we actually don't allow http links in gallery like google+ or
            // facebook images
            File file = new File(fPath);
            Log.e(TAG, "onActivityResult: file exist " + fPath + " " + file.exists());
            if (!file.exists()) {
                mUploadMessage.onReceiveValue(null);
                Toast.makeText(getApplicationContext(),
                        "Please select a local Imagefile.", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                File kompres = new Compressor(context)
                        .setQuality(QUALITY_COMPRESS)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ).getAbsolutePath())
                        .compressToFile(file);

                Log.e(TAG, "Hasil Kompresss: " + kompres.getAbsolutePath());
                Log.e(TAG, "Size Kompresss: " + kompres.length());

                Uri hasilKompres = Uri.fromFile(kompres);
                Log.e(TAG, TAG + " # result.getPath()=" + result.getPath());
                Log.e(TAG, TAG + " # kompres.getPath()=" + kompres.getPath());
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{hasilKompres});
                mUploadMessageForAndroid5 = null;
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE_FROM_CAMERA_ANDROID_5) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG, "onActivityResult: RESULT_OK");
//                if (mUploadMessageForAndroid5 == null) return;
//                Uri result = getCacheImagePath(fileName); // get file
//                Uri result = Uri.fromFile(new File(getCacheDir(), queryName(getContentResolver(), getCacheImagePath(fileName)))); // get file
                Uri result = intent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                @SuppressLint("Recycle")
                Cursor cursor = getContentResolver().query(result, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //file path of captured image
                    String filePath = cursor.getString(columnIndex);
                    //file path of captured image
                    File f = new File(filePath);
                    String filename = f.getName();

                    Log.e(TAG, "onActivityResult: filePath " + filePath);
                    Log.e(TAG, "onActivityResult: filename " + filename);
                    cursor.close();
                }
                if (result == null) {
                    Log.e(TAG, "onActivityResult: result null :( ");
                    mUploadMessageForAndroid5.onReceiveValue(null);
                    return;
                }

//                String fPath = getPath(result);
                String fPath = result.getPath();
                Log.e(TAG, "onActivityResult: fPath " + fPath);

                File file = new File(fPath);
                Log.e(TAG, "onActivityResult: file exist " + fPath + " " + file.exists());
                if (!file.exists()) {
                    mUploadMessageForAndroid5.onReceiveValue(null);
                    Toast.makeText(getApplicationContext(),
                            "Please select a local Imagefile.", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                try {
                    File kompres = new Compressor(context)
                            .setQuality(QUALITY_COMPRESS)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                            ).getAbsolutePath())
                            .compressToFile(file);

                    Log.e(TAG, "Hasil Kompresss: " + kompres.getAbsolutePath());
                    Log.e(TAG, "Size Kompresss: " + kompres.length());

                    Uri hasilKompres = Uri.fromFile(kompres);
                    Log.e(TAG, TAG + " # result.getPath()=" + result.getPath());
                    Log.e(TAG, TAG + " # kompres.getPath()=" + kompres.getPath());

                    mUploadMessageForAndroid5.onReceiveValue(new Uri[]{hasilKompres});
                    mUploadMessageForAndroid5 = null;
                } catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "onActivityResult: NO OKE");
                mUploadMessageForAndroid5.onReceiveValue(null);
                Toast.makeText(context, "Gagal mengambil foto", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void settingToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle("JUDUL NYAA");
        toolbar.setSubtitle("SUB JUDUL");

        //TODO : UN COMENT KALO MAU ADA TOMBOL BACK
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));

//        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void settingComponent() {
        context = MainActivity.this;
        webView = findViewById(R.id.webView);
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}