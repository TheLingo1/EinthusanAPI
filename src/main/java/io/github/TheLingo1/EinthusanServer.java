package io.github.TheLingo1;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


public class EinthusanServer {
    public static void main(String[] args){
        try {
            // bind server to port 8080 and have 0 queuing for requests
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(22222), 0);

            // Adding the 'search' context
            httpServer.createContext("/search", new SearchHandler());
            httpServer.createContext("/raw", new RawUrlHandler());
            httpServer.createContext("/heartbeat", new HeartBeatHandler());
            httpServer.createContext("/shutdown", new ShutDownHandler());

            httpServer.start();

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void shutdown(HttpServer server){
        server.stop(0);
    }

    // Handler for '/search' context
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) {

            System.out.println("Serving search request of method " + he.getRequestMethod());
            // Browser sends an OPTIONS request first called a pre flight that checks CORS and if Content-type header is allowed
            if (he.getRequestMethod().equalsIgnoreCase("OPTIONS")){
                System.out.println("Serving Options Request");
                try{
                    he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                    he.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin, Content-type");
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    he.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            // Make sure it's a post request
            if (he.getRequestMethod().equalsIgnoreCase("POST")){
                try{
                    // Request Headers
                    Headers requestHeaders = he.getRequestHeaders();
                    // read the header content-length to set the byte array for data
                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                    // Request body
                    InputStream is = he.getRequestBody();

                    byte[] data = new byte[contentLength];
                    // reads data into data buffer and returns number of bytes read
                    int datalen = is.read(data);
                    if (datalen != contentLength){
                        throw new Exception("Header content-length and length of data read do not match!");
                    }

                    // Convert read data into a string that can be parsed into json
                    String jsonString = new String(data, StandardCharsets.UTF_8);

                    // Parse the string into json
                    JSONObject json = new JSONObject(jsonString);
                    String query = json.getString("query");
                    String lang = json.getString("lang");
                    // Call the einthusanAPI with query and language
                    EinthusanAPI einthuAPI = new EinthusanAPI();
                    // take the json response and save it as a string because OutputStreamWriter needs a string
                    String searchResults = einthuAPI.search(query, lang).toString();

                    // Response Headers
                    //Headers responseHeaders = he.getResponseHeaders();

                    // Send response headers saying status and length of response body
                    System.out.println(searchResults);
                    he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                    he.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin");
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, searchResults.length());

                    // Response body
                    // Open an output stream with the response body
                    OutputStream os = he.getResponseBody();
                    // Use output stream writer to write strings instead of bytes
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(searchResults);
                    // Flush and close the output stream writer
                    osw.flush();
                    osw.close();

                    // close the request input stream and output stream
                    he.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    static class RawUrlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he){
            System.out.println("Serving raw url request of method " + he.getRequestMethod());
            // Browser sends an OPTIONS request first called a pre flight that checks CORS and if Content-type header is allowed
            if (he.getRequestMethod().equalsIgnoreCase("OPTIONS")){
                System.out.println("Serving Options Request");
                try{
                    he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                    he.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin, Content-type");
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    he.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try{
                if (he.getRequestMethod().equalsIgnoreCase("POST")){
                    // Request headers
                    Headers requestHeaders = he.getRequestHeaders();
                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                    InputStream is = he.getRequestBody();
                    byte[] data = new byte[contentLength];
                    int dataLength = is.read(data);

                    if (dataLength != contentLength){
                        throw new Exception("Request data length does not match content length header");
                    }

                    String jsonString = new String(data, StandardCharsets.UTF_8);
                    JSONObject requestBody = new JSONObject(jsonString);
                    String movieUrl = requestBody.getString("url");
                    EinthusanAPI einthusanAPI = new EinthusanAPI();
                    String rawUrl = einthusanAPI.getRawUrl(movieUrl);

                    he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                    he.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin");
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, rawUrl.length());
                    OutputStream os = he.getResponseBody();
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(rawUrl);
                    osw.flush();
                    osw.close();

                    he.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    static class HeartBeatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he){
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            System.out.println("Heartbeat request received");

            if (he.getRequestMethod().equalsIgnoreCase("GET")){
                try {
                    String responseString = "Server is up!";
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseString.length());
                    OutputStream os = he.getResponseBody();
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(responseString);
                    osw.flush();
                    osw.close();

                    he.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    static class ShutDownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he){
            System.out.println("Shutdown request received");
            if (he.getRequestMethod().equalsIgnoreCase("GET")){
                try{
                    String responseString = "Shutting down server!";
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseString.length());
                    OutputStream os = he.getResponseBody();
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(responseString);
                    osw.flush();
                    osw.close();

                    // Close the connection and stop the server
                    he.close();
                    he.getHttpContext().getServer().stop(0);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }
}
