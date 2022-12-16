package io.github.TheLingo1;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.JSONObject;



public class EinthusanAPI
{
    public static void main( String[] args ) {
        EinthusanAPI s = new EinthusanAPI();
        //System.out.println(s.search("Premam", "malayalam"));
//        System.out.println(s.getRawUrl("https://einthusan.tv/movie/watch/3118/?lang=malayalam"));

    }

    public JSONObject search(String query, String lang) {
        String requestUrl = String.format("https://einthusan.tv/movie/results/?lang=%s&query=%s", lang, query);

        JSONObject output = new JSONObject();
        try {
            Document doc = Jsoup.connect(requestUrl).get();

            Elements results = doc.select("section#UIMovieSummary > ul > li");

            for (Element result : results) {
                // Scrape the title of each result from the search
                String resultTitle = result.select("div.block2 > a.title > h3").text();

                // Scrape the movie url
                String resultUrl = result.select("div.block2 > a.title").attr("href");

                // Scrape the image link of each result
                String resultImageUrl = result.select("div.block1 > a > img").attr("src").substring(2);

                // Scrape the description
                String resultDescription = result.select("div.block2 > p.synopsis").text();
                // Need to reset pre ouput by declaring it newly here every loop
                JSONObject preOutput = new JSONObject();
                preOutput.put("title", resultTitle);
                preOutput.put("url", resultUrl);
                preOutput.put("image", resultImageUrl);
                preOutput.put("desc", resultDescription);
                String resultIndex = String.format("%s",results.indexOf(result));

               output.put(resultIndex, preOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return output;
    }

    public String getRawUrl(String movieUrl) {
        String rawVidLink = null;
        try {
            Document doc = Jsoup.connect(movieUrl).get();
            rawVidLink = doc.select("section#UIVideoPlayer").attr("data-mp4-link").substring(9).split("/", 2)[1];
            rawVidLink = "https://cdn1.einthusan.io/" + rawVidLink;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawVidLink;
    }
}
