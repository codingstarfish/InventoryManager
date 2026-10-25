package inventory.util;

import java.io.BufferedReader;
import java.io.IOException;
import inventory.exception.EndOfInputException;


public class Input {

    // UTF-8 System.in을 감싼 입력 채널
    private final BufferedReader reader;

    
    public Input(BufferedReader reader) {
        this.reader = reader;
    }

    // 입력 끝(EOF)이면 EndOfInputException. 엔터만 누른 빈 줄은 빈 문자열로 반환
    // 입력을 읽는 중 IOException은 호출한 곳으로 전달
    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new EndOfInputException();
        }
        return line;
    }
}
