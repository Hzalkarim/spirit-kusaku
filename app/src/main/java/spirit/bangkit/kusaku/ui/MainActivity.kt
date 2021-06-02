package spirit.bangkit.kusaku.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        val LABEL = listOf(
            "Angry",
            "Disgust",
            "Fear",
            "Happy",
            "Neutral",
            "Sad",
            "Surprise")
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()

        binding.progressBar.visibility = View.INVISIBLE

        binding.btnResult.isEnabled = false
        binding.btnExtract.isEnabled = false

        binding.btnSelectVideo.setOnClickListener (this)
        binding.btnExtract.setOnClickListener(this)
        binding.btnResult.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_select_video -> {
                model.getVideo()
                model.clearFaces()
            }
            R.id.btn_extract -> {
                model.extractImageAndDetectFace()
                binding.progressBar.visibility = View.VISIBLE
                binding.btnExtract.isEnabled = false
            }
            R.id.btn_result -> {
                val intent = Intent(this, ResultActivity::class.java)
                val mapResult = model.data.value?.groupingBy { it }?.eachCount()
                for (pair in mapResult!!) {
                    intent.putExtra(pair.key, pair.value)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupViewModel() {
        model = ViewModelProvider(
            this,
            KusakuViewModelFactory.getInstance(application, activityResultRegistry)
        ).get(MainViewModel::class.java)

        model.data.observe(this, { arr ->
            val map = arr.groupingBy { it }.eachCount()
            Log.d("Hasil", map.toString())
            binding.progressBar.visibility = View.INVISIBLE
        })

        model.ready.observe(this, { s ->
            binding.tvPredict.text = s
            when (s) {
                MainViewModel.READY_EXTRACT -> {
                    binding.btnExtract.isEnabled = true
                    binding.btnResult.isEnabled = false
                }
                MainViewModel.READY_RESULT -> {
                    binding.btnResult.isEnabled = true
                    binding.btnExtract.isEnabled = false
                }
                MainViewModel.FAILED -> {
                    binding.btnResult.isEnabled = false
                    binding.btnExtract.isEnabled = false
                }
            }
        })
    }

}