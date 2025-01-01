package choo.weather.error;

public class InvalidDate extends RuntimeException{
    /**
     * 날씨를 알 수 없는 먼 미래의 날씨 error
     */
    private static final String MESSAGE = "해당 날짜는 현재 설정할 수 없습니다.";

    public InvalidDate(){
        super(MESSAGE);
    }
}
