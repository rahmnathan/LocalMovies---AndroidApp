package remote;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import activity.MainActivity;

class ViewPressRepeater extends Thread {

    private final String command;

    public ViewPressRepeater(String command){
        this.command = command;
    }

    public void run(){
        do{
            sendControl(command);
            try{
                Thread.sleep(200);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        while (Remote.repeat);
    }

    private void sendControl(String command) {

        String uri = "http://" + MainActivity.myPhone.getComputerIP() + "8080/control?control=" +
                command + "&name=" + MainActivity.myPhone.getPhoneName();

        try {
            URL url = new URL(uri);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.disconnect();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}