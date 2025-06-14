package com.dicoding.picodiploma.loginwithanimation.view.user.ui.waste

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.FragmentPlasticWasteDetailBinding

class PlasticWasteFragment : Fragment() {

    private var _binding: FragmentPlasticWasteDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlasticWasteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            textViewWasteTitle.text = "Plastik"
            textViewWasteDescription.text = resources.getString(R.string.plastic_waste_description)
            textViewWasteProcessing.text = resources.getString(R.string.plastic_waste_processing)
            imageViewWaste.setImageResource(R.drawable.img_plastic_waste)
            cardViewWaste.setCardBackgroundColor(resources.getColor(R.color.plastic_waste_color))

            // Set text color based on background brightness
            val color = resources.getColor(R.color.plastic_waste_color)
            val brightness = (android.graphics.Color.red(color) * 299 +
                    android.graphics.Color.green(color) * 587 +
                    android.graphics.Color.blue(color) * 114) / 1000

            if (brightness >= 128) {
                textViewWasteDescription.setTextColor(android.graphics.Color.BLACK)
                textViewWasteTitle.setTextColor(android.graphics.Color.BLACK)
            } else {
                textViewWasteDescription.setTextColor(android.graphics.Color.WHITE)
                textViewWasteTitle.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}