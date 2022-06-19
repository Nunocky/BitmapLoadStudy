package com.example.bitmaploadstudy

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
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