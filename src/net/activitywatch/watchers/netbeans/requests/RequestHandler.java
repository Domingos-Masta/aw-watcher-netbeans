/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.requests;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import net.activitywatch.watchers.netbeans.util.Consts;
import net.activitywatch.watchers.netbeans.util.Util;

/**
 *
 * @author domingos.fernando
 */
public class RequestHandler
{

    public static final String AW_WATCHER_URL = "http://localhost:";
    public static String AW_PROD_PORT = "5600";
    public boolean IS_TESTING = false;
    private static final String USER_AGENT = "Mozilla/5.0";

    public static HashMap<String, String> postRquest(Object badyData, String endPoint, boolean isTesting) throws IOException
    {
        String port = isTesting ? "5666" : AW_PROD_PORT;
        URL url = new URL(AW_WATCHER_URL + port + endPoint);
        StringBuilder response = null;

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");

        con.setDoOutput(true);
        String jsonInputString = javaObjToJSON(badyData);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        String reponseMessage = con.getResponseMessage();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            Util.info(response.toString());
        }

        HashMap<String, String> responseHash = new HashMap<>();
        responseHash.put(Consts.RESP_CODE, code + "");
        responseHash.put(Consts.RESP_MESSAGE, reponseMessage);
        responseHash.put(Consts.RESPONSE, response.toString());

        return responseHash;

    }

    public static HashMap<String, String> getRequest(String endPoint, boolean isTesting) throws IOException
    {
        String port = isTesting ? "5666" : AW_PROD_PORT;
        URL url = new URL(AW_WATCHER_URL + port + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        String responseCode = con.getResponseCode();
        ActivityWatch.info("Query response: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            ActivityWatch.info("Query response: " + response.toString());
            responseHash = new HashMap<>();
            responseHash.put(Consts.RESP_CODE, code + "");
            responseHash.put(Consts.RESP_MESSAGE, reponseMessage);
            responseHash.put(Consts.RESPONSE, response.toString());

        }
        else {
            ActivityWatch.info("GET request not worked");
            responseHash.put(Consts.RESP_CODE, code + "");
            responseHash.put(Consts.RESP_MESSAGE, reponseMessage);
            responseHash.put(Consts.RESPONSE, response.toString());Ï
        }

        return responseHash;

    }

    public static String javaObjToJSON(Object entity)
    {
        Gson mapper = new Gson();
        try {
            return mapper.toJson(entity);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
