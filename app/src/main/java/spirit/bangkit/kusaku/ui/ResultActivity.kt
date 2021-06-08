package spirit.bangkit.kusaku.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import spirit.bangkit.kusaku.adapter.ResultAdapter
import spirit.bangkit.kusaku.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapResult = mutableMapOf<String?, Int>()
        for (label in LocalModelActivity.LABEL) {
            if (intent.hasExtra(label)) {
                mapResult[label] = intent.getIntExtra(label, 0)
            }
        }
        Log.d("Hasil di Result", mapResult.toString())
        if (mapResult.isNullOrEmpty()) return

        val resultAdapter = ResultAdapter(mapResult)

        with (binding.rvResult) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ResultActivity)
            adapter = resultAdapter
        }
        resultAdapter.notifyDataSetChanged()
    }
}