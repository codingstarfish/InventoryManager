from pathlib import Path

# (상품명, 크기, 가격, 누적 입고 수, 누적 판매 수)
products = []
data = Path("data")
data.mkdir(exist_ok=True)

logical = []
physical = []

for i, (name, size, price, received, sold) in enumerate(products, 1):
    assert 0 <= sold <= received <= 999

    code = f"P{i:05d}"
    logical.append(f"{name},{code},{size},{price}")

    for number in range(1, received + 1):
        status = 1 if number <= sold else 0
        physical.append(f"{code},{number:03d},{status}")

def write_lines(filename, lines):
    content = "\n".join(lines)
    if lines:
        content += "\n"
    (data / filename).write_text(content, encoding="utf-8")


write_lines("logical_item.txt", logical)
write_lines("physical_item.txt", physical)