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

