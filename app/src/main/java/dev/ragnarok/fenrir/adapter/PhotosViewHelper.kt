package dev.ragnarok.fenrir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import coil.clear
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder.OnAttachmentsActionCallback
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoSize
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.AspectRatioImageView
import dev.ragnarok.fenrir.view.mozaik.MozaikLayout
import dev.ragnarok.fenrir.view.zoomhelper.ZoomHelper.Companion.addZoomableView
import java.util.*

class PhotosViewHelper internal constructor(
    private val context: Context,
    private val attachmentsActionCallback: OnAttachmentsActionCallback
) {
    @PhotoSize
    private val mPhotoPreviewSize = Settings.get().main().prefPreviewImageSize
    private val mIconColorActive = CurrentTheme.getColorPrimary(context)
    private val isUseCoil = Settings.get().other().isUse_coil

    @SuppressLint("SetTextI18n")
    fun displayVideos(videos: List<PostImage>, container: ViewGroup) {
        container.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE
        if (videos.isEmpty()) {
            return
        }
        val i = videos.size - container.childCount
        for (j in 0 until i) {
            val root = LayoutInflater.from(context)
                .inflate(R.layout.item_video_attachment, container, false)
            val holder = VideoHolder(root)
            root.tag = holder
            Utils.setColorFilter(holder.ivPlay.background, mIconColorActive)
            container.addView(root)
        }
        for (g in 0 until container.childCount) {
            val tmpV = container.getChildAt(g)
            val holder = tmpV.tag as VideoHolder
            if (g < videos.size) {
                val image = videos[g]
                holder.vgPhoto.setOnClickListener {
                    if (image.type == PostImage.TYPE_VIDEO) {
                        val video = image.attachment as Video
                        attachmentsActionCallback.onVideoPlay(video)
                    }
                }
                val url = image.getPreviewUrl(mPhotoPreviewSize)
                if (image.type == PostImage.TYPE_VIDEO) {
                    val video = image.attachment as Video
                    holder.tvDelay.text = AppTextUtils.getDurationString(video.duration)
                    holder.tvTitle.text = Utils.firstNonEmptyString(video.title, " ")
                }
                if (Utils.nonEmpty(url)) {
                    PicassoInstance.with()
                        .load(url)
                        .placeholder(R.drawable.background_gray)
                        .tag(Constants.PICASSO_TAG)
                        .into(holder.vgPhoto)
                    tmpV.visibility = View.VISIBLE
                } else {
                    PicassoInstance.with().cancelRequest(holder.vgPhoto)
                    tmpV.visibility = View.GONE
                }
            } else {
                tmpV.visibility = View.GONE
            }
        }
    }

    fun displayPhotos(photos: List<PostImage>, container: ViewGroup) {
        container.visibility = if (photos.isEmpty()) View.GONE else View.VISIBLE
        if (photos.isEmpty()) {
            return
        }
        val roundedMode = Settings.get().main().photoRoundMode
        if (roundedMode == 1) container.removeAllViews()
        val i = photos.size - container.childCount
        for (j in 0 until i) {
            val root: View = when (roundedMode) {
                1 -> if (photos.size > 1) LayoutInflater.from(context).inflate(
                    R.layout.item_photo_gif_not_round,
                    container,
                    false
                ) else LayoutInflater.from(context)
                    .inflate(R.layout.item_photo_gif, container, false)
                2 -> LayoutInflater.from(context)
                    .inflate(R.layout.item_photo_gif_not_round, container, false)
                else -> LayoutInflater.from(context)
                    .inflate(R.layout.item_photo_gif, container, false)
            }
            val holder = Holder(root)
            root.tag = holder
            container.addView(root)
            addZoomableView(root, holder)
        }
        if (container is MozaikLayout) {
            if (photos.size > 10) {
                val images = ArrayList<PostImage>(10)
                for (s in 0..9) images.add(photos[s])
                container.setPhotos(images)
            } else container.setPhotos(photos)
        }
        for (g in 0 until container.childCount) {
            val tmpV = container.getChildAt(g)
            val holder = tmpV.tag as Holder
            if (g < photos.size) {
                val image = photos[g]
                if (isUseCoil) {
                    holder.ivPlay.visibility = View.GONE
                } else {
                    holder.ivPlay.visibility =
                        if (image.type == PostImage.TYPE_IMAGE) View.GONE else View.VISIBLE
                    if (image.type != PostImage.TYPE_IMAGE) Utils.setColorFilter(
                        holder.ivPlay.background,
                        mIconColorActive
                    )
                }
                holder.tvTitle.visibility =
                    if (image.type == PostImage.TYPE_IMAGE) View.GONE else View.VISIBLE
                holder.vgPhoto.setOnClickListener {
                    when (image.type) {
                        PostImage.TYPE_IMAGE -> openImages(photos, g)
                        PostImage.TYPE_VIDEO -> {
                            val video = image.attachment as Video
                            attachmentsActionCallback.onVideoPlay(video)
                        }
                        PostImage.TYPE_GIF -> {
                            val document = image.attachment as Document
                            attachmentsActionCallback.onDocPreviewOpen(document)
                        }
                    }
                }
                val url = image.getPreviewUrl(mPhotoPreviewSize)
                when (image.type) {
                    PostImage.TYPE_VIDEO -> {
                        val video = image.attachment as Video
                        holder.tvTitle.text = AppTextUtils.getDurationString(video.duration)
                    }
                    PostImage.TYPE_GIF -> {
                        val document = image.attachment as Document
                        holder.tvTitle.text = context.getString(
                            R.string.gif,
                            AppTextUtils.getSizeString(document.size)
                        )
                    }
                }
                if (isUseCoil) {
                    if (image.type == PostImage.TYPE_GIF) {
                        if (Utils.nonEmpty(url)) {
                            holder.vgPhoto.load((image.attachment as Document).url) {
                                listener(
                                    onError = { _, _ ->
                                        run {
                                            Utils.setColorFilter(
                                                holder.ivPlay.background,
                                                mIconColorActive
                                            )
                                            holder.ivPlay.visibility = View.VISIBLE
                                            if (!Utils.isEmpty(url)) {
                                                holder.vgPhoto.load(url) {
                                                    crossfade(true)
                                                    placeholder(R.drawable.background_gray)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            holder.vgPhoto.clear()
                        }
                    } else {
                        if (Utils.nonEmpty(url)) {
                            holder.vgPhoto.load(url) {
                                crossfade(true)
                                placeholder(R.drawable.background_gray)
                            }
                            tmpV.visibility = View.VISIBLE
                        } else {
                            holder.vgPhoto.clear()
                            tmpV.visibility = View.GONE
                        }
                    }
                } else {
                    if (Utils.nonEmpty(url)) {
                        PicassoInstance.with()
                            .load(url)
                            .placeholder(R.drawable.background_gray)
                            .tag(Constants.PICASSO_TAG)
                            .into(holder.vgPhoto)
                        tmpV.visibility = View.VISIBLE
                    } else {
                        PicassoInstance.with().cancelRequest(holder.vgPhoto)
                        tmpV.visibility = View.GONE
                    }
                }
            } else {
                tmpV.visibility = View.GONE
            }
        }
    }

    private fun openImages(photos: List<PostImage>, index: Int) {
        val models = ArrayList<Photo>()
        for (postImage in photos) {
            if (postImage.type == PostImage.TYPE_IMAGE) {
                models.add(postImage.attachment as Photo)
            }
        }
        attachmentsActionCallback.onPhotosOpen(models, index, true)
    }

    private class Holder(itemView: View) {
        val vgPhoto: ShapeableImageView = itemView.findViewById(R.id.item_video_image)
        val ivPlay: ImageView = itemView.findViewById(R.id.item_video_play)
        val tvTitle: TextView = itemView.findViewById(R.id.item_video_title)

    }

    private class VideoHolder(itemView: View) {
        val vgPhoto: AspectRatioImageView = itemView.findViewById(R.id.item_video_album_image)
        val ivPlay: ImageView = itemView.findViewById(R.id.item_video_play)
        val tvTitle: TextView = itemView.findViewById(R.id.item_video_album_title)
        val tvDelay: TextView = itemView.findViewById(R.id.item_video_album_count)

    }

}
