package com.example.rtmpexoplayerstream

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.exoplayer2.*

import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource

import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory


import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource

import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

import com.google.android.exoplayer2.trackselection.TrackSelection

import com.google.android.exoplayer2.trackselection.TrackSelector

import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView

import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource

import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


import java.util.logging.Logger


class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var constraintLayoutRoot: ConstraintLayout
    private lateinit var exoPlayerView: PlayerView
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var urlType: URLType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        initializePlayer()
//        playVideo()
//        player.playWhenReady = true
//        player.prepare()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        findView()
        initPlayer()
    }

    private fun findView() {
        constraintLayoutRoot = findViewById(R.id.constraintLayoutRoot)
        exoPlayerView = findViewById(R.id.videoView)
    }

    private fun initPlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()

        simpleExoPlayer.addListener(playerListener)

        exoPlayerView.player = simpleExoPlayer

        createMediaSource()
        simpleExoPlayer.setMediaSource(mediaSource)
        simpleExoPlayer.prepare()
    }

    private fun createMediaSource() {
//        urlType = URLType.MP4
//        urlType.url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"

        urlType = URLType.HLS
        urlType.url = "http://***.***.*.***:****/hls/test.m3u8"

        simpleExoPlayer.seekTo(0)

        when (urlType) {
            URLType.MP4 -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    this,
                    Util.getUserAgent(this, applicationInfo.name)
                )

                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }

            URLType.HLS -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    this,
                    Util.getUserAgent(this, applicationInfo.name)
                )

                mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val constraintSet = ConstraintSet()
        constraintSet.connect(
            exoPlayerView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            exoPlayerView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            exoPlayerView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constraintSet.connect(
            exoPlayerView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )

        constraintSet.applyTo(constraintLayoutRoot)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            showSystemUI()
            val layoutParams = exoPlayerView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "16:9"
        }

        window.decorView.requestLayout()
    }


    private  fun hideSystemUI(){
        actionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    private  fun showSystemUI(){
        actionBar?.show()

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.play()
    }

    override fun onPause() {
        super.onPause()

        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false

    }

    override fun onStop() {
        super.onStop()

        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false

    }

    override fun onDestroy() {
        super.onDestroy()

        simpleExoPlayer.removeListener(playerListener)
        simpleExoPlayer.stop()
        simpleExoPlayer.clearMediaItems()

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private var playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            if(urlType == URLType.HLS) {
                exoPlayerView.useController = false
            }

            if (urlType == URLType.MP4){
                exoPlayerView.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@MainActivity, "${error.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class URLType(var url: String) {
    MP4(""), HLS("")
}