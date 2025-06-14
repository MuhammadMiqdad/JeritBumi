package com.dicoding.picodiploma.loginwithanimation.view.user.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import android.os.Handler
import android.os.Looper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var indicators: List<View>
    private lateinit var slides: List<SlideItem>
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        viewPager = binding.viewPagerSlideshow
        indicators = listOf(
            binding.indicator1,
            binding.indicator2,
            binding.indicator3,
            binding.indicator4,
            binding.indicator5
        )

        // Initialize ImageView
        imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Clear container and add ImageView
        binding.slideshowContainer.removeAllViews()
        binding.slideshowContainer.addView(imageView, 0)
        binding.slideshowContainer.addView(binding.viewPagerSlideshow)

        // Set up slides
        setupSlideshow()

        // Set up card click listeners
        setupCardClickListeners()
    }

    private fun setupCardClickListeners() {
        binding.cardViewSisaMakanan.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_foodWasteFragment)
        }

        binding.cardViewKertas.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_paperWasteFragment)
        }

        binding.cardViewPlastik.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_plasticWasteFragment)
        }

        binding.cardViewLogam.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_metalWasteFragment)
        }

        binding.cardViewB3.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_hazardousWasteFragment)
        }

        binding.cardViewKaca.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_glassWasteFragment)
        }
    }

    // Handler for auto-sliding
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        viewPager.currentItem = (viewPager.currentItem + 1) % slides.size
    }

    private fun setupSlideshow() {
        // Create slide items - only image resource needed now
        slides = listOf(
            SlideItem(R.drawable.slide_image1, ""),
            SlideItem(R.drawable.slide_image2, ""),
            SlideItem(R.drawable.slide_image3, ""),
            SlideItem(R.drawable.slide_image4, ""),
            SlideItem(R.drawable.slide_image5, "")
        )

        // Set initial slide
        if (slides.isNotEmpty()) {
            updateSlide(0)
        }

        // Set up custom adapter
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val v = View(context)
                v.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {}
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                // ViewPager2 will recycle views, but we handle displaying content outside of the adapter
            }

            override fun getItemCount(): Int = slides.size
        }

        viewPager.adapter = adapter

        // Set up page change callback
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateSlide(position)

                // Reset auto-slide timer when page is manually changed
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })

        // Start auto-sliding
        startAutoSlide()
    }

    private fun startAutoSlide() {
        sliderHandler.postDelayed(sliderRunnable, 3000) // Change slides every 3 seconds
    }

    // Lifecycle methods to manage the slideshow
    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        startAutoSlide()
    }

    private fun updateSlide(position: Int) {
        // Update image only
        imageView.setImageResource(slides[position].imageResId)

        // Update indicators
        updateIndicators(position)
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, view ->
            if (index == position) {
                view.background = resources.getDrawable(R.drawable.shape_indicator_active, null)
            } else {
                view.background = resources.getDrawable(R.drawable.shape_indicator_inactive, null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}