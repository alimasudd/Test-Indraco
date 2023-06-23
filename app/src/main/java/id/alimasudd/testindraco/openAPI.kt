package id.alimasudd.testindraco

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_open_api.*

class openAPI : AppCompatActivity() {
    lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_api)

        search.setOnClickListener {
            var input = userinput.text.toString()
            fetchData(input)
        }
    }

    fun fetchData( input: String){
        val url = "http://www.omdbapi.com/?t=${input}&apikey=cebd9b53"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if(response.get("Response")=="False"){
                    name.text = "Incorrect detail"
                }else {
                    Log.d("volPoster",response.getString("Poster"))
                    Glide.with(this).load(response.getString("Poster")).into(image)
                    name.text = response.getString("Title")+"\n\n"+"Writer: "+response.getString("Writer")
                    plot.text = response.getString("Plot")
                }
            },
            { error ->
                Log.d("vol",error.toString())
            }
        )

        val appnetwork = BasicNetwork(HurlStack())
        val appcache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        requestQueue = RequestQueue(appcache, appnetwork).apply {
            start()
        }
        requestQueue.add(jsonObjectRequest)
    }
}