package spirit.bangkit.kusaku.ui.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import spirit.bangkit.kusaku.adapter.ResultAdapter
import spirit.bangkit.kusaku.data.source.remote.response.FaceResult
import spirit.bangkit.kusaku.databinding.ActivityResultBinding
import spirit.bangkit.kusaku.ui.main.LocalModelActivity
import spirit.bangkit.kusaku.util.ConverterHelper
import java.lang.Exception

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESULT = "extra_result"
    }

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val faceResult = intent.getParcelableExtra<FaceResult>(EXTRA_RESULT)
        var resultAdapter : ResultAdapter? = null
        try {
            val mapResult = ConverterHelper.faceResultToMap(faceResult!!)
            resultAdapter = ResultAdapter(mapResult)
        } catch (e: Exception) {
            Toast.makeText(this, "Fail load result", Toast.LENGTH_SHORT).show()
        }


        with (binding.rvResult) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ResultActivity)
            adapter = resultAdapter
        }
        resultAdapter?.notifyDataSetChanged()
    }
}