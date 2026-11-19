"""Java 17 compilation and isolated CLI regression cases for the second specification."""
from pathlib import Path
import json
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'target/phase2-review'
CLASSES = BASE / 'classes'
BASE.mkdir(parents=True, exist_ok=True)
sources = list((ROOT/'src/main/java').rglob('*.java'))
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-Xlint:all', '-d', str(CLASSES), *map(str,sources)],check=True)
results=[]

def run(inputs='5\n', logical='Milk,P00001,3,1700,5', physical='', date='2026-11-13', folder=None):
    if folder is None:
        folder=Path(tempfile.mkdtemp(dir=BASE))
        data=folder/'data'; data.mkdir()
        for name,value in [('logical_item.txt',logical),('physical_item.txt',physical),('virtual_date.txt',date)]:
            (data/name).write_bytes(value.encode('utf-8'))
    result=subprocess.run(['java','-cp',str(CLASSES),'inventory.Main'],cwd=folder,input=inputs,encoding='utf-8',capture_output=True,timeout=15)
    assert not result.stderr,result.stderr
    return folder,result

def case(name,action):
    action(); results.append(name); print('PASS',name)

def invalid(logical,physical,date,message):
    folder,r=run(logical=logical,physical=physical,date=date)
    assert r.returncode==1 and message in r.stdout,r.stdout
    assert 'ConvenienceStore>' not in r.stdout
    assert (folder/'data/physical_item.txt').read_bytes()==physical.encode()

def initial():
    f,r=run('2026-02-29\n2024-02-29\n5\n',logical='',date='')
    assert r.returncode==0 and '존재하지 않는 날짜' in r.stdout,r.stdout
    assert 'y/n' not in r.stdout
    assert (f/'data/virtual_date.txt').read_text().strip()=='2024-02-29'
case('initial date calendar validation',initial)
case('all syntax before semantics',lambda:invalid('Milk,P00001,3,1,5','P00001,1,0,2026-11-13,-,0','2026-11-13','접미번호'))
case('legacy schema rejected',lambda:invalid('Milk,P00001,3,1700','','2026-11-13','5개 필드'))
for date in ['2026-11-13\n2026-11-14',' 2026-11-13','\n','\ufeff2026-11-13']:
    case('invalid date file '+repr(date),lambda d=date:invalid('Milk,P00001,3,1700,5','',d,'virtual_date.txt'))
for physical in ['P00001,001,0,2026-02-30,-,0','P00001,001,1,2026-11-10,-,0','P00001,001,0,2026-11-01,-,0','P00001,001,1,2026-11-01,2026-11-10,0']:
    case('invalid physical '+physical,lambda p=physical:invalid('Milk,P00001,3,1700,5',p,'2026-11-13','physical_item.txt'))
case('missing date with units',lambda:invalid('Milk,P00001,3,1700,5','P00001,001,0,2026-11-13,-,0','','virtual_date.txt'))

def lifecycle():
    f,r=run('2026-11-13\n1\nMilk\n3\n30\n5\n2\nP00001\n2\n2026-11-13\n3\nP00001\n1\n2\n6\nP00001-001\n6\nP00001-002\n3\nP00001\n1\n2\n6\nP00001-001\nn\n5\n',logical='',date='')
    assert r.returncode==0 and '합계: 40원' in r.stdout and '이미 환불' in r.stdout,r.stdout
    physical=(f/'data/physical_item.txt').read_text()
    assert physical.splitlines()==['P00001,001,1,2026-11-13,2026-11-13,1','P00001,002,1,2026-11-13,2026-11-13,1']
    _,r=run(folder=f); assert r.returncode==0,r.stdout
case('register receive sell refund discount resell restart',lifecycle)

def expiry():
    f,r=run('7\n2026-11-14\n3\nP00002\n1\n5\n',logical='Milk,P00001,3,1700,3\nBread,P00002,1,10,0',physical='P00001,001,0,2026-11-10,-,0\nP00002,001,0,2000-01-01,-,0')
    assert r.returncode==0,r.stdout
    assert (f/'data/physical_item.txt').read_text().splitlines()==['P00001,001,2,2026-11-10,-,0','P00002,001,1,2000-01-01,2026-11-14,0']
    _,r=run(folder=f); assert r.returncode==0,r.stdout
case('expiry boundary and new sale date in same session',expiry)

P='P00001,001,0,2026-11-14,-,0\nP00001,002,0,2026-11-14,-,0\nP00001,003,0,2026-11-13,-,0'
def order():
    _,r=run('3\nP00001\n1\n3\n5\n',physical=P,date='2026-11-17')
    assert r.returncode==0,r.stdout
    body=r.stdout.split('판매가 완료되었습니다.')[1]
    assert body.index('P00001-003')<body.index('P00001-001')<body.index('P00001-002'),body
case('expiry order with suffix tie break',order)
def direct():
    f,r=run('3\nP00001\n2\n P00001-001 \n5\n',physical=P,date='2026-11-17')
    assert r.returncode==0,r.stdout
    assert (f/'data/physical_item.txt').read_text().splitlines()[0]=='P00001,001,1,2026-11-14,2026-11-17,0'
case('direct sale with console spaces',direct)
def production():
    f,r=run('2\nP00001\n1\n2026-11-14\ny\n2026-11-10\ny\n2026-11-11\n5\n',logical='Milk,P00001,1,10,2')
    assert r.returncode==0 and '이후' in r.stdout and '유통기한이 지났' in r.stdout,r.stdout
    assert (f/'data/physical_item.txt').read_text().strip()=='P00001,001,0,2026-11-11,-,0'
case('production retry keeps quantity and suffix',production)
def upper():
    f,r=run('2\nP00001\n1\n2037-12-31\n4\n2\nP00001\n5\n',logical='Milk,P00001,1,10,1',date='2037-12-31')
    assert r.returncode==0 and '2038-01-01' in r.stdout,r.stdout
case('expiry beyond input year bound',upper)
for elapsed,day,ok in [(14,'2026-11-15',True),(15,'2026-11-16',False)]:
    def boundary(d=day,yes=ok):
        f,r=run('6\nP00001-001\n'+('' if yes else 'n\n')+'5\n',logical='Milk,P00001,1,10,0',physical='P00001,001,1,2026-11-01,2026-11-01,0',date=d)
        assert r.returncode==0 and ('환불이 완료' in r.stdout)==yes,r.stdout
    case('refund boundary '+str(elapsed),boundary)
for tail in ['n\n5\n','']:
    def cancel(t=tail):
        f,r=run('2\nP00001\n1\n2026-11-14\n'+t)
        assert r.returncode==0 and (f/'data/physical_item.txt').read_bytes()==b'',r.stdout
    case('production cancel or EOF '+repr(tail),cancel)
(BASE/'results.json').write_text(json.dumps({'passed':len(results),'cases':results},ensure_ascii=False,indent=2),encoding='utf-8')
print(len(results),'passed')
