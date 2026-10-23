#!/usr/bin/env python3
"""기획서 7.7 경계 사례를 실제 JAR로 점검합니다. 모든 데이터는 임시 폴더에 둡니다.

실행: mvn clean package && python3 scripts/check_spec.py
외부 Python 패키지나 기존 data 파일을 사용하지 않습니다.
"""

import argparse
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import threading


LOGICAL = "logical_item.txt"
PHYSICAL = "physical_item.txt"
BASE = "우유,P00001,5,1200\n"
THREE = "P00001,001,0\nP00001,002,0\nP00001,003,0\n"
RETRY = "ConvenienceStore: 다시 입력하시겠습니까? (y/n) > "
EOF = "입력이 종료되어 프로그램을 종료합니다."
NEWLINE = os.linesep


class SkipCase(Exception):
    """환경에서 실제로 재현할 수 없는 검수 사례입니다."""


def encoded(value):
    return value.encode("utf-8") if isinstance(value, str) else value


def saved(*rows):
    """저장 결과는 UTF-8, BOM 없음, 환경 개행 및 마지막 개행까지 비교합니다."""
    return (NEWLINE.join(rows) + NEWLINE).encode("utf-8") if rows else b""


def physical(code, count, sold=0):
    return "".join(f"{code},{i:03d},{int(i <= sold)}\n" for i in range(1, count + 1))


def expect_output(result, code=0, contains=(), absent=()):
    assert result.returncode == code, f"종료 코드 {result.returncode}, 예상 {code}\n{result.stdout}"
    assert not result.stderr, f"예상하지 않은 stderr:\n{result.stderr}"
    for message in contains:
        assert message in result.stdout, f"필요한 출력 없음: {message!r}\n{result.stdout}"
    for message in absent:
        assert message not in result.stdout, f"출력되면 안 되는 내용: {message!r}\n{result.stdout}"


class Fixture:
    def __init__(self, jar, logical=BASE, stock=""):
        self.jar = jar
        self.temp = tempfile.TemporaryDirectory(prefix="inventory-spec-")
        self.root = Path(self.temp.name)
        self.data = self.root / "data"
        if logical is not None or stock is not None:
            self.data.mkdir()
        for name, value in ((LOGICAL, logical), (PHYSICAL, stock)):
            if value is not None:
                (self.data / name).write_bytes(encoded(value))

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.temp.cleanup()

    def contents(self):
        return {name: (self.data / name).read_bytes()
                for name in (LOGICAL, PHYSICAL) if (self.data / name).is_file()}

    def run(self, inputs="5\n", code=0, contains=(), absent=(), unchanged=False):
        before = self.contents() if unchanged else {}
        result = subprocess.run(["java", "-jar", str(self.jar)], cwd=self.root,
                                input=inputs, encoding="utf-8", capture_output=True, timeout=30)
        expect_output(result, code, contains, absent)
        if unchanged:
            after = self.contents()
            for name, content in before.items():
                assert after.get(name) == content, f"{name}의 기존 바이트가 변경됨"
        return result.stdout


def startup_cases(check, jar):
    def valid(logical, stock, expected=None):
        with Fixture(jar, logical, stock) as fixture:
            fixture.run(contains=("무결성 검사 완료. 프로그램을 시작합니다.",), unchanged=True)
            assert set(fixture.contents()) == {LOGICAL, PHYSICAL}
            if expected is not None:
                assert fixture.contents() == expected

    check("두 파일 없음: 빈 파일 자동 생성", lambda: valid(None, None, {LOGICAL: b"", PHYSICAL: b""}))
    check("논리 파일만 있음: 빈 물리 파일 생성", lambda: valid(BASE, None))
    check("빈 물리 파일만 있음: 빈 논리 파일 생성", lambda: valid(None, ""))
    check("두 0바이트 파일", lambda: valid("", ""))
    check("정상 물리 레코드", lambda: valid(BASE, "P00001,001,0"))
    check("중복 속성의 서로 다른 논리상품", lambda: valid(BASE + "우유,P00002,5,1200\n", ""))
    for separator in ("\n", "\r", "\r\n"):
        check(f"개행 {separator!r}", lambda sep=separator: valid(
            f"우유,P00001,5,1200{sep}빵,P00002,1,10{sep}",
            f"P00001,001,0{sep}P00002,001,0{sep}"))
    check("개행 혼용 및 정렬되지 않은 정상 행", lambda: valid(
        "빵,P00002,1,10\r\n우유,P00001,5,1200\r물,P00003,2,20\n",
        "P00002,001,0\rP00001,003,0\nP00001,001,1\r\nP00001,002,0"))

    cases = [
        ("미등록 물리 파일만 있음", None, "P00001,001,0\n", "등록되어 있지 않습니다."),
        ("논리 파일 개행만 있음", "\n", "", "빈 행은 허용하지 않습니다."),
        ("물리 파일 개행만 있음", BASE, "\r\n", "빈 행은 허용하지 않습니다."),
        ("마지막 두 개행", BASE + "\n", "", "빈 행은 허용하지 않습니다."),
        ("잘못된 UTF-8 논리 파일", b"\xff", "", "올바른 UTF-8 파일이어야 합니다."),
        ("잘못된 UTF-8 물리 파일", BASE, b"\xff", "올바른 UTF-8 파일이어야 합니다."),
        ("논리 파일 BOM", "\ufeff" + BASE, "", "BOM 없는 UTF-8 파일이어야 합니다."),
        ("물리 파일 BOM", BASE, "\ufeffP00001,001,0", "BOM 없는 UTF-8 파일이어야 합니다."),
        ("헤더", "상품명,논리코드,크기,가격\n", "", "논리코드는 대문자 P"),
        ("논리 필드 부족", "우유,P00001,5\n", "", "논리 파일의 각 행은 4개 필드여야 합니다."),
        ("논리 여분 필드", BASE.rstrip() + ",\n", "", "논리 파일의 각 행은 4개 필드여야 합니다."),
        ("물리 여분 필드", BASE, "P00001,001,0,\n", "물리 파일의 각 행은 3개 필드여야 합니다."),
        ("빈 상품명", ",P00001,5,1200\n", "", "상품명은 완성형 한글"),
        ("상품명 앞 공백", " 우유,P00001,5,1200\n", "", "상품명은 완성형 한글"),
        ("상품명 뒤 공백", "우유 ,P00001,5,1200\n", "", "상품명은 완성형 한글"),
        ("파일 소문자 코드", "우유,p00001,5,1200\n", "", "논리코드는 대문자 P"),
        ("파일 크기 선행 0", "우유,P00001,0005,1200\n", "", "크기는 첫 문자가 1~9"),
        ("파일 크기 0", "우유,P00001,0,1200\n", "", "크기는 첫 문자가 1~9"),
        ("파일 가격 선행 0", "우유,P00001,5,01200\n", "", "가격은 첫 문자가 1~9"),
        ("물리코드 전체를 접미번호로 기록", BASE, "P00001,P00001-001,0\n", "접미번호는 숫자 세 자리"),
        ("접미번호 자릿수", BASE, "P00001,1,0\n", "접미번호는 숫자 세 자리"),
        ("판매여부 01", BASE, "P00001,001,01\n", "판매여부는 0 또는 1"),
        ("논리번호 00000", "우유,P00000,5,1200\n", "", "논리코드의 번호는 00001"),
        ("파일 크기 범위", "우유,P00001,1001,1200\n", "", "상품 크기는 1~1000"),
        ("파일 가격 범위 우선", "우유,P00001,5,10000001\n", "", "가격은 10~10000000"),
        ("파일 가격 단위", "우유,P00001,5,15\n", "", "가격은 10원 단위"),
        ("논리코드 중복", BASE + BASE, "", "논리코드가 중복되었습니다."),
        ("논리코드 중간 누락", BASE + "빵,P00003,1,10\n", "", "논리코드가 P00001부터 중간 번호"),
        ("접미번호 000", BASE, "P00001,000,0\n", "접미번호는 001~999"),
        ("미등록 상품 참조", BASE, "P00002,001,0\n", "등록되어 있지 않습니다."),
        ("낱개 중복", BASE, "P00001,001,0\nP00001,001,0\n", "조합이 중복되었습니다."),
        ("접미번호 중간 누락", BASE, "P00001,001,0\nP00001,003,0\n", "접미번호는 001부터 중간 번호"),
        ("FIFO 위반", BASE, "P00001,001,0\nP00001,002,1\n", "더 작은 접미번호의 미판매 상품"),
        ("논리 의미 오류보다 물리 문법 오류 우선", "우유,P00000,5,1200\n", "P00001,1,0\n", "접미번호는 숫자 세 자리"),
        ("물리 의미 오류보다 논리 문법 오류 우선", "우유,P00001,0005,1200\n", "P00001,000,0\n", "크기는 첫 문자가 1~9"),
        ("앞 행 의미 오류보다 뒤 행 문법 오류 우선", "우유,P00000,5,1200\n빵,P00002,0005,10\n", "", "크기는 첫 문자가 1~9"),
    ]
    for name, logical, stock, reason in cases:
        def invalid(logical=logical, stock=stock, reason=reason):
            with Fixture(jar, logical, stock) as fixture:
                fixture.run(code=1, contains=(reason, "데이터 파일 오류로 프로그램을 종료합니다."),
                            absent=("ConvenienceStore>",), unchanged=True)
        check(name, invalid)

    def inaccessible_path(name):
        with Fixture(jar) as fixture:
            path = fixture.data / name
            path.unlink()
            path.mkdir()
            fixture.run(code=1, contains=(name, "데이터 파일 경로가 일반 파일이 아닙니다."))
    check("물리 파일 경로가 디렉터리", lambda: inaccessible_path(PHYSICAL))
    check("논리 파일 경로가 디렉터리", lambda: inaccessible_path(LOGICAL))

    def data_is_file():
        with Fixture(jar, None, None) as fixture:
            original = b"existing data path content"
            fixture.data.write_bytes(original)
            fixture.run(code=1, contains=("데이터 디렉터리나 빈 데이터 파일을 생성할 수 없습니다.",),
                        absent=("ConvenienceStore>",))
            assert fixture.data.read_bytes() == original, "data 경로의 기존 일반 파일이 변경됨"
    check("data 경로가 일반 파일: 생성 실패 및 기존 내용 보존", data_is_file)

    def permission_denied(permission, removed_bits, reason):
        if os.name != "posix":
            raise SkipCase("POSIX 파일 권한 검사를 지원하지 않는 환경")
        with Fixture(jar) as fixture:
            path = fixture.data / LOGICAL
            original_mode = path.stat().st_mode & 0o7777
            before = fixture.contents()
            try:
                path.chmod(original_mode & ~removed_bits)
                if os.access(path, permission):
                    raise SkipCase("권한 비트를 제거해도 접근 가능하여 권한 거부를 재현할 수 없음")
                fixture.run(code=1, contains=(LOGICAL, reason), absent=("ConvenienceStore>",))
            finally:
                path.chmod(original_mode)
            assert fixture.contents() == before, "접근 오류 후 기존 파일 바이트가 변경됨"
    check("논리 파일 읽기 권한 없음", lambda: permission_denied(
        os.R_OK, 0o444, "데이터 파일을 읽을 수 없습니다."))
    check("논리 파일 쓰기 권한 없음", lambda: permission_denied(
        os.W_OK, 0o222, "데이터 파일에 쓸 수 없습니다."))


def input_cases(check, jar):
    def invalid(inputs, message, logical=BASE, stock="", absent=()):
        with Fixture(jar, logical, stock) as fixture:
            output = fixture.run(inputs, contains=(message,), absent=absent, unchanged=True)
            assert output.count(RETRY) == 1, "오류 단계의 재입력 확인이 한 번이어야 함"

    for value in ("", "01", "0", "6", "\t1", "１"):
        def bad_menu(value=value):
            with Fixture(jar) as fixture:
                fixture.run(f"{value}\n5\n", contains=("메뉴는 1~5 중 하나",), absent=(RETRY,), unchanged=True)
        check(f"주 메뉴 문법 {value!r}", bad_menu)
    for value in ("", "우유(대)", "A,B", "ㄱㅏ", "우\t유", "😀", "A" * 31):
        check(f"상품명 문법 {value!r}", lambda value=value: invalid(
            f"1\n{value}\nn\n5\n", "상품명은 완성형 한글"))
    for label, prefix in (("입고", "2\n"), ("판매", "3\n"), ("조회", "4\n2\n")):
        for code, reason in (("p00001", "대문자 P와 숫자 5자리"),
                             ("P00000", "번호는 00001~99999"),
                             ("P00010", "존재하지 않는 논리적 상품코드")):
            check(f"{label} 코드 오류 {code}", lambda prefix=prefix, code=code, reason=reason: invalid(
                f"{prefix}{code}\nn\n5\n", reason))
    for label, prefix, subject in (("크기", "1\n우유\n", "상품 크기는"),
                                   ("가격", "1\n우유\n5\n", "가격은"),
                                   ("입고", "2\nP00001\n", "입고 수량은"),
                                   ("판매", "3\nP00001\n", "판매 수량은")):
        for value in ("12345678901", "+1", "1,000", "\t1", "1 0", "１"):
            check(f"{label} 숫자 문법 {value!r}", lambda prefix=prefix, subject=subject, value=value: invalid(
                f"{prefix}{value}\nn\n5\n", subject + " 숫자 1~10자리로 입력해야 합니다."))
        check(f"{label} 10자리 숫자 의미 오류", lambda prefix=prefix, subject=subject: invalid(
            f"{prefix}9999999999\nn\n5\n", subject + " " + (
                "1~1000 용량 단위여야 합니다." if subject == "상품 크기는" else
                "10~10000000원이어야 합니다." if subject == "가격은" else "1~100개여야 합니다."),
            absent=(subject + " 숫자",)))
    for value in ("0", "1001"):
        check(f"크기 범위 {value}", lambda value=value: invalid(
            f"1\n우유\n{value}\nn\n5\n", "상품 크기는 1~1000"))
    check("가격 15 단위 오류", lambda: invalid("1\n우유\n5\n15\nn\n5\n", "가격은 10원 단위"))
    check("가격 범위 검사가 단위 검사보다 우선", lambda: invalid(
        "1\n우유\n5\n10000001\nn\n5\n", "가격은 10~10000000", absent=("가격은 10원 단위",)))
    for menu, label in ((2, "입고"), (3, "판매")):
        for value in (0, 101):
            check(f"{label} 수량 범위 {value}", lambda menu=menu, label=label, value=value: invalid(
                f"{menu}\nP00001\n{value}\nn\n5\n", label + " 수량은 1~100"))
    check("조회 방식 오류", lambda: invalid("4\n3\nn\n5\n", "조회 방식은 1 또는 2"))

    def retry_and_register():
        with Fixture(jar, "", "") as fixture:
            output = fixture.run("1\n  Ab  우유  \n0\nY\n y \n0005\n15\ny\n01200\n5\n",
                                 contains=("y 또는 n을 입력해야 합니다.", "신규 상품이 등록되었습니다."))
            assert output.count("신규 상품명 >") == 1, "앞 단계 정상 입력을 다시 요구함"
            assert fixture.contents() == {LOGICAL: saved("Ab  우유,P00001,5,1200"), PHYSICAL: b""}
    check("y 재입력·잘못된 확인 반복·표준 공백 제거·선행 0 저장", retry_and_register)

    def cancel_then_register():
        with Fixture(jar, "", "") as fixture:
            fixture.run("1\n취소상품\n5\n15\n n \n1\n우유\n5\n10\n1\n우유\n5\n10\n5\n",
                        contains=("작업을 취소했습니다.", "논리코드: P00001", "논리코드: P00002"))
            assert fixture.contents() == {LOGICAL: saved("우유,P00001,5,10", "우유,P00002,5,10"), PHYSICAL: b""}
    check("취소 시 코드 미발급 및 동일 속성 중복 등록", cancel_then_register)

    def input_boundaries():
        with Fixture(jar, "", "") as fixture:
            name = "가" * 30
            fixture.run(f" 1 \n {name} \n 1000 \n 10000000 \n 2 \n P00001 \n 001 \n 4 \n 2 \n P00001 \n 5 \n",
                contains=(f"상품명: {name}", "가격: 10000000원", "입고 수량: 1개, 현재 재고: 1개"))
            assert fixture.contents() == {LOGICAL: saved(f"{name},P00001,1000,10000000"),
                                          PHYSICAL: saved("P00001,001,0")}
    check("상품명 30자·크기 가격 상한·모든 입력 앞뒤 표준 공백", input_boundaries)

    def empty_menus():
        with Fixture(jar, "", "") as fixture:
            fixture.run("2\n3\n4\n1\n5\n", contains=("등록된 상품이 없어 입고할 수 없습니다.",
                "등록된 상품이 없어 판매할 수 없습니다.", "등록된 상품이 없습니다.",
                "창고 사용 용량: 0, 남은 용량: 1000000"), unchanged=True)
    check("등록상품 없는 입고·판매·전체 조회", empty_menus)


def stock_cases(check, jar):
    def transaction(menu, quantity, stock="", contains=(), code_rows=None):
        with Fixture(jar, BASE, stock) as fixture:
            fixture.run(f"{menu}\nP00001\n{quantity}\n5\n", contains=contains)
            assert fixture.contents()[LOGICAL] == encoded(BASE), "입고·판매가 논리 파일을 변경함"
            if code_rows is not None:
                assert fixture.contents()[PHYSICAL] == saved(*code_rows)
    for quantity in (1, 100):
        check(f"입고 수량 {quantity} 성공", lambda quantity=quantity: transaction(2, quantity,
            contains=(f"입고 수량: {quantity}개, 현재 재고: {quantity}개",),
            code_rows=[f"P00001,{i:03d},0" for i in range(1, quantity + 1)]))
        check(f"판매 수량 {quantity} 성공", lambda quantity=quantity: transaction(3, quantity,
            stock=physical("P00001", quantity), contains=(f"판매 수량: {quantity}개, 현재 재고: 0개",),
            code_rows=[f"P00001,{i:03d},1" for i in range(1, quantity + 1)]))
    check("현재 재고 3개 전량 판매", lambda: transaction(3, 3, THREE,
        contains=("판매 수량: 3개, 현재 재고: 0개",), code_rows=[f"P00001,{i:03d},1" for i in range(1, 4)]))

    def rejected(stock, quantity, message, menu=2, absent=()):
        with Fixture(jar, BASE, stock) as fixture:
            fixture.run(f"{menu}\nP00001\n{quantity}\nn\n5\n", contains=(message, RETRY),
                        absent=absent, unchanged=True)
    check("재고 3개에서 4개 판매 전체 거절", lambda: rejected(THREE, 4, "현재 재고: 3개", 3))
    check("재고 0개도 판매 수량 입력 후 부족 안내", lambda: rejected("", 1, "현재 재고: 0개", 3))
    check("H=998에서 1개 입고: 999 발급", lambda: transaction(2, 1, physical("P00001", 998, 998),
        code_rows=[f"P00001,{i:03d},1" for i in range(1, 999)] + ["P00001,999,0"]))
    check("H=998에서 2개 입고 전체 거절", lambda: rejected(physical("P00001", 998, 998), 2,
        "물리적 상품코드가 부족합니다. (발급 가능 수: 1개)"))

    def retry_quantity(menu, stock, requested, retried, message, expected, current, next_suffix):
        with Fixture(jar, BASE, stock) as fixture:
            output = fixture.run(f"{menu}\nP00001\n{requested}\ny\n{retried}\n4\n2\nP00001\n5\n",
                contains=(message, f"현재 재고: {current}개", f"다음 접미번호: {next_suffix}"))
            prompt = "입고할 논리적 상품코드 >" if menu == 2 else "판매할 기존 상품의 논리코드 >"
            assert output.count(prompt) == 1, "수량 재입력 시 코드 입력을 다시 요구함"
            assert output.count("상품명: 우유, 현재 재고:") == 1, "선택한 상품을 다시 표시함"
            assert fixture.contents() == {LOGICAL: encoded(BASE), PHYSICAL: saved(*expected)}
    check("접미번호 부족 후 y 재입력: 2개 거절 → 1개 입고", lambda: retry_quantity(
        2, physical("P00001", 998, 998), 2, 1, "물리적 상품코드가 부족합니다.",
        [f"P00001,{i:03d},1" for i in range(1, 999)] + ["P00001,999,0"], 1, "없음"))
    check("재고 부족 후 y 재입력: 4개 거절 → 3개 판매", lambda: retry_quantity(
        3, THREE, 4, 3, "재고가 부족합니다.", [f"P00001,{i:03d},1" for i in range(1, 4)], 0, "004"))

    def exhausted():
        with Fixture(jar, BASE, physical("P00001", 999, 999)) as fixture:
            fixture.run("2\nP00001\n5\n", contains=("물리적 상품코드가 소진되어 입고할 수 없습니다.",),
                        absent=("입고 수량 >", RETRY), unchanged=True)
    check("H=999 Q=0: 수량 입력 전 소진 안내", exhausted)

    # 999 * 1000 + 198 * 5 = 999990. 대상 P00002의 발급 가능 수는 801.
    capacity_logical = "큰상품,P00001,1000,10\n작은상품,P00002,5,10\n"
    capacity_stock = physical("P00001", 999) + physical("P00002", 198)

    def capacity(quantity, success):
        with Fixture(jar, capacity_logical, capacity_stock) as fixture:
            fixture.run(f"2\nP00002\n{quantity}\n" + ("4\n1\n5\n" if success else "n\n5\n"),
                        contains=(("창고 사용 용량: 1000000, 남은 용량: 0",) if success else
                                  ("창고의 남은 용량이 부족합니다. (필요 용량: 15, 남은 용량: 10)",)),
                        unchanged=not success)
            if success:
                assert fixture.contents()[PHYSICAL] == saved(*(capacity_stock.splitlines() +
                    ["P00002,199,0", "P00002,200,0"]))
    check("U=999990 S=5에서 2개 입고 허용", lambda: capacity(2, True))
    check("U=999990 S=5에서 3개 입고 거절", lambda: capacity(3, False))

    def retry_capacity():
        with Fixture(jar, capacity_logical, capacity_stock) as fixture:
            output = fixture.run("2\nP00002\n3\ny\n2\n4\n2\nP00002\n4\n1\n5\n", contains=(
                "창고의 남은 용량이 부족합니다.", "입고 수량: 2개, 현재 재고: 200개",
                "다음 접미번호: 201", "창고 사용 용량: 1000000, 남은 용량: 0"))
            assert output.count("입고할 논리적 상품코드 >") == 1, "수량 재입력 시 코드 입력을 다시 요구함"
            assert output.count("상품명: 작은상품, 현재 재고:") == 1, "선택한 상품을 다시 표시함"
            assert fixture.contents() == {LOGICAL: encoded(capacity_logical), PHYSICAL: saved(*(
                capacity_stock.splitlines() + ["P00002,199,0", "P00002,200,0"]))}
    check("용량 부족 후 y 재입력: 3개 거절 → 2개 입고", retry_capacity)

    def capacity_validation():
        with Fixture(jar, capacity_logical, capacity_stock + "P00002,199,0\nP00002,200,0\n") as fixture:
            fixture.run("1\n공간없는신규상품\n1\n10\n5\n", contains=("논리코드: P00003",))
        with Fixture(jar, capacity_logical, capacity_stock + "P00002,199,0\nP00002,200,0\nP00002,201,0\n") as fixture:
            fixture.run(code=1, contains=("logical_item.txt / physical_item.txt", "창고 사용 용량이 최대 용량 1000000을 초과합니다."),
                        unchanged=True)
    check("정확한 최대 용량에서 등록 허용·초과 파일 거절", capacity_validation)

    def validation_order():
        logical = "큰상품,P00001,1000,10\n작은상품,P00002,5,10\n"
        stock = physical("P00001", 998) + physical("P00002", 400)
        with Fixture(jar, logical, stock) as fixture:
            fixture.run("2\nP00001\n101\ny\n2\nn\n5\n", contains=("입고 수량은 1~100개여야 합니다.",
                        "물리적 상품코드가 부족합니다. (발급 가능 수: 1개)"),
                        absent=("창고의 남은 용량이 부족합니다.",), unchanged=True)
    check("수량 범위 → 발급 가능 수 → 용량 검사 순서", validation_order)


def persistence_cases(check, jar):
    logical = "빵,P00002,1,10\r\n우유,P00001,5,1200"  # 일부러 역순·마지막 개행 없음
    stock = "P00002,001,0\rP00001,003,0\nP00001,001,1\r\nP00001,002,0"

    def read_only():
        with Fixture(jar, logical, stock) as fixture:
            output = fixture.run("4\n1\n4\n2\nP00001\n1\n취소\n0\nn\n5\n", unchanged=True,
                contains=("누적 입고: 3개, 누적 판매: 1개, 현재 재고: 2개, 발급 가능 수: 996개, 다음 접미번호: 004",
                          "물리코드: P00001-001, 판매여부: 1", "창고 사용 용량: 11, 남은 용량: 999989"))
            assert output.index("논리코드: P00001") < output.index("논리코드: P00002")
            assert output.index("P00001-001") < output.index("P00001-002") < output.index("P00001-003")
    check("조회 정렬·파생 수량·취소·종료의 기존 바이트 보존", read_only)

    def registration():
        with Fixture(jar, logical, stock) as fixture:
            fixture.run("1\n새상품\n1\n10\n5\n", contains=("논리코드: P00003",))
            assert fixture.contents() == {LOGICAL: saved("우유,P00001,5,1200", "빵,P00002,1,10", "새상품,P00003,1,10"),
                                          PHYSICAL: encoded(stock)}
    check("등록은 논리 파일만 UTF-8·정렬·환경 개행으로 저장", registration)

    def fifo_and_restart():
        with Fixture(jar, logical, stock) as fixture:
            fixture.run("3\nP00001\n1\n2\nP00001\n1\n5\n")
            assert fixture.contents() == {LOGICAL: encoded(logical), PHYSICAL: saved("P00001,001,1", "P00001,002,1",
                "P00001,003,0", "P00001,004,0", "P00002,001,0")}
            fixture.run("4\n2\nP00001\n2\nP00001\n1\n1\n다음상품\n1\n10\n5\n",
                contains=("다음 접미번호: 005", "논리코드: P00003"))
            assert fixture.contents()[PHYSICAL].endswith(saved("P00001,005,0", "P00002,001,0"))
    check("FIFO 판매·물리 저장 정렬·접미번호 미재사용·재실행 다음 번호", fifo_and_restart)

    stages = [("주 메뉴", ""), ("상품명", "1\n"), ("크기", "1\n우유\n"),
              ("가격", "1\n우유\n5\n"), ("입고 코드", "2\n"), ("입고 수량", "2\nP00001\n"),
              ("판매 코드", "3\n"), ("판매 수량", "3\nP00001\n"), ("조회 방식", "4\n"),
              ("조회 코드", "4\n2\n"), ("재입력 확인", "1\n\n")]
    for stage, inputs in stages:
        def eof_stage(inputs=inputs):
            with Fixture(jar, logical, stock) as fixture:
                fixture.run(inputs, contains=(EOF,), unchanged=True)
        check(f"EOF 단계: {stage}", eof_stage)

    def eof_after_save():
        with Fixture(jar, "", "") as fixture:
            fixture.run("1\n우유\n5\n1200\n1\n미완료\n5\n", contains=(EOF,))
            assert fixture.contents() == {LOGICAL: saved("우유,P00001,5,1200"), PHYSICAL: b""}
            fixture.run("1\n빵\n1\n10\n5\n", contains=("논리코드: P00002",))
    check("EOF 이전 완료 작업 보존·미완료 등록 폐기·다음 코드", eof_after_save)

    def logical_limit():
        full = "".join(f"상품,P{i:05d},1,10\n" for i in range(1, 100000))
        with Fixture(jar, full, "") as fixture:
            fixture.run("1\n2\nP99999\n1\n3\nP99999\n1\n4\n2\nP99999\n5\n", contains=(
                "사용 가능한 논리적 상품코드가 없어 신규 상품을 등록할 수 없습니다.",
                "입고가 완료되었습니다.", "판매가 완료되었습니다.", "논리코드: P99999"),
                absent=("신규 상품명 >",))
            assert fixture.contents()[LOGICAL] == encoded(full)
            assert fixture.contents()[PHYSICAL] == saved("P99999,001,1")
    check("논리코드 99999개 소진: 등록만 거절·기존 입고 판매 조회 유지", logical_limit)


def save_failure_cases(check, jar):
    def failed_save(menu, name, inputs, completed):
        with Fixture(jar, BASE, THREE) as fixture:
            process = subprocess.Popen(["java", "-jar", str(jar)], cwd=fixture.root,
                stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            output = bytearray()
            started = threading.Event()

            def read_output():
                while True:
                    character = process.stdout.read(1)
                    if not character:
                        break
                    output.extend(character)
                    if output.endswith(b"ConvenienceStore> "):
                        started.set()
            reader = threading.Thread(target=read_output, daemon=True)
            reader.start()
            try:
                assert started.wait(15), "시작 성공 후 주 프롬프트를 기다리다 시간 초과"
                # 시작 검사는 통과시킨 뒤 실제 쓰기 실패만 강제합니다.
                path = fixture.data / name
                path.unlink()
                path.mkdir()
                process.stdin.write(f"{menu}\n{inputs}".encode("utf-8"))
                process.stdin.close()
                process.wait(timeout=15)
                reader.join(timeout=5)
                assert not reader.is_alive(), "출력 수집이 종료되지 않음"
                result = subprocess.CompletedProcess(process.args, process.returncode,
                    output.decode("utf-8"), process.stderr.read().decode("utf-8"))
                expect_output(result, 1, (f"[오류] {name} 저장에 실패했습니다. 작업을 완료하지 못했습니다.",
                    "프로그램을 종료합니다."), (completed, RETRY, EOF))
                assert result.stdout.count("ConvenienceStore> ") == 1, "저장 실패 후 주 메뉴로 복귀함"
                other = PHYSICAL if name == LOGICAL else LOGICAL
                assert fixture.contents()[other] == encoded(THREE if other == PHYSICAL else BASE)
                fixture.run(code=1, contains=(name, "데이터 파일 경로가 일반 파일이 아닙니다."),
                            absent=("ConvenienceStore>",))
            finally:
                if process.poll() is None:
                    process.kill()
                    process.wait()
                reader.join(timeout=5)
                process.stdout.close()
                process.stderr.close()
                if not process.stdin.closed:
                    process.stdin.close()

    check("등록 저장 실패: 오류·종료 코드 1·완료 안내 없음", lambda: failed_save(
        1, LOGICAL, "신규\n5\n10\n", "신규 상품이 등록되었습니다."))
    check("입고 저장 실패: 오류·종료 코드 1·완료 안내 없음", lambda: failed_save(
        2, PHYSICAL, "P00001\n1\n", "입고가 완료되었습니다."))
    check("판매 저장 실패: 오류·종료 코드 1·완료 안내 없음", lambda: failed_save(
        3, PHYSICAL, "P00001\n1\n", "판매가 완료되었습니다."))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, default=Path(__file__).resolve().parents[1] / "target/InventoryManager.jar")
    args = parser.parse_args()
    jar = args.jar.resolve()
    if not jar.is_file():
        parser.error(f"JAR가 없습니다: {jar}. 먼저 mvn clean package를 실행하세요.")
    results = []

    def check(name, action):
        try:
            action()
        except SkipCase as reason:
            results.append(None)
            print(f"[SKIP] {name}: {reason}", flush=True)
        except (AssertionError, OSError, subprocess.SubprocessError) as error:
            results.append(False)
            print(f"[FAIL] {name}\n{error}", flush=True)
        else:
            results.append(True)
            print(f"[PASS] {name}", flush=True)

    for group in (startup_cases, input_cases, stock_cases, persistence_cases, save_failure_cases):
        group(check, jar)
    passed = sum(result is True for result in results)
    skipped = sum(result is None for result in results)
    print(f"\n검수 결과: {passed}/{len(results)} 통과, {skipped} 건너뜀")
    return 0 if all(result is not False for result in results) else 1


if __name__ == "__main__":
    sys.exit(main())
