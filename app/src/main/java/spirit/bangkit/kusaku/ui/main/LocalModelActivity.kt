package spirit.bangkit.kusaku.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory
import spirit.bangkit.kusaku.ui.main.base.MainBaseViewModel
import spirit.bangkit.kusaku.ui.result.ResultActivity
import spirit.bangkit.kusaku.util.ConverterHelper
import java.lang.Exception

class LocalModelActivity : AppCompatActivity(), View.OnClickListener {

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
    private lateinit var model: MainLocalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocalViewModel()

        with (binding.baseLocal) {
            btnStart.isEnabled = false
            progressBar.visibility = View.INVISIBLE

            btnResult.isEnabled = false

            btnVideo.setOnClickListener(this@LocalModelActivity)
            btnStart.setOnClickListener(this@LocalModelActivity)
            btnResult.setOnClickListener(this@LocalModelActivity)
        }
    }

    private fun setupLocalViewModel() {
        val factory = KusakuViewModelFactory.getInstance(application, activityResultRegistry)
        model = ViewModelProvider(this, factory)[MainLocalViewModel::class.java]

        model.loading.observe(this) {
            when (model.workingOn.value) {
                MainBaseViewModel.EXTRACT_FRAME -> {
                    binding.baseLocal.tvStatusNum.text = getString(R.string.count_frame, it)
                }
                MainBaseViewModel.LABEL_FACE -> {
                    binding.baseLocal.tvStatusNum.text = getString(R.string.count_label, it)
                }
            }
        }

        model.workingOn.observe(this) {
            when (it) {
                MainBaseViewModel.EXTRACT_FRAME -> {
                    binding.baseLocal.tvStatusWorking.text = getString(R.string.working_frame)
                }
                MainBaseViewModel.DETECT_FACE -> {
                    binding.baseLocal.tvStatusWorking.text = getString(R.string.working_detect)
                }
                MainBaseViewModel.LABEL_FACE -> {
                    binding.baseLocal.tvStatusWorking.text = getString(R.string.working_label)
                }
                MainBaseViewModel.IDLE -> {
                    binding.baseLocal.progressBar.visibility = View.INVISIBLE
                }
                MainBaseViewModel.DONE -> {
                    binding.baseLocal.btnResult.isEnabled = true
                }
            }
        }

        model.ready.observe(this) {
            binding.baseLocal.btnStart.isEnabled = it == "READY"
        }

        model.imageIcon.observe(this, { img ->
            Glide.with(this)
                .load(img)
                .into(binding.baseLocal.imgVideo)
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_video -> {
                model.getVideo()
                binding.baseLocal.tvStatusNum.text = ""
                binding.baseLocal.tvStatusWorking.text = ""
                binding.baseLocal.btnResult.isEnabled = false
            }
            R.id.btn_start -> {
                model.startProcessingVideo()
                v.isEnabled = false
                binding.baseLocal.progressBar.visibility = View.VISIBLE
            }
            R.id.btn_result -> {
                val intent = Intent(this, ResultActivity::class.java)
                val list = model.data.value
                try {
                    val faceResult = ConverterHelper.stringListToFaceResult(list!!)
                    intent.putExtra(ResultActivity.EXTRA_RESULT, faceResult)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}