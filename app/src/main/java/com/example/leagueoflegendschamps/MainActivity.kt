package com.example.leagueoflegendschamps

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var randomChampionButton: Button
    private lateinit var championNameTextView: TextView
    private lateinit var championImageView: ImageView
    private lateinit var passiveTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        randomChampionButton = findViewById(R.id.randomChampionButton)
        championNameTextView = findViewById(R.id.ChampionName)
        championImageView = findViewById(R.id.champImage)
        passiveTextView = findViewById(R.id.passiveTextView)

        randomChampionButton.setOnClickListener {
            FetchRandomChampion().execute()
        }
    }

    private fun fetchWithApiKey(urlString: String): String {
        val url = URL(urlString)
        val urlConnection = url.openConnection() as HttpURLConnection
        val apiKey = "RGAPI-744d2904-b78b-43a1-89ab-d48417ef8ace"

        try {
            urlConnection.setRequestProperty("X-Riot-Token", apiKey)
            val inputStream = urlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            return stringBuilder.toString()
        } finally {
            urlConnection.disconnect()
        }
    }

    inner class FetchRandomChampion : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val url = "https://ddragon.leagueoflegends.com/cdn/14.6.1/data/en_US/champion.json"
            return fetchWithApiKey(url)
        }

        override fun onPostExecute(result: String?) {
            result?.let {
                val jsonObject = JSONObject(it)
                val dataObject = jsonObject.getJSONObject("data").keys().asSequence().toList()
                val randomChampionKey = dataObject.random()
                FetchChampion().execute(randomChampionKey) // Now truly selects a random champion
            }
        }
    }

    inner class FetchChampion : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            val championName = params[0] ?: return ""
            val url = "https://ddragon.leagueoflegends.com/cdn/14.6.1/data/en_US/champion/$championName.json"
            return fetchWithApiKey(url)
        }

        override fun onPostExecute(result: String?) {
            result?.let {
                val jsonObject = JSONObject(it)
                val data = jsonObject.getJSONObject("data")
                val championKey = data.keys().next()
                val championObject = data.getJSONObject(championKey)

                val name = championObject.getString("name")
                val title = championObject.getString("title")
                val passiveDescription = championObject.getJSONObject("passive").getString("description")
                val imageUrl = "https://ddragon.leagueoflegends.com/cdn/14.6.1/img/champion/${championObject.getJSONObject("image").getString("full")}"

                championNameTextView.text = "$name - $title"
                passiveTextView.text = passiveDescription
                Glide.with(this@MainActivity).load(imageUrl).into(championImageView)
            }
        }
    }
}
