package egaas.org.egaas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import egaas.org.egaas.tasks.WebAsyncRequest
import okhttp3.OkHttpClient
import org.json.JSONArray

/**
 * Created by anechaev on 1/8/17.
 */

class VersionActivityFragment: Fragment() {

    private val TAG = this@VersionActivityFragment.javaClass.canonicalName

    private lateinit var mStableButton: Button
    private lateinit var mDevButton: Button

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_version, container, false)

        mStableButton = view.findViewById(R.id.stable_server_button)
        mDevButton = view.findViewById(R.id.dev_server_button)
        mStableButton.setOnClickListener {
            val value = resources.getString(R.string.nodes)
            nodeJSONArray(value)
        }
        mDevButton.setOnClickListener {
            val value = resources.getString(R.string.nodes)
            nodeJSONArray(value)
        }

        verifyStoragePermissions(activity)
        return view
    }

    fun nodeJSONArray(addr: String) {
        val request = LocalAsyncTask(activity)
        request.execute(addr)
    }

    class LocalAsyncTask(val activity: Activity): WebAsyncRequest(OkHttpClient()) {

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val intent = Intent(this.activity, MainActivity::class.java)
            val key = activity.resources.getString(R.string.node_type)
            intent.putExtra(key, result!!)
            this.activity.startActivity(intent)
        }
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }

        val internetPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.INTERNET), 1)
        }
    }
}