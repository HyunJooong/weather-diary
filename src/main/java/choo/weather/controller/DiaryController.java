package choo.weather.controller;

import choo.weather.service.DiaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping("/create/diary")
    void creteDiary(@RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                    @RequestBody String text
    ) {
        diaryService.createDiary(date, text);
    }

}