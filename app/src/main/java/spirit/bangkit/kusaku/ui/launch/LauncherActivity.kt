package spirit.bangkit.kusaku.ui.launch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityLauncherBinding
import spirit.bangkit.kusaku.ui.main.LocalModelActivity
import spirit.bangkit.kusaku.ui.main.RemoteModelActivity

class LauncherActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityLauncherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLocalModel.setOnClickListener(this)
        binding.btnRemoteModel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_local_model -> {
                val intent = Intent(this, LocalModelActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_remote_model -> {
                val intent = Intent(this, RemoteModelActivity::class.java)
                startActivity(intent)
            }
        }
    }
}