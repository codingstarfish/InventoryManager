package inventory.util;

import java.io.BufferedReader;
import java.io.IOException;
import inventory.exception.EndOfInputException;


public class Input {

    // UTF-8 System.in을 감싼 입력 채널
    private final BufferedReader reader;

    
    /**
     * 콘솔에서 한 줄씩 읽을 입력 채널을 보관
     * @param reader BufferedReader
     */
    public Input(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * 입력에서 한 줄을 읽으며 EOF이면 EndOfInputException을 전달, 빈 줄은 EOF와 구분
     * @return 줄 끝 개행을 제외한 원문 문자열, 빈 줄이면 빈 문자열이며 null은 반환하지 않음
     * @throws EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 전달
     * @throws IOException 입력 또는 파일 입출력이 실패하면 전달
     */
    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new EndOfInputException();
        }
        return line;
    }
}
