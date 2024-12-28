package choo.weather.service;

import choo.weather.domain.Diary;
import choo.weather.repository.DiaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;

    @Value("${openweathermap.key}")
    private String apiKey;

    public DiaryService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    public String createDiary(LocalDate date, String text) {
        String weatherData = getWeatherString();

        // Check if the weather data retrieval failed
        if ("failed to get response".equals(weatherData)) {
            throw new RuntimeException("Failed to fetch weather data from OpenWeather API.");
        }

        Map<String, Object> parseWeather = parseWeather(weatherData);

        // Handle missing data gracefully
        String weather = parseWeather.get("main") != null ? parseWeather.get("main").toString() : "Unknown";
        String icon = parseWeather.get("icon") != null ? parseWeather.get("icon").toString() : "N/A";
        Double temperature = parseWeather.get("temp") != null ? (Double) parseWeather.get("temp") : 0.0;

        Diary myDiary = new Diary();
        myDiary.setWeather(weather);
        myDiary.setIcon(icon);
        myDiary.setTemperature(temperature);
        myDiary.setText(text);
        myDiary.setDate(date);

        diaryRepository.save(myDiary);
        return weatherData;
    }


  /*  public String createDiary(LocalDate date, String text) {
        //open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();
        log.info("Weather API response: {}", weatherData);

        //받아온 날씨 json 파싱
        Map<String, Object> parseWeather = parseWeather(weatherData);


        //파싱된 데이터
        Diary myDiary = new Diary();
        myDiary.setWeather(parseWeather.get("weather").toString());
        myDiary.setIcon(parseWeather.get("icon").toString());
        myDiary.setTemperature((Double) parseWeather.get("temp"));
        myDiary.setText(text);
        myDiary.setDate(date);

        diaryRepository.save(myDiary);
        return weatherData;
    }*/

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException("JSON parsing failed", e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        // Check for "main" data
        if (jsonObject.containsKey("main")) {
            JSONObject mainData = (JSONObject) jsonObject.get("main");
            resultMap.put("temp", mainData.get("temp"));
        } else {
            resultMap.put("temp", null); // Default or handle gracefully
        }

        // Check for "weather" array
        if (jsonObject.containsKey("weather")) {
            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            if (!weatherArray.isEmpty()) {
                JSONObject weatherData = (JSONObject) weatherArray.get(0);
                resultMap.put("main", weatherData.get("main"));
                resultMap.put("icon", weatherData.get("icon"));
            }
        } else {
            resultMap.put("main", null); // Default or handle gracefully
            resultMap.put("icon", null); // Default or handle gracefully
        }

        return resultMap;
    }


   /* private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }*/

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid="
                + apiKey;


        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }

    }
}
