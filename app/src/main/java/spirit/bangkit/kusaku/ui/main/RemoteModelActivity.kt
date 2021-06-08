package spirit.bangkit.kusaku.ui.main

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainRemoteBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory
import spirit.bangkit.kusaku.ui.main.base.MainBaseViewModel
import spirit.bangkit.kusaku.ui.result.ResultActivity
import spirit.bangkit.kusaku.util.ConverterHelper
import java.lang.Exception

class RemoteModelActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainRemoteBinding
    private lateinit var model: MainRemoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseRemote.progressBar.visibility = View.INVISIBLE

        setUpModel()

        binding.baseRemote.btnStart.setOnClickListener(this)
        binding.baseRemote.btnVideo.setOnClickListener(this)
        binding.baseRemote.btnResult.setOnClickListener(this)
    }

    private fun setUpModel() {
        val factory = KusakuViewModelFactory.getInstance(application, activityResultRegistry)
        model = ViewModelProvider(this, factory)[MainRemoteViewModel::class.java]

        model.workingOn.observe(this) {
            when (it) {
                MainBaseViewModel.EXTRACT_FRAME -> {
                    binding.baseRemote.tvStatusWorking.text = getString(R.string.working_frame)
                }
                MainRemoteViewModel.ENCODING_FRAME -> {
                    binding.baseRemote.tvStatusWorking.text = getString(R.string.working_encoding)
                }
                MainRemoteViewModel.WAITING_RESPONSE -> {
                    binding.baseRemote.tvStatusWorking.text = getString(R.string.working_wait)
                }
                MainBaseViewModel.IDLE -> {
                    binding.baseRemote.progressBar.visibility = View.INVISIBLE
                }
                MainBaseViewModel.DONE -> {
                    binding.baseRemote.btnResult.isEnabled = true
                    binding.baseRemote.progressBar.visibility = View.INVISIBLE
                    binding.baseRemote.tvStatusWorking.text = getString(R.string.working_wait_done)
                }
                MainRemoteViewModel.SERVER_BUSY -> {
                    binding.baseRemote.progressBar.visibility = View.INVISIBLE
                    binding.baseRemote.btnStart.isEnabled = true
                    val msg = model.getResponse.value?.message
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
            binding.baseRemote.btnStart.text =
                if (it == MainRemoteViewModel.SERVER_BUSY) "GET DATA" else "START"
        }

        model.ready.observe(this) {
            binding.baseRemote.btnStart.isEnabled = it == "READY"
        }

        model.loading.observe(this) {
            when (model.workingOn.value) {
                MainBaseViewModel.EXTRACT_FRAME -> {
                    binding.baseRemote.tvStatusNum.text = getString(R.string.count_frame, it)
                }
            }
        }

        model.imageIcon.observe(this, { img ->
            Glide.with(this)
                .load(img)
                .into(binding.baseRemote.imgVideo)
        })
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_video -> {
                model.getVideo()
                binding.baseRemote.tvStatusNum.text = ""
                binding.baseRemote.tvStatusWorking.text = ""
                binding.baseRemote.btnResult.isEnabled = false
            }
            R.id.btn_start -> {
                if (model.workingOn.value == MainRemoteViewModel.SERVER_BUSY) {
                    model.getResultFromRemoteModel()
                } else {
                    model.startProcessingVideo()
                    v.isEnabled = false
                    binding.baseRemote.progressBar.visibility = View.VISIBLE
                }
            }
            R.id.btn_result -> {
                val intent = Intent(this, ResultActivity::class.java)
                val faceResult = model.getResponse.value
                try {
                    intent.putExtra(ResultActivity.EXTRA_RESULT, faceResult)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}