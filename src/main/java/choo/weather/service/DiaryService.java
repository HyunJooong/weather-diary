package choo.weather.service;

import choo.weather.WeatherApplication;
import choo.weather.domain.DateWeather;
import choo.weather.domain.Diary;
import choo.weather.repository.DateWeatherRepository;
import choo.weather.repository.DiaryRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    @Value("${openweathermap.key}")
    private String apiKey;

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    /**
     * 날씨 데이터 파싱
     *
     * @param jsonString
     * @return
     */
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

    /**
     * 날씨 openapi json data
     *
     * @return
     */
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

    /**
     * 날씨 데이터 API 가져오기
     * @return
     */
    private DateWeather getWeatherFromApi() {
        //open weather map 에서 날씨 데이터 가져오기
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

        DateWeather dateWeather = new DateWeather();
        //현재 날씨 now()로 가져오기
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(weather);
        dateWeather.setIcon(icon);
        dateWeather.setTemperature(temperature);

        return dateWeather;

    }

    /**
     * 매일 오전 1시 날씨 데이터 저장 스케쥴링
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매일 1시 마다 실행
    public void saveWeatherData() {
        try {
            DateWeather dateWeather = getWeatherFromApi();
            if (dateWeather != null) {
                dateWeatherRepository.save(dateWeather);
                System.out.println("Weather data saved successfully.");
            } else {
                System.err.println("Weather data is null. Skipping save.");
            }
        } catch (Exception e) {
            System.err.println("Error occurred while saving weather data: " + e.getMessage());
        }
    }


    /**
     * 날씨 일기 작성
     *
     * @param date 해당 날짜
     * @param text 일기 내용
     * @return
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {

        logger.info("started to create diary");
        //날씨 데이터 가죠오기(api or db)
        DateWeather getDateWeather = getDateWeather(date);

        Diary myDiary = new Diary();
        myDiary.setDateWeather(getDateWeather);
        myDiary.setText(text);

        diaryRepository.save(myDiary);
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.size() == 0) {
            //api에서 현재 날씨 가져오기
            return getWeatherFromApi();
        }else return dateWeatherListFromDB.get(0); //DB에서 현재 날씨 가져오기
    }

    /**
     * 날짜 기준 일기 가져오기
     *
     * @param date 날짜기준
     * @return 해당 날짜 일기들 가져오기
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.info("read Diary");
        return diaryRepository.findAllByDate(date);
    }

    /**
     * 날짜 기준 일기들 가져오기
     *
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 일기 list
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    /**
     * 일기 변경
     *
     * @param date 해당 날짜
     * @param text 변경 일기 내용
     */
    public void updateDiary(LocalDate date, String text) {
        Diary editDiary = diaryRepository.getFirstByDate(date);
        editDiary.setText(text);
        diaryRepository.save(editDiary);

    }

    /**
     * 일기 삭제
     *
     * @param date 해당 날짜 일기 모두 삭제
     */
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
