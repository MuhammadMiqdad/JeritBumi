package com.dicoding.picodiploma.loginwithanimation.view.story

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.view.adapter.StoryAdapter

class StoryActivity : AppCompatActivity() {

    private lateinit var rvStories: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val storyViewModel: StoryViewModel by viewModels { StoryViewModelFactory.getInstance(this) }

    private val storyAdapter: StoryAdapter by lazy {
        StoryAdapter { story ->
            val intent = Intent(this, StoryDetailActivity::class.java).apply {
                putExtra(StoryDetailActivity.EXTRA_STORY, story)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        rvStories = findViewById(R.id.rv_stories)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        setupObservers()
        storyViewModel.getStories()
    }

    private fun setupRecyclerView() {
        rvStories.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = storyAdapter
        }
    }

    private fun setupObservers() {
        storyViewModel.listStory.observe(this) { stories ->
            storyAdapter.submitList(stories)
        }

        storyViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        storyViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
