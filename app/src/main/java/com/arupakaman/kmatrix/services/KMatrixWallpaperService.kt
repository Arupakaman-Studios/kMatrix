package com.arupakaman.kmatrix.services

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.arupakaman.kmatrix.data.KMatrixSharedPrefs
import com.arupakaman.kmatrix.uiModules.other.BitSequenceMain


class KMatrixWallpaperService : WallpaperService() {

    companion object {
        private val TAG by lazy { "KMWallpaperService" }

        private var reset = false
        private var previewReset = false
        fun reset() {
            previewReset = true
            reset = true
        }

    }

    private var r = 0
    private var g = 0
    private var b = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "onCreateEngine")
        return KMatrixWallpaperEngine()
    }

    inner class KMatrixWallpaperEngine : Engine() {
        private val handler: Handler = Handler(Looper.getMainLooper())
        private var visible = true

        /** The sequences to draw on the screen  */
        private val sequences: MutableList<BitSequenceMain> = ArrayList()
        private var width = 0

        /**
         * The main runnable that is given to the Handler to draw the animation
         */
        private val drawRunnable = Runnable { draw() }

        /** Draws all of the bit sequences on the screen  */
        private fun draw() {
            if (visible) {
                // We can't have just one reset flag, because then the preview
                // would consume that flag and the actual wallpaper wouldn't be
                // reset
                if (previewReset && isPreview) {
                    previewReset = false
                    resetSequences()
                } else if (reset && !isPreview) {
                    reset = false
                    resetSequences()
                }
                val holder = surfaceHolder
                val c: Canvas? = holder.lockCanvas()
                try {
                    if (c != null) {
                        c.drawARGB(255, r, g, b)
                        for (i in sequences.indices) {
                            sequences[i].draw(c)
                        }
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c)
                    }
                }

                // Remove the runnable, and only schedule the next run if
                // visible
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            } else {
                pause()
            }
        }

        // TODO: Not all of the sequences need to be cleared
        private fun resetSequences() {
            val mPrefs = KMatrixSharedPrefs.getInstance(applicationContext)
            val color = Color.parseColor(mPrefs.colorBackground)
            r = color shr 16 and 0xFF
            g = color shr 8 and 0xFF
            b = color shr 0 and 0xFF
            stop()
            sequences.clear()
            val numSequences = (1.5 * width / BitSequenceMain.getWidth()).toInt()
            for (i in 0 until numSequences) {
                sequences.add(BitSequenceMain(((i * BitSequenceMain.getWidth() / 1.5)).toInt()))
            }
            start()
        }

        private fun pause() {
            handler.removeCallbacks(drawRunnable)
            for (i in sequences.indices) {
                sequences[i].pause()
            }
        }

        private fun start() {
            handler.post(drawRunnable)
            for (i in sequences.indices) {
                sequences[i].unpause()
            }
        }

        private fun stop() {
            handler.removeCallbacks(drawRunnable)
            for (i in sequences.indices) {
                sequences[i].stop()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "onSurfaceCreated")
            BitSequenceMain.configure(applicationContext)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "onSurfaceDestroyed")
            pause()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int,
                                      width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "onSurfaceChanged")
            this.width = width
            BitSequenceMain.setScreenDim(width, height)

            // Initialize BitSequences
            resetSequences()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d(TAG, "onVisibilityChanged")
            if (visible) {
                start()
            } else {
                pause()
            }
            this.visible = visible
        }
    }

}