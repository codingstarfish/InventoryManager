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

    /**
     * @throws EndOfInputException EOF
     * @throws IOException 입력 장치 읽기 실패
     */
    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new EndOfInputException();
        }
        return line;
    }
}
