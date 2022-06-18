package com.example.bitmaploadstudy

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.example.bitmaploadstudy.databinding.FragmentImageViewBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ImageViewViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    val bitmap = MutableLiveData<Bitmap>()

    fun loadBitmap(uri: Uri) {
        val bmp = uri.getBitmapOrNull(getApplication<MyApplication>().contentResolver)
        bitmap.value = bmp
    }
}

@AndroidEntryPoint
class ImageViewFragment : Fragment() {
    private val args by navArgs<ImageViewFragmentArgs>()
    private lateinit var binding: FragmentImageViewBinding
    private val viewModel: ImageViewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageViewBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.loadBitmap(args.uri)

        return binding.root
    }
}


private const val MAX_IMAGE_EDGE_LENGTH = 1280

fun Uri.getBitmapOrNull(contentResolver: ContentResolver): Bitmap? {
    return kotlin.runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, this)
            ImageDecoder.decodeBitmap(
                source
            ) { decoder, imageInfo, decoderSource ->
                // Log.d("Image", "${imageInfo.size.width}, ${imageInfo.size.height}")

                if (MAX_IMAGE_EDGE_LENGTH < imageInfo.size.width ||
                    MAX_IMAGE_EDGE_LENGTH < imageInfo.size.height
                ) {
                    val ratio = imageInfo.size.width / imageInfo.size.height.toFloat()
                    val (width: Int, height: Int) = if (imageInfo.size.width > imageInfo.size.height) {
                        val width = MAX_IMAGE_EDGE_LENGTH
                        val height = MAX_IMAGE_EDGE_LENGTH / ratio
                        width.toInt() to height.toInt()
                    } else {
                        val height = MAX_IMAGE_EDGE_LENGTH
                        val width = MAX_IMAGE_EDGE_LENGTH * ratio
                        width.toInt() to height.toInt()
                    }
                    decoder.setTargetSize(width, height)
                }
            }
        } else {
            var iStream = contentResolver.openInputStream(this)
            iStream?.mark(0)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(iStream, null, options)

            iStream?.close()
            iStream = contentResolver.openInputStream(this)

            val imageWidth = options.outWidth.toFloat()
            val imageHeight = options.outHeight.toFloat()
            //val imageType: String = options.outMimeType
            //Log.d("safd", "${imageWidth}, ${imageHeight}")

            if (MAX_IMAGE_EDGE_LENGTH < imageWidth ||
                MAX_IMAGE_EDGE_LENGTH < imageHeight
            ) {
                val ratio = imageWidth / imageHeight
                val (width: Int, height: Int) =
                    if (imageWidth > imageHeight) {
                        val width = MAX_IMAGE_EDGE_LENGTH
                        val height = MAX_IMAGE_EDGE_LENGTH / ratio
                        width.toInt() to height.toInt()
                    } else {
                        val height = MAX_IMAGE_EDGE_LENGTH
                        val width = MAX_IMAGE_EDGE_LENGTH * ratio
                        width.toInt() to height.toInt()
                    }

                options.inSampleSize = calculateInSampleSize(options, width, height)
                options.inJustDecodeBounds = false
                return BitmapFactory.decodeStream(iStream, null, options)
            } else {
                return BitmapFactory.decodeStream(iStream)
            }
        }
    }.getOrNull()
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}