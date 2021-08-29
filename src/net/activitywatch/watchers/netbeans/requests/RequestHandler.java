/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.requests;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.activitywatch.watchers.netbeans.ActivityWatch;

/**
 *
 * @author domingos.fernando
 */
public class RequestHandler
{

    public static final String AW_WATCHER_URL = "http://localhost";
    public static final String AW_WATCHER_PORT = ":5600";
    private static final String USER_AGENT = "Mozilla/5.0";

    public static String postJSON(Object badyData, String endPoint) throws IOException
    {
//Change the URL with any other publicly accessible POST resource, which accepts JSON request body
        URL url = new URL(AW_WATCHER_URL + AW_WATCHER_PORT + endPoint);
        StringBuilder response = null;

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");

        con.setDoOutput(true);

        //JSON String need to be constructed for the specific resource.
        //We may construct complex JSON using any third-party JSON libraries such as jackson or org.json
//        String jsonInputString = "{\"name\": \"Upendra\", \"job\": \"Programmer\"}";
        String jsonInputString = javaObjToJSON(badyData);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();

        System.out.println(code);

        ActivityWatch.info(code
                           + "");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            ActivityWatch.info(response.toString());
        }

        return response.toString();

    }

    public static String sendGET(String endPoint) throws IOException
    {

        String url = AW_WATCHER_URL + AW_WATCHER_PORT + endPoint;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
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
            return response.toString();
        }
        else {
            ActivityWatch.info("GET request not worked");
            return "" + responseCode;
        }

    }

    public static String sendPOST(String endPoint) throws IOException
    {
        URL obj = new URL(AW_WATCHER_URL + AW_WATCHER_PORT + endPoint);;
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write("".getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        ActivityWatch.info("POST Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
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
            return response.toString();
        }
        else {
            ActivityWatch.info("POST request not worked");
            return "" + responseCode;
        }
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
