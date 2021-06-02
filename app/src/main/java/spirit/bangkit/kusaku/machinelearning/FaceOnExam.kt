package spirit.bangkit.kusaku.machinelearning

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.metadata.MetadataExtractor
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.util.*

/**
 * Identify Facial Expression such asAngry, Disgust, Fear, Happy, Neutral, Sad, Surprise  */
class FaceOnExam private constructor(context: Context, options: Model.Options) : MlModel {
    private val imageProcessor: ImageProcessor
    private var imageHeight = 0
    private var imageWidth = 0
    private var labels: List<String>
    private val probabilityPostProcessor: TensorProcessor
    private var model: Model
    fun process(image: TensorImage): Outputs {
        imageHeight = image.height
        imageWidth = image.width
        val processedimage = imageProcessor.process(image)
        val outputs = Outputs(model)
        model.run(arrayOf<Any>(processedimage.buffer), outputs.buffer)
        return outputs
    }

    fun close() {
        model.close()
    }

    fun process(image: TensorBuffer): Outputs {
        val outputs = Outputs(model)
        model.run(arrayOf<Any>(image.buffer), outputs.buffer)
        return outputs
    }

    inner class Outputs constructor(model: Model) {
        private val probability: TensorBuffer =
            TensorBuffer.createFixedSize(model.getOutputTensorShape(0), DataType.FLOAT32)
        val probabilityAsCategoryList: List<Category>
            get() = TensorLabel(
                labels,
                probabilityPostProcessor.process(probability)
            ).getCategoryList()
        val probabilityAsTensorBuffer: TensorBuffer
            get() = probabilityPostProcessor.process(probability)
        val buffer: Map<Int, Any>
            get() {
                val outputs: MutableMap<Int, Any> = HashMap()
                outputs[0] = probability.buffer
                return outputs
            }

    }

    companion object {
        @Throws(IOException::class)
        fun newInstance(context: Context): FaceOnExam {
            return FaceOnExam(context, Model.Options.Builder().build())
        }

        fun newInstance(context: Context, options: Model.Options): FaceOnExam {
            return FaceOnExam(context, options)
        }
    }

    init {
        model = Model.createModel(context, "face_on_exam.tflite", options)
        val extractor = MetadataExtractor(model.getData())
        val imageProcessorBuilder = ImageProcessor.Builder()
            .add(ResizeOp(48, 48, ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(floatArrayOf(127.5f), floatArrayOf(127.5f)))
            .add(QuantizeOp(0f, 0.0f))
            .add(CastOp(DataType.FLOAT32))
        imageProcessor = imageProcessorBuilder.build()
        val probabilityPostProcessorBuilder = TensorProcessor.Builder()
            .add(DequantizeOp(0.toFloat(), 0.0.toFloat()))
            .add(NormalizeOp(floatArrayOf(0.0f), floatArrayOf(1.0f)))
        probabilityPostProcessor = probabilityPostProcessorBuilder.build()
        labels = FileUtil.loadLabels(context, "face_label.txt")
    }
}