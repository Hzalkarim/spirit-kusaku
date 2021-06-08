package spirit.bangkit.kusaku.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainRemoteBinding

class RemoteModelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainRemoteBinding
    private lateinit var model: MainRemoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}