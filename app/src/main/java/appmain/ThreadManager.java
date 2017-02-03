package appmain;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.phoneinfo.Phone;
import com.rahmnathan.MovieInfo;
import com.restclient.RestClient;

import java.util.Collections;
import java.util.List;

public class ThreadManager extends Thread {

    public enum SERVER_CALL {
        GET_TITLES, REFRESH
    }

    private final SERVER_CALL request;
    private final String title;
    private final Phone phone;
    private final Handler UIHandler = new Handler(Looper.getMainLooper());
    private final RestClient REST_CLIENT = new RestClient();

    public ThreadManager(SERVER_CALL request, String title){
        this.request = request;
        this.title = title;
        this.phone = MainActivity.myPhone;
    }

    public void run(){
        switch(request){
            case GET_TITLES:
                MainActivity.myPhone.setCurrentPath(phone.getCurrentPath() + title + "/");
                updateListView();
                break;
            case REFRESH:
                MainActivity.movieInfo.invalidateAll();
                REST_CLIENT.refresh(phone);
                MainActivity.myPhone.setCurrentPath(MainActivity.myPhone.getMainPath() + "Movies/");
                break;
        }
    }

    private void updateListView() {

        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.progressBar.setVisibility(View.VISIBLE);
            }
        });

        MainActivity.MOVIE_INFO_LIST.clear();
        try {
            List<MovieInfo> infoList = MainActivity.movieInfo.get(MainActivity.myPhone.getCurrentPath());
            Collections.sort(infoList, MovieInfo.Builder.newInstance().build());
            MainActivity.MOVIE_INFO_LIST.addAll(infoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.progressBar.setVisibility(View.GONE);
                MainActivity.movieListAdapter.notifyDataSetChanged();
            }
        });
    }
}