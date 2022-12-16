package io.github.TheLingo1;

import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.lang.System.exit;

public class EinthusanCommandline {
    public static void main(String[] args) throws IOException {
        String[] sarr = new String[0];
        EinthusanServer.main(sarr);
        while (true){
            System.out.println("Enter Operation: \n\t 1 - Search\n\t 2 - Raw Url\n\t 3 - Heartbeat \n\t 4 - Exit");
            BufferedReader opReader = new BufferedReader(new InputStreamReader(System.in));
            int op = Integer.parseInt(opReader.readLine());
            if (op == 1) {
                System.out.println("Enter Search Term: ");
                BufferedReader searchReader = new BufferedReader(new InputStreamReader(System.in));
                String query = searchReader.readLine();

                System.out.println("Choose Language \n\t 1 - malayalam \n\t 2 - tamil \n\t 3 - telugu \n\t 4 - hindi \n\t 5 - kannada \n\t 6 - bengali \n\t 7 - marathi \n\t 8 - punjabi");
                BufferedReader langReader = new BufferedReader(new InputStreamReader(System.in));
                int lang = Integer.parseInt(langReader.readLine());
                String language = null;
                switch (lang){
                    case 1:
                        language = "malayalam";
                        break;
                    case 2:
                        language = "tamil";
                        break;
                    case 3:
                        language = "telugu";
                        break;
                    case 4:
                        language = "hindi";
                        break;
                    case 5:
                        language = "kannada";
                        break;
                    case 6:
                        language = "bengali";
                        break;
                    case 7:
                        language = "marathi";
                        break;
                    case 8:
                        language = "punjabi";
                        break;
                }



                SearchRequest(language, query);
            }else if (op == 2) {
                System.out.println("Enter movie url: ");
                BufferedReader movieReader = new BufferedReader(new InputStreamReader(System.in));
                String url = movieReader.readLine();
                RawUrlRequest(url);
            } else if (op == 3) {
                HeartbeatRequest();
            } else if (op == 4) {
                ExitRequest();
                exit(0);
            }

        }

    }

    public static void HeartbeatRequest() throws IOException {
        URL url = new URL("http://127.0.0.1:8080/heartbeat");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        int responseCode = request.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

            String inline;
            StringBuffer response = new StringBuffer();

            while ((inline = responseReader.readLine()) != null){
                response.append(inline);
            }
            responseReader.close();

            System.out.println(response);
        }
        System.out.println("Request response code - " + responseCode);


    }

    public static void SearchRequest(String lang, String query) throws IOException {

        URL url = new URL("http://127.0.0.1:8080/search");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");

        request.setDoOutput(true);
        OutputStream os = request.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        osw.write(String.format("{\"lang\":\"%s\",\"query\":\"%s\"}", lang, query));
        osw.flush();
        osw.close();

        os.close();

        int responseCode = request.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

            String inline;
            StringBuffer response = new StringBuffer();

            while ((inline = responseReader.readLine()) != null){
                response.append(inline);
            }
            responseReader.close();

            JSONObject responseJSON = new JSONObject(response.toString());
            int i = 0;
            for (i = 0; i <= 5; i++){
                String index = String.format("%s", i);
                String requestTitle = responseJSON.getJSONObject(index).getString("title");
                String requestDesc = responseJSON.getJSONObject(index).getString("desc");
                String requestUrl = "https://www.einthusan.tv" + responseJSON.getJSONObject(index).getString("url");

                System.out.println((i + 1) + ". " + requestTitle + " - " + requestDesc);
            }
            int searchSelection = -1;
            do {
                System.out.println("Please choose a result for raw URL");
                BufferedReader searchSelectionReader = new BufferedReader(new InputStreamReader(System.in));
                searchSelection = Integer.parseInt(searchSelectionReader.readLine()) - 1;
            } while (searchSelection < 0 || searchSelection >= i);

            String selectionUrl = "https://www.einthusan.tv" + responseJSON.getJSONObject(String.format("%s", searchSelection)).getString("url");
            RawUrlRequest(selectionUrl);

        }
        System.out.println("Request response code - " + responseCode);
        System.out.println("Exit now? (y/n)");
        BufferedReader searchExit = new BufferedReader(new InputStreamReader(System.in));
        String exitConfirm = searchExit.readLine();
        if (exitConfirm.equalsIgnoreCase("y")){
            ExitRequest();
            exit(0);
        } else {

        }
    }

    public static void RawUrlRequest(String reqUrl) throws IOException {
        URL url = new URL("http://127.0.0.1:8080/raw");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");

        request.setDoOutput(true);
        OutputStream os = request.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        osw.write(String.format("{\"url\":\"%s\"}", reqUrl));
        osw.flush();
        osw.close();

        os.close();

        int responseCode = request.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

            String inline;
            StringBuffer response = new StringBuffer();

            while ((inline = responseReader.readLine()) != null) {
                response.append(inline);
            }
            responseReader.close();
            System.out.println(response);
        }


    }
    public static void ExitRequest() throws IOException{
        URL url = new URL("http://127.0.0.1:8080/shutdown");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        int responseCode = request.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

            String inline;
            StringBuffer response = new StringBuffer();

            while ((inline = responseReader.readLine()) != null){
                response.append(inline);
            }
            responseReader.close();

            System.out.println(response);
        }
        System.out.println("Request response code - " + responseCode);
    }

}
