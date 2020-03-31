package com.github.rahmnathan.localmovies.app.control

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.github.rahmnathan.localmovies.app.activity.SetupActivity.Companion.saveData
import com.github.rahmnathan.localmovies.app.adapter.external.localmovie.MediaFacade
import com.github.rahmnathan.localmovies.app.adapter.list.MediaListAdapter
import com.github.rahmnathan.localmovies.app.control.MediaPathUtils.getParentPath
import com.github.rahmnathan.localmovies.app.data.Client
import com.github.rahmnathan.localmovies.app.data.MovieEvent
import com.github.rahmnathan.localmovies.app.persistence.media.MediaPersistenceService
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

class MediaEventLoader(private val mediaListAdapter: MediaListAdapter,
                       private val client: Client,
                       private val mediaFacade: MediaFacade,
                       private val persistenceService: MediaPersistenceService,
                       private val context: Context) : Runnable {
    private val logger = Logger.getLogger(MediaEventLoader::class.java.name)
    private val UIHandler = Handler(Looper.getMainLooper())

    override fun run() {
        logger.log(Level.INFO, "Dynamically loading events.")
        if (client.accessToken == null) {
            UIHandler.post { Toast.makeText(context, "Login failed - Check credentials", Toast.LENGTH_LONG).show() }
            return
        }
        val count = mediaFacade.getMovieEventCount()
        if (!count.isPresent) {
            logger.severe("Error retrieving media event count.")
            return
        }

        for (page in 0..count.get() / ITEMS_PER_PAGE) {
            val events = mediaFacade.getMovieEvents(page.toInt(), ITEMS_PER_PAGE)
            events.forEach(Consumer { event: MovieEvent ->
                logger.info("Found media event: $event")
                if (event.event.equals("CREATE", ignoreCase = true)) {
                    val media = event.media
                    persistenceService.deleteMovie(event.relativePath)
                    persistenceService.addOne(getParentPath(event.relativePath), media!!)
                    mediaListAdapter.clearLists()
                    mediaListAdapter.updateList(persistenceService.getMoviesAtPath(client.currentPath.toString()).orElse(ArrayList()))
                    UIHandler.post { mediaListAdapter.notifyDataSetChanged() }
                } else {
                    persistenceService.deleteMovie(event.relativePath)
                    mediaListAdapter.clearLists()
                    mediaListAdapter.updateList(persistenceService.getMoviesAtPath(client.currentPath.toString()).orElse(ArrayList()))
                    UIHandler.post { mediaListAdapter.notifyDataSetChanged() }
                }
            })
        }

        if (mediaListAdapter.chars != "") {
            UIHandler.post { mediaListAdapter.filter.filter(mediaListAdapter.chars) }
        }

        client.lastUpdate = System.currentTimeMillis()
        saveData(client, context)
    }

    companion object {
        private const val ITEMS_PER_PAGE = 30
    }

}