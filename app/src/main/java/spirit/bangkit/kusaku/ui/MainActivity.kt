package spirit.bangkit.kusaku.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import org.tensorflow.lite.support.image.TensorImage
import spirit.bangkit.kusaku.R
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import spirit.bangkit.kusaku.factory.KusakuViewModelFactory
import spirit.bangkit.kusaku.machinelearning.FaceOnExam
import java.lang.Exception
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var model: MainViewModel

    private val imagesResource = listOf(
        R.drawable.satu,
        R.drawable.dua,
        R.drawable.tiga,
        R.drawable.empat,
        R.drawable.lima,
        R.drawable.enam,
        R.drawable.tujuh,
        R.drawable.lapan,
    )

    private var imgIndex = 0

    enum class ScrollDirection(i: Int) {LEFT(-1), RIGHT(1)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = ViewModelProvider(this, KusakuViewModelFactory.getInstance(this)).get(MainViewModel::class.java)

        model.data.observe(this, { arr ->
            val map = arr.groupingBy { it }.eachCount()
            Log.d("Hasil", map.toString())
        })
        val mmr = MediaMetadataRetriever()
        try{
            val uriString = "android.resource://" + packageName + "/" + R.raw.video_test
            mmr.setDataSource(this, Uri.parse(uriString))

            for (i in 1..10) {
                val bitmap = mmr.getFrameAtTime(1000000 * i.toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
                Log.d("Config", "${bitmap?.config}")

                model.detectFaces(bitmap!!, i==10)
            }
/*
            val detector = FaceDetection.getClient()

            val image = InputImage.fromBitmap(bitmap!!, 0)
            binding.btnDetect.isClickable = false
            detector.process(image)
                .addOnSuccessListener { faces ->
                    with (faces[0].boundingBox){
                        val newBitmap = try{

                            Bitmap.createBitmap(bitmap, centerX() - width()/2 , centerY() - height()/2, width(), height())
                        } catch (e: IllegalArgumentException) {
                            binding.tvPredict.text = "Face Error"
                            null
                        }

                        TensorPredict(newBitmap!!.copy(Bitmap.Config.ARGB_8888, false))
                        runOnUiThread { binding.imgFace.setImageBitmap(newBitmap) }
                    }
                }*/
        } catch (e: Exception){
            binding.tvPredict.text = "No File"
            e.printStackTrace()
        }

        /*Glide.with(this)
            .load(R.drawable.satu)
            .into(binding.imgFace)*/

        with(binding){
            btnDetect.setOnClickListener(this@MainActivity)
            btnLeft.setOnClickListener(this@MainActivity)
            btnRight.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_detect -> {
                val bitmap = (binding.imgFace.drawable as BitmapDrawable).bitmap
                val image = InputImage.fromBitmap(bitmap, 0)

                val detector = FaceDetection.getClient()

                binding.btnDetect.isClickable = false
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        //val drawingView = DrawRect(applicationContext, faces)
                        //drawingView.draw(Canvas(bitmap))

                        with (faces[0].boundingBox){
                            val newBitmap = try{

                                Bitmap.createBitmap(bitmap, centerX() - width()/2 , centerY() - height()/2, width(), height())
                            } catch (e: IllegalArgumentException) {
                                binding.tvPredict.text = "Face Error"
                                null
                            }

                            TensorPredict(newBitmap!!)
                            runOnUiThread { binding.imgFace.setImageBitmap(newBitmap) }
                        }
                    }
            }

            R.id.btn_left -> {
                scrollImage(ScrollDirection.LEFT)
                binding.btnDetect.isClickable = true
                binding.tvPredict.text = "NoN"
            }
            R.id.btn_right -> {
                scrollImage(ScrollDirection.RIGHT)
                binding.btnDetect.isClickable = true
                binding.tvPredict.text = "NoN"
            }
        }
    }

    private fun scrollImage(dir: ScrollDirection) {
        when (dir) {
            ScrollDirection.LEFT -> {
                if (imgIndex - 1 < 0) {
                    imgIndex = imagesResource.lastIndex
                    setImage(imagesResource[imgIndex])
                } else {
                    setImage(imagesResource[--imgIndex])
                }
            }
            ScrollDirection.RIGHT -> {
                if (imgIndex + 1 > imagesResource.lastIndex) {
                    setImage(imagesResource[0])
                    imgIndex = 0
                } else {
                    setImage(imagesResource[++imgIndex])
                }
            }
        }
    }

    private fun setImage(id: Int) {
        Glide.with(this)
            .load(id)
            .into(binding.imgFace)
    }

    private fun TensorPredict(bitmap: Bitmap) {
        val tensor = FaceOnExam.newInstance(this)

        val tImage = TensorImage.fromBitmap(bitmap)
        val labels = tensor.process(tImage)

        val category = labels.probabilityAsCategoryList[0].label

        binding.tvPredict.text = category
        Log.d("Hasil", labels.probabilityAsCategoryList.toString())
    }

}