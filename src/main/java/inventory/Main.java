package inventory;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        System.out.println("프로젝트 설정 확인");

        System.out.println(
                "작업 디렉터리: " + Path.of("").toAbsolutePath()
        );

        System.out.println(
                "데이터 디렉터리: " + Path.of("data").toAbsolutePath()
        );
    }
}