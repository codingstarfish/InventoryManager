package inventory.util;

import java.io.BufferedReader;
import java.io.IOException;
import inventory.exception.EndOfInputException;


public class Input {

    // UTF-8 System.in을 감싼 입력 채널
    private final BufferedReader reader;

    
    /**
     * 콘솔에서 한 줄씩 읽을 입력 채널 보관
     *
     * @param reader 사용자 입력을 읽는 BufferedReader
     */
    public Input(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * 입력에서 한 줄을 읽고, 입력 끝(EOF)이면 EndOfInputException
     *
     * @return 줄 끝 개행을 제외한 원문 문자열. 엔터만 누른 빈 줄은 빈 문자열
     * @throws EndOfInputException 입력이 끝나 더 읽을 줄이 없는 경우
     * @throws IOException 입력을 읽는 중 입출력 오류가 발생한 경우
     */
    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new EndOfInputException();
        }
        return line;
    }
}
