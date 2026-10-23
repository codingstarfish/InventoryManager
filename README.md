# InventoryManager

2026-2 전공기초프로젝트 A02팀의 **편의점 재고 관리 시스템**입니다. 터미널에서 상품 등록, 재고 입고, 상품 판매, 재고 조회를 수행하며, 두 개의 UTF-8 텍스트 파일에 데이터를 저장합니다.

## 실행 환경

| 구분 | 요구사항 |
| --- | --- |
| 완성된 JAR 실행 | Java SE 17 이상 실행 환경 |
| 소스 빌드 | JDK 17 이상, Maven |
| 운영체제 | Windows 10/11, macOS, Linux |
| 터미널 | UTF-8 인코딩 및 유니코드 입출력 지원 |
| 파일 권한 | 작업 디렉터리의 `data/` 생성 및 데이터 파일 읽기·쓰기 가능 |

별도의 외부 실행 라이브러리는 사용하지 않습니다. 최초 Maven 빌드 시 빌드 플러그인 다운로드를 위한 인터넷 연결이 필요할 수 있습니다.

## 빌드 및 실행

### 1. 소스 받기

```sh
git clone https://github.com/codingstarfish/InventoryManager.git
cd InventoryManager
```

ZIP으로 소스를 받은 경우에는 압축을 푼 뒤 `pom.xml`이 있는 프로젝트 루트에서 다음 명령을 실행합니다.

### 2. 환경 확인 및 빌드

```sh
java -version
javac -version
mvn -version
mvn clean package
```

Maven이 사용하는 Java도 17 이상인지 `mvn -version` 출력에서 확인합니다. 빌드에 성공하면 `target/InventoryManager.jar`가 생성됩니다.

### 3. 실행

프로젝트 루트에서 바로 실행할 수 있습니다.

```sh
java -jar target/InventoryManager.jar
```

기획서의 실행 방식에 맞추려면 생성된 JAR를 프로젝트 루트로 복사합니다.

**Windows PowerShell**

```powershell
Copy-Item target/InventoryManager.jar ./InventoryManager.jar
java -jar InventoryManager.jar
```

**Windows 명령 프롬프트(cmd)**

```bat
copy target\InventoryManager.jar InventoryManager.jar
java -jar InventoryManager.jar
```

**macOS / Linux**

```sh
cp target/InventoryManager.jar ./InventoryManager.jar
java -jar InventoryManager.jar
```

## 데이터 저장 위치

데이터 파일은 현재 작업 디렉터리 아래의 `data/`에 저장됩니다.

| 경로 | 저장 내용 |
| --- | --- |
| `{작업 디렉터리}/data/logical_item.txt` | 상품명, 논리코드, 크기, 가격 |
| `{작업 디렉터리}/data/physical_item.txt` | 논리코드, 접미번호, 판매여부 |

## 코드 구조

실행 흐름은 `Main → 시작 검사 → Console → Service → InventoryDatabase → EntityWriter`입니다.

| 담당 | 클래스 |
| --- | --- |
| 파일 생성과 접근 확인 | `FileAccess` |
| 파일 읽기와 시작 순서 | `StartupLoader` |
| 파일의 필드 형식 검사 | `EntityParser` |
| 파일의 값 범위·중복·연속성·참조·FIFO·용량 검사 | `FileIntegrityValidator` |
| 사용자 입력과 화면 출력 | `Input`, `InputValidator`, `Console`, `ConsoleView` |
| 상품 등록, 입고·판매, 조회 | `ProductService`, `StockService`, `QueryService` |
| 가변 Map·List의 조회·변경 및 파일 저장 요청 | `InventoryDatabase` |
| 정렬과 UTF-8 파일 기록 | `EntityWriter` |

파일 검증은 두 파일의 문법 검사를 모두 끝낸 뒤 의미 검사를 수행합니다.
문법 검사는 `EntityParser`, 의미·관계 검사는 `FileIntegrityValidator`가 담당합니다.
검사를 통과한 행만 `StartupLoader`가 데이터베이스에 넣고, 낱개 목록을 접미번호순으로 정렬합니다.

사용자 입력의 문법·범위·가격 단위는 `InputValidator`가 검사합니다.
`Console`은 등록 한도를 확인하고, 입고·판매에서는 기존 상품을 선택한 결과와 검증된 수량을 서비스에 전달합니다.
`StockService`는 접미번호 여유·창고 용량·현재 재고 등 실제 업무 조건을 검사합니다.
Raw 행과 엔티티 생성자, 예외 객체는 이미 확인한 값을 다시 검증하지 않습니다.
현재 재고·발급 가능 수·남은 용량은 저장된 낱개 기록으로부터 계산합니다.

등록은 논리 파일만, 입고·판매는 물리 파일만 저장합니다.
필요한 검증을 모두 마친 뒤 데이터베이스의 Map·List를 수정하고 해당 파일을 저장합니다.
파일 저장과 닫기가 성공한 뒤 완료 메시지를 출력합니다.
저장 실패 시 오류 메시지와 종료 코드 1로 즉시 종료하며, 별도의 메모리 롤백은 하지 않습니다.

## 동작 검증

```sh
mvn clean verify
python3 scripts/check_spec.py
```

현재 `src/test`의 Maven 테스트 소스는 없습니다. `mvn clean verify`는 컴파일과 JAR 생성을 확인하며,
Python 3 표준 라이브러리로 작성한 검수 스크립트가 기획서 7.7절의 사례를 실제 JAR 실행으로 점검합니다.
입력 경계값, 문법 검사 후 의미 검사, 코드·접미번호 연속성, FIFO, 수량·용량 한도,
저장 형식과 대상 파일, 재입력·취소·EOF, 저장 실패 메시지와 종료 코드, 재실행 후 다음 번호를 확인합니다.
검수 데이터는 모두 임시 디렉터리에 생성하므로 기존 `data/` 파일은 변경하지 않습니다.
