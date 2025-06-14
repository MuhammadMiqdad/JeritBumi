package com.dicoding.picodiploma.loginwithanimation.view.story

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem

class StoryDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

    private lateinit var ivDetailPhoto: ImageView
    private lateinit var tvDetailName: TextView
    private lateinit var tvDetailDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        // Initialize views
        ivDetailPhoto = findViewById(R.id.iv_detail_photo)
        tvDetailName = findViewById(R.id.tv_detail_name)
        tvDetailDescription = findViewById(R.id.tv_detail_description)

        // Retrieve the ListStoryItem object from the intent
        val story = intent.getSerializableExtra(EXTRA_STORY) as? ListStoryItem

        story?.let {
            // Set the values to the views
            tvDetailName.text = it.name
            tvDetailDescription.text = it.description

            // Load image using Glide
            Glide.with(this)
                .load(it.photoUrl)
                .into(ivDetailPhoto)
        }
    }
}
