package spirit.bangkit.kusaku.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import spirit.bangkit.kusaku.databinding.ActivityMainRemoteBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory

class RemoteModelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainRemoteBinding
    private lateinit var model: MainRemoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpModel()
    }

    private fun setUpModel() {
        val factory = KusakuViewModelFactory.getInstance(application, activityResultRegistry)
        model = ViewModelProvider(this, factory)[MainRemoteViewModel::class.java]


    }
}