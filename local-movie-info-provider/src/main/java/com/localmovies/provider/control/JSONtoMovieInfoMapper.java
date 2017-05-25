package com.localmovies.provider.control;

import com.localmovies.provider.data.MovieInfo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

class JSONtoMovieInfoMapper {

    List<MovieInfo> jsonArrayToMovieInfoList(JSONArray jsonList) {
        List<MovieInfo> movieInfoList = new ArrayList<>();

        for(int i = 0; i<jsonList.length(); i++) {
            JSONObject object = jsonList.getJSONObject(i);
            MovieInfo.Builder builder = MovieInfo.Builder.newInstance()
                    .setReleaseYear(object.getString("releaseYear"))
                    .setMetaRating(object.getString("metaRating"))
                    .setIMDBRating(object.getString("imdbrating"))
                    .setTitle(object.getString("title"))
                    .setCreated(object.getLong("dateCreated"))
                    .setViews(object.getInt("views"));

            try {
                builder.setImage(object.getString("image"));
            } catch (Exception e){}

            movieInfoList.add(builder.build());
        }
        return movieInfoList;
    }
}
