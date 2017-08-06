package com.github.rahmnathan.localmovies;

import com.github.rahmnathan.localmovies.client.Client;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KeycloakAuthenticator implements Runnable {
    private final Logger logger = Logger.getLogger(KeycloakAuthenticator.class.getName());
    private final Client client;

    public KeycloakAuthenticator(Client client){
        this.client = client;
    }

    public void run(){
        updateAccessToken();
    }

    void updateAccessToken(){
        String urlString = client.getComputerUrl() + "/auth/realms/LocalMovies/protocol/openid-connect/token";

        byte[] loginInfo = buildLoginInfo(client);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(loginInfo.length);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setConnectTimeout(5000);
            connection.connect();
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(loginInfo);
            wr.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            br.lines().forEachOrdered(result::append);
            br.close();
            connection.disconnect();
            client.setAccessToken(new JSONObject(result.toString()).getString("access_token"));
        } catch (Exception e){
            logger.info(e.toString());
        }
    }

    private byte[] buildLoginInfo(Client client){
        Map<String, String> args = new HashMap<>();
        args.put("grant_type", "password");
        args.put("client_id", "movielogin");
        args.put("username", client.getUserName());
        args.put("password", client.getPassword());
        StringBuilder sb = new StringBuilder();
        args.entrySet().forEach(entry -> {
            try {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e){
                logger.info(e.toString());
            }
        });

        return  sb.toString().substring(0, sb.length()-1).getBytes();
    }
}