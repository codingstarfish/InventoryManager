package inventory.util;

import java.io.BufferedReader;
import java.io.IOException;
import inventory.exception.EndOfInputException;
import java.util.Objects;

/**
 * 한 줄 사용자 입력만 담당. 파일 reader로 재사용하지 않습니다.
 */
public class Input {
    /** Main에서 UTF-8 System.in을 감싼 입력 채널 하나 */
    private final BufferedReader reader;

    /**
     * 의존 부품을 보관합니다. 생성 시 입력·출력·파일 작업 없음.
     * @param reader Main에서 UTF-8 System.in을 감싼 입력 채널 하나
     */
    public Input(BufferedReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    /**
     * 한 줄만 소비합니다. reader.readLine()==null만 EOF.
     * 표준 공백 제거는 InputValidator 담당, 빈 줄은 EOF로 처리하지 않습니다.
     * 입력 장치 오류는 반복 복구하지 않고 최상위 실행 경계에 전달.
     * @return 줄 끝 개행을 제외한 원문; 빈 줄이면 빈 문자열
     * @throws EndOfInputException EOF
     * @throws IOException 입력 장치 읽기 실패; 정상 사용자 오류와 섞지 않음
     */
    public String readLine() throws IOException {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
