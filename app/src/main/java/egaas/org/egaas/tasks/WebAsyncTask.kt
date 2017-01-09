package egaas.org.egaas.tasks

import android.os.AsyncTask
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*

/**
 * Created by faraday on 5/7/16.
 * ${PROJECT}
 */

open class WebAsyncRequest(val httpClient: OkHttpClient): AsyncTask<String, Int, String>() {

    private val TAG = this@WebAsyncRequest.javaClass.canonicalName

    override fun doInBackground(vararg params: String?): String? {
        try {
            val request = Request.Builder().url(params[0]).build()
            val response = httpClient.newCall(request).execute()

            val gson = Gson()
            val json = gson.fromJson(response.body().string(), JsonObject::class.java)

            val arr = json.getAsJsonArray("nodes")
            if (arr != null && arr.size() > 0) {
                val elem = arr.get(Random().nextInt(arr.size()))
                return elem.asString
            }
            val pool = "http://node0.egaas.org"
            return pool
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

        return "http://localhost"
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
    }
}