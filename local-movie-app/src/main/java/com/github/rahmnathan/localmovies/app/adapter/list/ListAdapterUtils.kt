package com.github.rahmnathan.localmovies.app.adapter.list

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.github.rahmnathan.localmovies.app.data.Media
import com.github.rahmnathan.localmovies.app.data.MovieOrder
import rahmnathan.localmovies.R
import java.util.*

object ListAdapterUtils {

    @JvmStatic
    fun sort(mediaList: List<Media>, order: MovieOrder?) {
        when (order) {
            MovieOrder.DATE_ADDED -> mediaList.sortedByDescending { media -> media.created }
            MovieOrder.RATING -> mediaList.sortedByDescending { media -> media.imdbRating }
            MovieOrder.RELEASE_YEAR -> mediaList.sortedByDescending { media -> media.releaseYear }
            MovieOrder.TITLE -> mediaList.sortedBy { media -> media.title }
        }
    }

    @JvmStatic
    fun mapTitleToView(title: String?, titleView: TextView, fontSize: Int) {
        titleView.text = title
        titleView.textSize = fontSize.toFloat()
        titleView.gravity = Gravity.CENTER
        titleView.setTextColor(Color.WHITE)
    }

    @JvmStatic
    fun mapRatingsToView(imdbRating: String?, metaRating: String?, ratings: TextView) {
        ratings.gravity = Gravity.CENTER
        ratings.setTextColor(Color.WHITE)
        ratings.textSize = 12f
        ratings.text = String.format("IMDB: %s Meta: %s", imdbRating, metaRating)
    }

    @JvmStatic
    fun mapImageToView(base64Image: String?, imageView: ImageView) {
        if (base64Image != null && base64Image != "" && base64Image != "null") {
            val image = Base64.decode(base64Image, Base64.DEFAULT)
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
        } else {
            imageView.setImageResource(R.mipmap.no_poster)
        }
    }

    @JvmStatic
    fun mapYearToView(releaseYear: String?, year: TextView, fontSize: Int) {
        year.text = String.format("Release Year: %s", releaseYear)
        year.setTextColor(Color.WHITE)
        year.gravity = Gravity.CENTER
        year.textSize = fontSize.toFloat()
    }
}