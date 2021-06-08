package spirit.bangkit.kusaku.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory

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
            groupMetadata.visibility = View.GONE

            btnVideo.setOnClickListener(this@LocalModelActivity)
            btnStart.setOnClickListener(this@LocalModelActivity)
        }
    }

    private fun setupLocalViewModel() {
        val factory = KusakuViewModelFactory.getInstance(application, activityResultRegistry)
        model = ViewModelProvider(this, factory)[MainLocalViewModel::class.java]

        model.loading.observe(this) {
            binding.baseLocal.tvStatusNum.text = it.toString()
        }

        model.workingOn.observe(this) {
            when (it) {
                MainLocalViewModel.EXTRACT_FRAME -> {
                    binding.baseLocal.tvStatusWorking.text = getString(R.string.working_frame)
                }
                MainLocalViewModel.DETECT_LABEL_FACE -> {
                    binding.baseLocal.tvStatusWorking.text = getString(R.string.working_label)
                }
                MainLocalViewModel.IDLE -> {
                    binding.baseLocal.groupMetadata.visibility = View.GONE
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

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_video -> {
                model.getVideo()
            }
            R.id.btn_start -> {
                model.startProcessingVideo()
                v.isEnabled = false
                binding.baseLocal.groupMetadata.visibility = View.VISIBLE
            }
            /*
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
            }*/
        }
    }

    /*private fun setupViewModel() {
        model = ViewModelProvider(
            this,
            KusakuViewModelFactory.getInstance(application, activityResultRegistry, mmr)
        ).get(MainViewModel::class.java)

        model.data.observe(this, { arr ->
            val map = arr.groupingBy { it }.eachCount()
            Log.d("Hasil", map.toString())
            binding.progressBar.visibility = View.INVISIBLE
        })

        model.ready.observe(this, { s ->
            when (s) {
                MainViewModel.READY_EXTRACT -> {
                    binding.btnVideo.alpha = .3f
                    binding.tvMetadata.text = "READY"
                    binding.tvMetadata.visibility = View.VISIBLE
                }
                MainViewModel.READY_RESULT -> {
                    binding.btnVideo.alpha = .3f
                    binding.tvMetadata.visibility = View.GONE


                }
                MainViewModel.FAILED -> {
                    binding.btnVideo.alpha = 1f
                    binding.tvMetadata.visibility = View.GONE

                }
            }
        })

        model.imageIcon.observe(this, { img ->
            Glide.with(this)
                .load(img)
                .into(binding.imgVideo)
        })
    }*/

}