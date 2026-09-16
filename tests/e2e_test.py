# -*- coding: utf-8 -*-
"""
家教直通车系统 - 接口级端到端测试

覆盖：匿名可访问页面、静态资源、三角色登录、权限隔离、搜索、
      发布 -> 编辑 -> 审核 的完整流转，以及越权与非法状态的反向用例。

运行前先启动服务（默认 8080）：
    java -jar target/tutor-system-1.0.0.jar
执行：
    python tests/e2e_test.py
"""
import http.cookiejar
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request

BASE = os.environ.get('BASE_URL', 'http://localhost:8080')
PASS = 0
FAIL = 0
FAILED = []


def check(name, condition, detail=''):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f'  [OK]   {name}')
    else:
        FAIL += 1
        FAILED.append(name)
        print(f'  [FAIL] {name}  {detail}')


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def make(follow=True):
    cj = http.cookiejar.CookieJar()
    handlers = [urllib.request.HTTPCookieProcessor(cj)]
    if not follow:
        handlers.append(NoRedirect())
    return urllib.request.build_opener(*handlers)


def call(op, path, data=None, method=None):
    """返回 (状态码, 响应体, 最终落地地址)
    最终落地地址：未跟随重定向时是 Location，跟随重定向后是最终 URL。
    """
    url = BASE + urllib.parse.quote(path, safe='/?=&%')
    body = urllib.parse.urlencode(data, encoding='utf-8').encode() if data else None
    req = urllib.request.Request(url, body, method=method)
    if body:
        req.add_header('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
    try:
        r = op.open(req, timeout=25)
        return r.status, r.read().decode('utf-8', 'replace'), r.geturl()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8', 'replace'), (e.headers or {}).get('Location', '')


def title(html):
    m = re.search(r'<title>(.*?)</title>', html, re.S)
    return m.group(1).strip() if m else ''


def login(user, pwd):
    op = make(True)
    call(op, '/login', {'username': user, 'password': pwd}, 'POST')
    return op


print('=' * 66)
print('一、匿名可访问的页面与静态资源')
print('=' * 66)
anon = make(True)
for path, expect in [('/', '家教直通车系统'), ('/demands', '家教需求列表'),
                     ('/resumes', '教员简历列表'), ('/login', '用户登录'),
                     ('/register', '用户注册')]:
    code, body, _ = call(anon, path)
    check(f'GET {path} 返回 200 且标题正确', code == 200 and expect in title(body),
          f'-> {code} / {title(body)}')

code, body, _ = call(anon, '/style.css')
check('GET /style.css 返回真实 CSS', code == 200 and '.navbar' in body, f'-> {code}')

code, _, _ = call(anon, '/demand/detail/1')
check('GET /demand/detail/1 详情页可匿名访问', code == 200, f'-> {code}')
code, _, _ = call(anon, '/resume/detail/1')
check('GET /resume/detail/1 详情页可匿名访问', code == 200, f'-> {code}')

print()
print('=' * 66)
print('二、登录与权限隔离')
print('=' * 66)
admin = login('admin', 'admin123')
parent = login('parent1', 'parent123')
tutor = login('tutor1', 'tutor123')

for name, op in [('admin', admin), ('parent1', parent), ('tutor1', tutor)]:
    code, body, _ = call(op, '/user/center')
    check(f'{name} 登录后可访问个人中心', code == 200, f'-> {code}')

code, _, loc = call(make(False), '/admin/dashboard')
check('匿名访问后台被重定向到登录页', code == 302 and '/login' in loc, f'-> {code} {loc}')

code, _, _ = call(parent, '/admin/dashboard')
check('家长访问后台返回 403', code == 403, f'-> {code}')

code, _, _ = call(admin, '/admin/dashboard')
check('管理员访问后台返回 200', code == 200, f'-> {code}')

bad = make(True)
code, _, landing = call(bad, '/login', {'username': 'parent1', 'password': 'wrong_password'}, 'POST')
check('错误密码被拒绝（跳转到 ?error）', 'error' in landing, f'-> {landing}')

print()
print('=' * 66)
print('三、搜索功能')
print('=' * 66)
for path, tip in [('/resumes?keyword=数学', '简历'), ('/demands?keyword=数学', '需求'),
                  ('/?keyword=数学', '首页')]:
    code, body, _ = call(anon, path)
    check(f'{tip}搜索 {path} 正常返回', code == 200 and 'Whitelabel' not in body,
          f'-> {code}')

print()
print('=' * 66)
print('四、家长：发布需求 -> 编辑 -> 提交审核')
print('=' * 66)
code, _, _ = call(parent, '/demand/publish', {
    'subject': '物理', 'grade': '高中一年级',
    'description': '端到端测试：高一物理，力学基础薄弱，每周两次课',
    'salary': '150.00', 'contact': '13800138001'}, 'POST')
check('发布需求成功', code == 200, f'-> {code}')

code, body, _ = call(parent, '/user/center')
ids = [int(i) for i in re.findall(r'/demand/edit/(\d+)', body)]
check('新需求出现在个人中心的可编辑列表', len(ids) > 0, f'-> {ids}')
new_id = max(ids) if ids else None

if new_id:
    code, body, _ = call(parent, f'/demand/edit/{new_id}')
    check('编辑需求页返回 200', code == 200, f'-> {code}')
    check('编辑页是需求表单而非个人资料表单', '编辑家教需求' in title(body),
          f'-> {title(body)}')
    check('编辑页回填了原描述', '力学基础薄弱' in body)
    check('编辑页表单提交到 /demand/update', '/demand/update' in body)

    code, _, landing = call(parent, '/demand/update', {
        'demandId': new_id, 'subject': '物理', 'grade': '高中二年级',
        'description': '端到端测试：已改为高二物理', 'salary': '160.00',
        'contact': '13800138001'}, 'POST')
    check('提交编辑成功并跳回个人中心', code == 200 and '/user/center' in landing,
          f'-> {code} {landing}')

    code, body, _ = call(parent, f'/demand/detail/{new_id}')
    check('编辑后内容已更新', '高二物理' in body, f'-> 未找到新内容')

print()
print('=' * 66)
print('五、教员：发布简历 -> 编辑 -> 提交审核')
print('=' * 66)
code, _, _ = call(tutor, '/resume/publish', {
    'subjects': '物理,数学',
    'experience': '端到端测试：本科在读，两年高中物理辅导经验',
    'availableTime': '周末全天', 'expectedSalary': '130.00',
    'introduction': '耐心负责', 'certificates': '无'}, 'POST')
check('发布简历成功', code == 200, f'-> {code}')

code, body, _ = call(tutor, '/user/center')
rids = [int(i) for i in re.findall(r'/resume/edit/(\d+)', body)]
check('新简历出现在个人中心的可编辑列表', len(rids) > 0, f'-> {rids}')
rid = max(rids) if rids else None

if rid:
    code, body, _ = call(tutor, f'/resume/edit/{rid}')
    check('编辑简历页返回 200', code == 200, f'-> {code}')
    check('编辑页是简历表单', '编辑家教简历' in title(body), f'-> {title(body)}')
    check('编辑页回填了原经验', '两年高中物理辅导经验' in body)
    check('编辑页表单提交到 /resume/update', '/resume/update' in body)

print()
print('=' * 66)
print('六、管理员：审核需求')
print('=' * 66)
code, body, _ = call(admin, '/admin/demands/review')
check('待审核列表可访问', code == 200, f'-> {code}')

if new_id:
    code, _, landing = call(admin, f'/admin/demands/approve/{new_id}', {'status': '1'}, 'POST')
    check('管理员通过审核', code == 200 and 'demands/review' in landing,
          f'-> {code} {landing}')

    code, body, _ = call(anon, f'/demand/detail/{new_id}')
    check('通过后该需求对外可见（状态已发布）', '已发布' in body or '高二物理' in body)

    code, body, _ = call(parent, f'/demand/edit/{new_id}')
    check('已发布的需求不可再编辑（返回 403 页）', '权限不足' in body, f'-> {title(body)}')

print()
print('=' * 66)
print('七、反向用例')
print('=' * 66)
code, body, _ = call(parent, '/demand/edit/999999')
check('编辑不存在的需求返回 403 页而非 500', code == 200 and 'Whitelabel' not in body,
      f'-> {code}')
check('不存在的需求详情显示 404 页', '页面未找到' in call(anon, '/demand/detail/999999')[1])

print()
print('=' * 66)
print(f'结果：{PASS} 项通过，{FAIL} 项失败')
if FAILED:
    print('失败项：')
    for f in FAILED:
        print('  -', f)
print('=' * 66)
sys.exit(1 if FAIL else 0)
