package com.draw.free.customView

import android.annotation.SuppressLint
import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.Skybox
import com.google.android.filament.utils.*
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer


class GlbView() {
    companion object
    {
        init {
            Utils.init();
        }
    }

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private var loadJob : Job? = null
    var isBackgroundLight = false

    fun loadEntity()
    {
        choreographer = Choreographer.getInstance()
    }

    fun changeBackground() {
        if (isBackgroundLight) {
            modelViewer.scene.skybox!!.setColor(0.0f, 0.0f, 0.0f, 1.0f)
        } else {
            modelViewer.scene.skybox!!.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        }

        isBackgroundLight = !isBackgroundLight
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setSurfaceView(mSurfaceView: SurfaceView)
    {
        modelViewer = ModelViewer(mSurfaceView)
        mSurfaceView.setOnTouchListener(modelViewer)

        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)
        modelViewer.scene.skybox!!.setColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private suspend fun loadGlb(buffer: ByteBuffer) {
        withContext(Dispatchers.Main) {
            modelViewer.destroyModel()
            modelViewer.loadModelGlb(buffer.rewind())
            modelViewer.transformToUnitCube()
        }
    }

    fun loadModelData(glbUrl: String) {
       loadJob = CoroutineScope(Dispatchers.IO).launch {

           try {
               val url = URL(glbUrl)
               val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
               urlConnection.connect()
               val inputStream = BufferedInputStream(urlConnection.inputStream)
               val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
               val byteArrayOutputStream = ByteArrayOutputStream()
               var bytesRead: Int
               while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                   byteArrayOutputStream.write(buffer, 0, bytesRead)
               }
               val byteArr = byteArrayOutputStream.toByteArray()
               val byteBuffer = ByteBuffer.wrap(byteArr)

               loadGlb(byteBuffer)
           } catch (e : Exception) {

           }

        }

    }

    fun loadGlb(context:Context, name: String)
    {
        val buffer = readAsset(context, "model/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }
    fun loadGlb(context:Context, dirName: String, name: String)
    {
        val buffer = readAsset(context, "model/${dirName}/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }

    fun loadIndirectLight(context: Context)
    {
        val buffer = readAsset(context, "environments/venetian_crossroads_2k_ibl.ktx")
        KtxLoader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 10_000f
            modelViewer.scene.indirectLight = this
        }
    }

    fun loadEnvironment(context: Context)
    {
        val buffer = readAsset(context, "environments/venetian_crossroads_2k_skybox.ktx")
        KtxLoader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(context: Context, assetName: String): ByteBuffer
    {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            modelViewer.render(currentTime)
        }
    }

    fun onResume() {
        choreographer.postFrameCallback(frameCallback)
    }
    fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onStart() {
        loadJob?.start()
    }

    fun onStop() {
        loadJob?.cancel()
    }

    fun onDestroy() {
        choreographer.removeFrameCallback(frameCallback)
        loadJob?.cancel()
        loadJob = null
    }
}