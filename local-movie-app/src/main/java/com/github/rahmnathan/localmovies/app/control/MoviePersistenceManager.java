package com.github.rahmnathan.localmovies.app.control;

import android.content.Context;

import com.github.rahmnathan.localmovies.client.LocalMediaPath;
import com.github.rahmnathan.localmovies.info.provider.data.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MoviePersistenceManager {
    private final Logger logger = Logger.getLogger(MoviePersistenceManager.class.getName());
    private final ConcurrentMap<String, List<Movie>> movieInfoCache;
    private MovieDAO movieDAO;

    public MoviePersistenceManager(ConcurrentMap<String, List<Movie>> movieInfoCache, Context context, ExecutorService executorService) {
        this.movieInfoCache = movieInfoCache;

        CompletableFuture.runAsync(() -> {
            MovieDatabase db = MovieDatabase.getDatabase(context);
            movieDAO = db.movieDAO();

            List<MovieEntity> movieEntities = movieDAO.getAll();

            movieEntities.forEach(movieEntity -> {
                logger.info("Loading MovieEntities into memory - Path: " + movieEntity.getDirectoryPath() + " Filename: " + movieEntity.getMovie().getFilename());
                List<Movie> movies = movieInfoCache.getOrDefault(movieEntity.getDirectoryPath(), new ArrayList<>());
                movies.add(movieEntity.getMovie());
                movieInfoCache.putIfAbsent(movieEntity.getDirectoryPath(), movies);
            });

        }, executorService);
    }

    public boolean contains(String key){
        return movieInfoCache.containsKey(key);
    }

    public void addAll(String path, List<Movie> movies){
        movieInfoCache.putIfAbsent(path, movies);
        logger.info("Adding movielistentities to database: " + path);
        List<MovieEntity> movieEntities = movies.stream().map(movie -> new MovieEntity(path, movie)).collect(Collectors.toList());
        movieDAO.insertAll(movieEntities);
    }

    public List<Movie> getMoviesAtPath(String path){
        return movieInfoCache.getOrDefault(path, new ArrayList<>());
    }

    public void deleteMovie(String path){
        String parentPath = getParentPath(path);
        String filename = getFilename(path);

        List<Movie> movies = movieInfoCache.getOrDefault(parentPath, new ArrayList<>());
        movies.removeIf(movie -> movie.getFilename().equalsIgnoreCase(filename));
        MovieEntity entity = movieDAO.getByPathAndFilename(parentPath, filename);

        if(entity != null)
            movieDAO.delete(entity);
    }

    private static String getParentPath(String path){
        String[] directoryList = path.split("/");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < directoryList.length - 1; i++){
            sb.append(directoryList[i]).append("/");
        }

        return sb.toString();
    }

    private static String getFilename(String path){
        String[] directoryList = path.split("/");
        return directoryList[directoryList.length - 1];
    }


//
//    public void addMovie(Movie movie){
//        LocalMediaPath mediaPath = new LocalMediaPath();
//        mediaPath.addAll(Arrays.asList(movie.getPath().split("/")));
//        mediaPath.remove();
//        List<Movie> movies = movieInfoCache.getOrDefault(mediaPath.toString(), new ArrayList<>());
//        movies.add(movie);
//        movieDAO.insert(new MovieEntity(mediaPath.toString(), movies));
//    }
}
