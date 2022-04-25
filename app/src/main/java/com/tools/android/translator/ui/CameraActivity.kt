package com.tools.android.translator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Size
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityCameraBinding
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.translate.TranslateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created on 2022/4/22
 * Describe:
 */
class CameraActivity: BaseBindingActivity<ActivityCameraBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivityCameraBinding {
        return ActivityCameraBinding.inflate(layoutInflater)
    }

    private lateinit var mTrModel: TranslateViewModel
    private lateinit var mPreviewView: PreviewView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivCamera.isSelected = true
            tvCamera.setTextColor(Color.parseColor("#FBB79F"))

            ivText.setOnClickListener(this@CameraActivity)
            tvText.setOnClickListener(this@CameraActivity)
            ivSetting.setOnClickListener(this@CameraActivity)
            tvSetting.setOnClickListener(this@CameraActivity)
        }
        mPreviewView = binding.previewView

        lifecycleScope.launch(Dispatchers.IO) {
            while (mPreviewView.width == 0) delay(20)
            startCamera()
        }
        choosePicture = registerForActivityResult(ChoosePicture()) {
            showImage(false, it)
        }

        mTrModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(TranslateViewModel::class.java)
        initLanguageViews()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_text, R.id.tv_text -> finish()

            R.id.iv_setting, R.id.tv_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }

            R.id.camera1 -> takePhoto()

            R.id.iv_album -> choosePicture.launch(null)

            R.id.iv_close -> cancelRecognize()

            R.id.copy -> {

            }
        }
    }

    private var cameraControl: CameraControl? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var choosePicture: ActivityResultLauncher<Void?>
    //private var recognizeDialog = LoadingDialog().apply { setMessage("Recognizing...") }
    private var parseBitmap: Bitmap? = null
    private var origin: Language = LanguageAdapter.sourceLa
    private var target: Language = LanguageAdapter.targetLa
    private var recognized = false

    private val recognizer by lazy {
        when (origin.code) {
            "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "ko" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "sa" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }.apply {
            lifecycleScope.launch { lifecycle.addObserver(this@apply) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        choosePicture.unregister()
        parseBitmap?.recycle()
    }

    private fun begin2autoRecognize() {
        binding.groupLanguage.visibility = View.GONE
        binding.layoutLoading.visibility = View.VISIBLE
        binding.progressBar.show()
        binding.ivClose.visibility = View.VISIBLE
        try {
            recognizeTextOnDevice(InputImage.fromBitmap(parseBitmap!!, 0))
        } catch (e: Exception) {
            failedRecognize()
        }
    }

    private fun recognizeTextOnDevice(image: InputImage): Task<Text> {
        return recognizer.process(image)
            .addOnSuccessListener { visionText ->
                binding.layoutLoading.visibility = View.GONE
                try {
                    successRecognize(visionText)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .addOnFailureListener { exception ->
                binding.layoutLoading.visibility = View.GONE
                val mlKitException = exception as? MlKitException
                if (mlKitException != null && mlKitException.errorCode == MlKitException.UNAVAILABLE) {
                    toastLong("Waiting for text recognition model to be downloaded")
                } else {
                    failedRecognize()
                }
            }
    }

    private fun successRecognize(visionText: Text) {
        if (visionText.text.isEmpty() && visionText.textBlocks.isEmpty()) {
            toastLong("Text not recognized")
        } else {
            recognized = true
            binding.groupResult.visibility = View.VISIBLE
            binding.resultTv.text = visionText.text
        }
    }

    private fun failedRecognize() {
        toastLong("Recognize Failed, try again")
    }

    private fun cancelRecognize() {
        binding.groupLanguage.visibility = View.VISIBLE
        mPreviewView.visibility = View.VISIBLE
        binding.imgSelect.visibility = View.GONE
        binding.ivClose.visibility = View.GONE
        binding.groupResult.visibility = View.GONE
        parseBitmap?.recycle()
        parseBitmap = null
        startCamera()

        try {
            File(cacheDir, "itrans_alum.jpg").delete()
        } catch (e: Exception) {}
    }

    private fun startCamera() {
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(mPreviewView.display.rotation)
            .setTargetResolution(Size(mPreviewView.width, mPreviewView.height))
            .build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(mPreviewView.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                val camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                cameraControl = camera.cameraControl
                mPreviewView.visibility = View.VISIBLE
            } catch (exc: Exception) {
                mPreviewView.visibility = View.VISIBLE
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(cacheDir, "itrans_alum.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    showImage(true)
                }
            })
    }

    private fun showImage(saved: Boolean, uri: Uri? = null) {
        recognized = false
        if (uri == null && !saved) {
            return
        }
        try {
            mPreviewView.visibility = View.GONE
            binding.imgSelect.visibility = View.VISIBLE
            if (saved) {
                parseBitmap =
                    BitmapFactory.decodeFile(File(cacheDir, "itrans_alum.jpg").absolutePath)
                binding.imgSelect.setImageBitmap(parseBitmap)
            } else {
                parseBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
                scaleBitmap(parseBitmap)
                binding.imgSelect.setImageBitmap(parseBitmap)
            }

            //自动识别
            begin2autoRecognize()
        } catch (e: Exception) {
            mPreviewView.visibility = View.VISIBLE
            binding.imgSelect.visibility = View.GONE
        }
    }

    private fun scaleBitmap(origin: Bitmap?) {
        if (origin == null) return
        val width = origin.width
        val height = origin.height
        val scaleW = mPreviewView.width * 1.0F / width
        val scaleH = mPreviewView.height * 1.0F / height
        val scale = maxOf(scaleW, scaleH)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
    }

    class ChoosePicture : ActivityResultContract<Void?, Uri>() {
        override fun createIntent(context: Context, input: Void?): Intent {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (context.packageManager.resolveActivity(intent, 0) == null) {
                intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
            }
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) {
                intent?.data
            } else {
                null
            }
        }
    }

    private var isTranslating = false
    private var exchanging = false
    private fun initLanguageViews() {
        binding.resultTv.isVerticalScrollBarEnabled = true
        binding.resultTv.movementMethod = ScrollingMovementMethod.getInstance()
        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = LanguageAdapter.targetLa
        binding.imgExchange.setOnClickListener {
            if (exchanging) return@setOnClickListener
            exchanging = true
            exchangeLanguage()
            freshLangUI()
            exchanging = false
            //textChangedForTranslatePrepared()
        }
        freshLangUI()

        binding.languagePanel.root.setChoiceListener(object :LanguageAdapter.ILangChoice{
            //选择语言
            override fun onChoice(language: Language) {
                if (!language.isAvailable()) return
                if (LanguageAdapter.isCurrentSource) {
                    mTrModel.sourceLang.value = language
                    LanguageAdapter.sourceLa = language
                    App.ins.sourceLa = language.code
                } else {
                    mTrModel.targetLang.value = language
                    LanguageAdapter.targetLa = language
                    App.ins.targetLa = language.code
                }
                binding.languagePanel.root.collapse()
                freshLangUI()
            }

            //下载 / 删除
            override fun onStatus(language: Language) {
                //下载
                if (language.isUnavailable()) {
                    language.available = 0
                    mTrModel.downloadLanguage(language)
                } else {
                    if ((LanguageAdapter.isCurrentSource && language == LanguageAdapter.sourceLa)
                        || (!LanguageAdapter.isCurrentSource && language == LanguageAdapter.targetLa)) {
                        return
                    } else {
                        mTrModel.deleteLanguage(language)
                    }
                }
                binding.languagePanel.root.notifyAllAdapter()
            }
        })

        mTrModel.translatedText.observe(
            this,
            { resultOrError ->
                isTranslating = false
                if (resultOrError.error != null) {
                    //srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    if (resultOrError.result.isNullOrEmpty()) {
                        binding.groupResult.visibility = View.GONE
                    } else {
                        binding.groupResult.visibility = View.VISIBLE
                    }
                    binding.resultTv.text = resultOrError.result
                }
            }
        )

        // Update sync toggle button states based on downloaded models list.
        mTrModel.availableModels.observe(this) {
            /*languageList.forEach { lang ->
                if (lang.available != 1)
                    lang.available = -1
            }*/
            for (la in languageList) {
                if (it.contains(la.code)) {
                    la.available = 1
                } else {
                    la.available = -1
                }
            }
            binding.languagePanel.root.notifyAllAdapter()
        }
    }

    private fun exchangeLanguage() {
        val lang = LanguageAdapter.sourceLa
        LanguageAdapter.sourceLa = LanguageAdapter.targetLa
        LanguageAdapter.targetLa = lang

        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = lang
        App.ins.sourceLa = LanguageAdapter.sourceLa.code
        App.ins.targetLa = LanguageAdapter.targetLa.code
    }

    private fun freshLangUI() {
        mTrModel.sourceLang.value?.apply {
            binding.tvLaSource.text = displayName
        }

        mTrModel.targetLang.value?.apply {
            binding.tvLaTarget.text = displayName
        }
    }
}