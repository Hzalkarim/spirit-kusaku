package spirit.bangkit.kusaku.tensorflow

import android.content.Context
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

class TensorFlowModel (context: Context) {

    private val _probabilityBuffer : TensorBuffer =
        TensorBuffer.createFixedSize(intArrayOf(1, 7), DataType.FLOAT32)

    private val _interpreter : Interpreter? = try {
        val tfliteModel = FileUtil.loadMappedFile(context,"face_model_two.tflite");
        Interpreter(tfliteModel)
    } catch (e : IOException){
        Log.e("tfliteSupport", "Error reading model", e);
        null
    }

    private val _axisLabels = try {
        FileUtil.loadLabels(context, "face_label.txt")
    } catch (e: IOException) {
        null
    }

    private val _probabilityProcessor =
        TensorProcessor.Builder().add(NormalizeOp(0f, 1f)).build()

    val probabilityBuffer get() = _probabilityBuffer
    val interpreter get() = _interpreter
    val axisLabels = _axisLabels
    val probabilityProcessor get() = _probabilityProcessor

    fun processImageToLabel(image: TensorImage) : TensorLabel? {
        interpreter?.run(image.buffer, probabilityBuffer.buffer)

        return if (axisLabels != null) {
            TensorLabel(axisLabels, probabilityProcessor.process(probabilityBuffer))
        } else {
            null
        }
    }

}