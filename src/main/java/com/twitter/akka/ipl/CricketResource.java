package com.twitter.akka.ipl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class CricketResource {

    private static final String MATCH_URL = "http://cricapi.com/api/matches?apikey=";
    private static final String MATCH_ID = "unique_id";
    private static final String TEAM_1 = "team-1";
    private static final String MATCHES = "matches";
    private static final String CRIC_API_KEY = "cric.apikey";
    private static final String TEAMS_JSON = "teams.json";
    private static final String CRIC_SCORE = "http://cricapi.com/api/cricketScore?apikey=%s&unique_id=%s";
    private static final HttpClient httpClient = HttpClientBuilder.create().build();

    private final String apiKey;
    private final Map<String, String> iplTeams;

    public CricketResource(Properties properties) throws IOException {
        this.apiKey = properties.getProperty(CRIC_API_KEY);
        iplTeams = new Gson().fromJson(
                IOUtils.toString(getClass().getClassLoader().getResourceAsStream(TEAMS_JSON), Charset.defaultCharset()),
                new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
    }

    public Set<Integer> getIPLMatchIDs() throws IOException {
        JSONObject matchJson = new JSONObject(executeGet(MATCH_URL + apiKey));
        Set<Integer> matchIds = new HashSet<>();
        for (Object match : matchJson.getJSONArray(MATCHES)) {
            if (isIPLMatch(match)) {
                matchIds.add(((JSONObject) match).getInt(MATCH_ID));
            }
        }
        System.out.println(matchIds);
        return matchIds;
    }

    private boolean isIPLMatch(Object match) {
        return iplTeams.keySet().contains(((JSONObject) match).getString(TEAM_1));
    }

    public String getScore(Integer id) {
        try {
            JSONObject matchJson = new JSONObject(executeGet(String.format(CRIC_SCORE, apiKey, id)));
            if (matchJson.getBoolean("matchStarted")) {
                return matchJson.getString("score");
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String executeGet(String url) throws IOException {
        HttpGet fetchMatchData = new HttpGet(url);
        HttpResponse response = httpClient.execute(fetchMatchData);
        return IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
    }
}

