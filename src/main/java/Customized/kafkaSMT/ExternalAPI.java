/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Customized.kafkaSMT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Administrator
 */
public class ExternalAPI {
    public static String MyGETRequest() throws IOException {
        try{
            
            //Call tokenization API here.
            URL urlForGetRequest = new URL("https://jsonplaceholder.typicode.com/posts/1");
            String readLine = null;
            HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
            conection.setRequestMethod("GET");
            conection.setRequestProperty("userId", "1"); // set userId its a sample here
            int responseCode = conection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conection.getInputStream()))) {
                    response = new StringBuilder();
                    while ((readLine = in .readLine()) != null) {
                        response.append(readLine);
                    }
                }
                // print result
                System.out.println("JSON String Result " + response.toString());
                //GetAndPost.POSTRequest(response.toString());
                return response.toString();
            } else {
                System.out.println("GET NOT WORKED");
                return "No string";
            }
        }
        catch (SecurityException | IOException e) {  
                return "Error caught";
            }
    }
}
