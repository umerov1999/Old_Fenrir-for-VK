package dev.ragnarok.fenrir.picasso

import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.drawable.toDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.DecodeUtils
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Size
import dev.ragnarok.fenrir.db.Stores
import java.io.IOException
import kotlin.math.roundToInt

class CoilLocalRequestHandler : Fetcher<Uri> {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    override fun key(data: Uri) = data.toString()

    override fun handles(data: Uri): Boolean {
        return data.path != null && data.lastPathSegment != null && data.scheme != null && data.scheme == "content"
    }

    private fun isConfigValid(bitmap: Bitmap, options: Options): Boolean {
        return SDK_INT < 26 || bitmap.config != Bitmap.Config.HARDWARE || options.config == Bitmap.Config.HARDWARE
    }

    private fun isSizeValid(bitmap: Bitmap, options: Options, size: Size): Boolean {
        return options.allowInexactSize || size is OriginalSize ||
                size == DecodeUtils.computePixelSize(
            bitmap.width,
            bitmap.height,
            size,
            options.scale
        )
    }

    private fun normalizeBitmap(
        pool: BitmapPool,
        inBitmap: Bitmap,
        size: Size,
        options: Options
    ): Bitmap {
        // Fast path: if the input bitmap is valid, return it.
        if (isConfigValid(inBitmap, options) && isSizeValid(inBitmap, options, size)) {
            return inBitmap
        }

        // Slow path: re-render the bitmap with the correct size + config.
        val scale: Float
        val dstWidth: Int
        val dstHeight: Int
        when (size) {
            is PixelSize -> {
                scale = DecodeUtils.computeSizeMultiplier(
                    srcWidth = inBitmap.width,
                    srcHeight = inBitmap.height,
                    dstWidth = size.width,
                    dstHeight = size.height,
                    scale = options.scale
                ).toFloat()
                dstWidth = (scale * inBitmap.width).roundToInt()
                dstHeight = (scale * inBitmap.height).roundToInt()
            }
            is OriginalSize -> {
                scale = 1f
                dstWidth = inBitmap.width
                dstHeight = inBitmap.height
            }
        }
        val safeConfig = when {
            SDK_INT >= 26 && options.config == Bitmap.Config.HARDWARE -> Bitmap.Config.ARGB_8888
            else -> options.config
        }

        val outBitmap = pool.get(dstWidth, dstHeight, safeConfig)
        outBitmap.applyCanvas {
            scale(scale, scale)
            drawBitmap(inBitmap, 0f, 0f, paint)
        }
        pool.put(inBitmap)

        return outBitmap
    }

    @Throws(IOException::class)
    override suspend fun fetch(
        pool: BitmapPool,
        data: Uri,
        size: Size,
        options: Options
    ): FetchResult {
        val out: Bitmap?
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            val width: Int
            val height: Int
            when (size) {
                is PixelSize -> {
                    width = size.width
                    height = size.height
                }
                is OriginalSize -> {
                    width = 256
                    height = 256
                }
            }
            out = Stores.getInstance().localMedia().getThumbnail(data, width, height)
        } else {
            val contentId = data.lastPathSegment!!.toLong()
            @Content_Local val ret: Int = when {
                data.path!!.contains("videos") -> {
                    Content_Local.VIDEO
                }
                data.path!!.contains("images") -> {
                    Content_Local.PHOTO
                }
                data.path!!.contains("audios") -> {
                    Content_Local.AUDIO
                }
                else -> {
                    throw IOException("Picasso Thumb Not Support")
                }
            }
            out = Stores.getInstance().localMedia().getOldThumbnail(ret, contentId)
        }
        checkNotNull(out) { "Failed to decode Thumbnail" }
        return DrawableResult(
            drawable = normalizeBitmap(
                pool,
                out,
                size,
                options
            ).toDrawable(options.context.resources),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }
}