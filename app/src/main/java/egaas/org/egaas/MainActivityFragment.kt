package egaas.org.egaas

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import egaas.org.egaas.tasks.SaveImageTask
import egaas.org.egaas.tasks.WebAsyncRequest
import okhttp3.OkHttpClient
import java.net.URL

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    private val TAG = this@MainActivityFragment.javaClass.canonicalName

    lateinit var webView: WebView
    var mUploadHandler: UploadHandler? = null
    var mNewUploaderHandler: NewUploadHandler? = null
    private var POOL = ""




    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_main, container, false)

        webView = view.findViewById(R.id.webView)
        webView.setBackgroundColor(R.color.colorPrimary)
        webView.webViewClient = CustomWebClient()
        webView.webChromeClient = CustomChromeClient()
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.d("!!!!", "$url, $userAgent, $contentDisposition, $mimetype, $contentLength")
        }
        initializeWebView()
//        WebAsyncRequest(httpClient, webView!!).execute("http://getpool.dcoin.club")

        nodeJSONArray(getString(R.string.nodes))
        return view
    }

//    override fun onPause() {
//        super.onPause()
//        bundle = Bundle()
//        webView?.saveState(bundle)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        webView?.restoreState(bundle)
//    }

    fun nodeJSONArray(addr: String) {
        val request = LocalAsyncTask(activity)
        request.execute(addr)
    }

    inner class LocalAsyncTask(val activity: Activity): WebAsyncRequest(OkHttpClient()) {

        override fun onPostExecute(result: String?) {
            if(result == null) return
            Log.d(TAG, result)
            webView.loadUrl(result)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Log.d(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == Result.FILE_SELECTED) {
            mUploadHandler?.onResult(resultCode, intent)
            mNewUploaderHandler?.onResult(resultCode, intent)
        }

    }


    fun initializeWebView() {
        val settings = webView?.settings
        settings?.javaScriptEnabled = true
        settings?.allowFileAccessFromFileURLs = true
        settings?.domStorageEnabled = true
        settings?.allowContentAccess = true
        settings?.loadsImagesAutomatically = true
        settings?.cacheMode = WebSettings.LOAD_NO_CACHE
        settings?.blockNetworkLoads = false
        settings?.blockNetworkImage = false
        settings?.databaseEnabled = true
        settings?.loadWithOverviewMode = true
        settings?.useWideViewPort = true
        settings?.allowFileAccess = true
        settings?.setSupportZoom(true)
        webView?.clearHistory()
        webView?.clearFormData()
    }


    inner class CustomWebClient : WebViewClient() {
        var poolName: String? = null

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            if (poolName == null) {
                poolName = url
            }
            Log.d(TAG, view.url + " " + url)
            if (url.contains("dcoinKey", true)) {
                SaveImageTask(activity).execute(url)
            }
            if (!url.contains(POOL, true)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.endsWith(".mp4") || !url.contains(poolName!!)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            } else {
                return false
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }
    }



    inner class CustomChromeClient : WebChromeClient() {

        override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
            Log.d("JavaGoWV", String.format("%s @ %d: %s",
                    cm.message(), cm.lineNumber(), cm.sourceId()))
            return true
        }


        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            callback.invoke(origin, true, false)
        }


        private fun getTitleFromUrl(url: String): String {
            val title = url
            try {
                val urlObj = URL(url)
                val host = urlObj.host
                if (host != null && !host.isEmpty()) {
                    return urlObj.protocol + "://" + host
                }
                if (url.startsWith("file:")) {
                    val fileName = urlObj.file
                    if (fileName != null && !fileName.isEmpty()) {
                        return fileName
                    }
                }
            } catch (e: Exception) {
                // ignore
            }

            return title
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {

            if (message.contains("location")) return false

            val newTitle = getTitleFromUrl(url)

            AlertDialog.Builder(this@MainActivityFragment.activity)
                    .setTitle(newTitle)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, which -> result.confirm() }
                    .setCancelable(false).create().show()
            return true
            // return super.onJsAlert(view, url, message, result);
        }

        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
            if (message.contains("location")) {
                return false
            }

            val newTitle = getTitleFromUrl(url)

            AlertDialog.Builder(this@MainActivityFragment.activity)
                    .setTitle(newTitle).setMessage(message)
                    .setPositiveButton(android.R.string.ok) {
                        _, _ -> result.confirm()
                    }
                    .setNegativeButton(android.R.string.cancel) {
                        _, _ -> result.cancel()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            return true

            // return super.onJsConfirm(view, url, message, result);
        }

        // Android 4.1
        @JvmOverloads
        fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String = "", capture: String) {
            mUploadHandler = UploadHandler(this@MainActivityFragment)
            mUploadHandler!!.openFileChooser(uploadMsg)
        }

        @TargetApi(21)
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
           Log.d(TAG, "onShowFileChooser")
            mNewUploaderHandler = NewUploadHandler(this@MainActivityFragment)
            mNewUploaderHandler!!.openFileChooser(filePathCallback!!, fileChooserParams!!)

            return true
        }
    }

}


