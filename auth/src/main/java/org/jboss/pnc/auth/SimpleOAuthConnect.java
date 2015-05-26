package org.jboss.pnc.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class SimpleOAuthConnect {
    
    public static String getAccessToken(String url, String clientId, String username, String password) 
            throws ClientProtocolException, IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        // add header
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List <BasicNameValuePair> urlParameters = new ArrayList <BasicNameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                if(line.contains("refresh_token")) {
                    String[] respContent = line.split(",");
                    for (int i = 0; i < respContent.length; i++) {
                        String split = respContent[i];
                        if(split.contains("refresh_token")) {
                            String refreshToken = split.split(":")[1].substring(1,split.split(":")[1].length() -1);
                        }
                        if(split.contains("access_token")) {
                            String accessToken = split.split(":")[1].substring(1,split.split(":")[1].length() -1);
                            return accessToken;
                        }
                    }
                }
            }

        } finally {
            response.close();
        }
        return null;

    }
    
    public static void main(String[] args) {
        try {
            System.out.println(">>> Access token: " + SimpleOAuthConnect
                    .getAccessToken(args[0], args[1], args[2], args[3]));
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
