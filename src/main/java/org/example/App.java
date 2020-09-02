package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class App {
    public static String loginURI = "http://0.0.0.0:5002/login";
    public static String getURI = "http://0.0.0.0:5002/";
    public static String protectedURI = "http://0.0.0.0:5002/protected";
    public static String refreshTokenURI = "http://0.0.0.0:5002/refresh";
    public static String itemsURI = "http://localhost:5002/items";
    public static void main(String[] args)  throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        getRequest();

        UserTokens userTokens;
        userTokens = objectMapper.readValue(login(),
                UserTokens.class);
        System.out.println("access token =     " + objectMapper.writeValueAsString(userTokens.access_token));

        protectedRequest(userTokens.access_token);
        JsonNode jsonNode =  objectMapper.readTree(refreshAccessToken(userTokens.refresh_token));
        String newAccesToken = jsonNode.get("access_token").asText();
        userTokens.access_token = newAccesToken;
        System.out.println("access token" +userTokens.access_token);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getItems(userTokens.access_token);
    }

    public static void getRequest() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(getURI);
            try (CloseableHttpResponse response = httpClient.execute(request)) {

                // Get HttpResponse Status
                System.out.println(response.getProtocolVersion());              // HTTP/1.1
                System.out.println(response.getStatusLine().getStatusCode());   // 200
                System.out.println(response.getStatusLine().getReasonPhrase()); // OK
                System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    System.out.println(result);
                }

            }
        }
//test
    }
    public static String login() throws IOException {
        String result;
        HttpPost post;
        post = new HttpPost(loginURI);
        post.addHeader("content-type","application/json");
        // send a JSON data
        String json = "{" +
                "\"username\":\"test\"," +
                "\"password\":\"test\"" +
                "}";
        post.setEntity(new StringEntity(json));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            result = EntityUtils.toString(response.getEntity());
        }
        return result;
    }
    public static void protectedRequest (String accessToken) throws IOException{
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(protectedURI);
            request.addHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                }

            }
        }
    }
    public static String refreshAccessToken(String refreshToken) throws IOException {
        String newToken;
        HttpPost request;
        request = new HttpPost(refreshTokenURI);
        request.addHeader("Authorization", "Bearer " + refreshToken);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            newToken = EntityUtils.toString(response.getEntity());
            System.out.println("new token = " + newToken);
        }
        return newToken;
    }
    public static void getItems (String accessToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getItemsRequest = new HttpGet(itemsURI);
            getItemsRequest.addHeader("Authorization","Bearer " + accessToken);
            try (CloseableHttpResponse response = httpClient.execute(getItemsRequest)) {

                // Get HttpResponse Status
                System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    System.out.println(result);
                }
            }
        }
    }

}

