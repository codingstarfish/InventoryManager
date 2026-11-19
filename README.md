# InventoryManager

2026-2 전공기초프로젝트 A02팀의 **편의점 재고 관리 시스템**입니다. 터미널에서 상품 등록, 재고 입고, 상품 판매, 환불, 가상날짜 변경, 재고 조회를 수행하며, 세 개의 UTF-8 텍스트 파일에 데이터를 저장합니다.

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
| `{작업 디렉터리}/data/logical_item.txt` | 상품명, 논리코드, 크기, 가격, 유통기한일수 |
| `{작업 디렉터리}/data/physical_item.txt` | 논리코드, 접미번호, 상태(0: 재고 / 1: 판매 / 2: 폐기), 생산일, 판매일(- 허용), 환불이력 |
| `{작업 디렉터리}/data/virtual_date.txt` | 가상날짜 한 행, 최초 설정 전에는 빈 파일 |

## 코드 구조

실행 흐름은 `Main → 시작 검사 → 최초 날짜 설정 → Console → Service → InventoryDatabase → EntityWriter`입니다.

| 담당 | 클래스 |
| --- | --- |
| 파일 생성과 접근 확인 | `FileAccess` |
| 파일 읽기와 시작 순서 | `StartupLoader` |
| 파일의 필드 형식 검사 | `EntityParser` |
| 파일의 값 범위·중복·연속성·참조·상태·날짜·용량 검사 | `FileIntegrityValidator` |
| 사용자 입력과 화면 출력 | `Input`, `InputValidator`, `Console`, `ConsoleView` |
| 상품 등록, 입고·판매, 조회 | `ProductService`, `StockService`, `DateService`, `QueryService` |
| 공유 상태 조회와 저장 후 상태 교체 | `InventoryDatabase` |
| 정렬과 UTF-8 파일 기록 | `EntityWriter` |

## 2차 확장

최초 실행 시 가상날짜(2000-01-01~2037-12-31)를 설정합니다. 메뉴 5는 종료, 6은 환불, 7은 날짜 변경입니다. 등록 시 유통기한 일수(0: 무기한)를, 입고 시 생산일을 입력합니다. 만료일 당일까지 판매·환불 가능하며 날짜 변경 시 만료된 재고를 폐기합니다. 유기한 상품은 임박 순 또는 물리코드 직접 지정으로 판매하고, 무기한 상품은 접미번호순으로 판매합니다. 최초 환불 후 재판매에는 70% 가격을 10원 단위로 내림(최저 10원)하여 적용합니다.

기존 4필드/3필드 데이터는 시작 검사에서 거부합니다. 생산일을 임의로 채우는 자동 변환은 제공하지 않습니다. 실제 `data/`는 작업 중 변경하지 않았습니다.

검증: `python scripts/review_phase2.py`와 `inventory.service.Phase2ServiceTest`를 실행합니다. 기존 `scripts/review_product_spec.py`는 1차 스키마 전용 검수 자료입니다. 요구사항 해석은 [2차 구현 결정](docs/phase2-decisions.md)을 참조합니다.
