package appmain;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import com.localmovies.AuthenticationProvider;
import com.localmovies.KeycloakAuthenticator;
import com.localmovies.client.Client;
import com.localmovies.provider.boundary.MovieInfoFacade;
import com.localmovies.provider.data.MovieInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class HttpRequestRunnable implements Runnable {

    enum Task {
        TITLE_REQUEST, TOKEN_REFRESH
    }

    private final Client client;
    private List<MovieInfo> movieInfoList;
    private ProgressBar progressBar;
    private MovieListAdapter movieListAdapter;
    private ConcurrentMap<String, List<MovieInfo>> movieInfoCache;
    private final Task task;
    private final MovieInfoFacade movieInfoFacade = new MovieInfoFacade();
    private final AuthenticationProvider authenticationProvider = new KeycloakAuthenticator();
    private final Logger logger = Logger.getLogger("HttpRequestRunnable");
    private final Handler UIHandler = new Handler(Looper.getMainLooper());

    HttpRequestRunnable(ProgressBar progressBar, MovieListAdapter movieListAdapter, Client myClient,
                        List<MovieInfo> movieInfoList, Task task, ConcurrentMap<String, List<MovieInfo>> movieInfoCache){
        this.task = task;
        this.movieInfoCache = movieInfoCache;
        this.client = myClient;
        this.movieListAdapter = movieListAdapter;
        this.progressBar = progressBar;
        this.movieInfoList = movieInfoList;
    }

    public void run() {
        switch (task){
            case TITLE_REQUEST:
                dynamicallyLoadTitles();
                break;
            case TOKEN_REFRESH:
                authenticationProvider.updateAccessToken(client);
                break;
        }
    }

    private void dynamicallyLoadTitles() {
        logger.log(Level.INFO, "Dynamically loading titles");
        int itemsPerPage = 30;
        UIHandler.post(()-> progressBar.setVisibility(View.VISIBLE));
        try {
            movieInfoList.clear();

            List<MovieInfo> movieInfos = new ArrayList<>();
            int i = 0;
            do{
                List<MovieInfo> infoList = movieInfoFacade.getMovieInfo(client, i, itemsPerPage);
                movieInfoList.addAll(infoList);
                movieInfos.addAll(infoList);
                UIHandler.post(movieListAdapter::notifyDataSetChanged);
                i++;
            } while (i <= (client.getMovieCount() / itemsPerPage));

            UIHandler.post(()-> progressBar.setVisibility(View.GONE));
            movieInfoCache.putIfAbsent(client.getCurrentPath().toString(), movieInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
