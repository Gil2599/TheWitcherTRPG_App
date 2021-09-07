package com.example.thewitcherrpg.characterSheet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.example.thewitcherrpg.characterSheet.charFrags.ViewPagerAdapter
import com.example.thewitcherrpg.databinding.FragmentCharBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.graphics.Bitmap

import android.content.ContextWrapper
import java.lang.Exception
import android.annotation.SuppressLint

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import java.io.*


class CharFragment : Fragment() {
    private var _bindind: FragmentCharBinding? = null
    private val binding get() = _bindind!!
    private lateinit var tabAdapter: ViewPagerAdapter

    private val sharedViewModel: SharedViewModel by activityViewModels()

    @SuppressLint("NewApi")
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
            uri: Uri? -> binding.imageView.setImageURI(uri)
        try {
            val bitmap: Bitmap = when {
                Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver, uri
                )
                else -> {
                    val source = ImageDecoder.createSource(requireActivity().contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                }
            }
            sharedViewModel.setImagePath(saveToInternalStorage(bitmap).toString())
        }
        catch (e: NullPointerException){
            Toast.makeText(context, "Action Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _bindind = FragmentCharBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel

        val imagePath = sharedViewModel.image.value.toString()

        if (imagePath.isNotEmpty()){
            loadImageFromStorage(imagePath)
        }
        else Toast.makeText(context, "Image not found", Toast.LENGTH_SHORT).show()

        binding.imageView.setOnClickListener(){
            getContent.launch("image/*")
        }

        val numTabs = 5
        val tabTitles = listOf<String>("Quick Stats", "Profession", "Armor", "Equipment", "Profession")

        tabAdapter = ViewPagerAdapter(childFragmentManager, lifecycle, numTabs)

        val viewPager = binding.viewPager
        viewPager.apply{adapter = tabAdapter}

        val tabs: TabLayout = binding.CharacterTabs

        TabLayoutMediator(tabs, viewPager,true) {tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        return view

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(activity?.applicationContext)
        // path to /data/data/thewitherrpg/app_data/imageDir
        val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val myPath = File(directory, sharedViewModel.uniqueID.value.toString() + ".jpeg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    private fun loadImageFromStorage(path: String) {
        try {
            val f = File(path, sharedViewModel.uniqueID.value.toString() + ".jpeg")
            //Toast.makeText(context, sharedViewModel.uniqueID.toString() + ".jpeg", Toast.LENGTH_SHORT).show()
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            val img = binding.imageView
            img.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}