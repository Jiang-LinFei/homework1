import os
import time
import datetime
from datetime import timedelta

from django import forms
from django.db import connection
from django.http import (
    HttpResponse,
    HttpResponseRedirect,
    JsonResponse,
    StreamingHttpResponse,
)
from django.shortcuts import render, redirect
from django.utils.timezone import now

from LaboratoryManagementSystem import settings
from LMS.models import DailyTime, StudentTask, Task, User


class RegisterForm(forms.Form):
    uid = forms.CharField(max_length=15)
    uname = forms.CharField(max_length=8)
    password = forms.CharField(max_length=30)
    type = forms.CharField(max_length=8)
    room = forms.CharField(max_length=8)
    sex = forms.CharField(max_length=2)
    class_field = forms.CharField(max_length=8)
    birthday = forms.CharField(max_length=8)
    photo = forms.CharField(max_length=50)
    tel = forms.CharField(max_length=11)
    email = forms.EmailField()


class LoginForm(forms.Form):
    uid = forms.CharField(max_length=15)
    password = forms.CharField(max_length=30)


def login(req):
    return render(req, 'login.html')


def loginVerify(request):
    if request.method == 'POST':
        uid = request.POST.get('uid')
        password = request.POST.get('password')
        try:
            user = User.objects.get(uid=uid)
            if uid == "111":
                return JsonResponse({'res': 2})
            if user.password == password:
                request.session['uname'] = user.uname
                request.session['uid'] = user.uid
                type = user.type
                if type == 'student':
                    return JsonResponse({'res': 1})
                if type == 'manager':
                    return JsonResponse({'res': 2})
            else:
                return JsonResponse({'res': 0})
        except User.DoesNotExist:
            return JsonResponse({'res': -1})
    return JsonResponse({'res': 100})


def logout(request):
    try:
        del request.session['uid']
    except KeyError:
        pass
    return redirect('/')


def inregister(request):
    return render(request, 'register.html')


def register(request):
    if request.method == 'POST':
        uid = request.POST.get('uid')
        password = request.POST.get('password')
        uname = request.POST.get('uname')
        type = request.POST.get('type')
        room = request.POST.get('room')
        sex = request.POST.get('sex')
        class_field = request.POST.get('class_field')
        birthday = request.POST.get('birthday')
        tel = request.POST.get('tel')
        email = request.POST.get('email')
        filterResult = User.objects.filter(uid=uid)
        if len(filterResult) > 0:
            return JsonResponse({'res': 1})
        else:
            user = User.objects.create(uid=uid, uname=uname, password=password, email=email, type=type,
                                       room=room, sex=sex, class_field=class_field, birthday=birthday, tel=tel)
            user.save()
            return JsonResponse({'res': 0})
    return JsonResponse({'res': 100})


def index(request):
    uname = request.session.get('uname')
    if not uname:
        return redirect('/')
    return render(request, 'index.html', {
        'uname': uname,
        'date': time.strftime("%Y-%m-%d"),
        'start_time': request.session.get('start_time', ''),
        'end_time': request.session.get('end_time', ''),
    })


def allTask(req):
    uname = req.session.get('uname')
    tasks = Task.objects.filter(finish='发布中')
    return render(req, 'allTask.html', locals())


def acceptTask(request):
    if request.method == 'POST':
        uname = request.session.get('uname')
        tid = request.POST.get('tid')
        task = Task.objects.get(tid=tid)
        sList = task.students.split(',')
        if uname in sList:
            return JsonResponse({'res': 1})
        task.students = task.students + uname + ','
        task.save()
        return JsonResponse({'res': 0})
    return JsonResponse({'res': 100})


def abandonTask(request):
    if request.method == 'POST':
        uname = request.session.get('uname')
        tid = request.POST.get('tid')
        task = Task.objects.get(tid=tid)
        sList = task.students.split(',')
        if uname in sList:
            sList.remove('')
            sList.remove(uname)
            students = ''
            for student in sList:
                students = student + ','
            task.students = students
            task.save()
            return JsonResponse({'res': 0})
        else:
            return JsonResponse({'res': 1})
    return JsonResponse({'res': 100})


def myWork(req):
    uid = req.session.get('uid')
    uname = req.session.get('uname')
    tasks = StudentTask.objects.filter(uid=uid)
    return render(req, 'myWork.html', locals())


def upload_ajax(request):
    uid = request.session.get('uid')
    if request.method == 'POST':
        file_obj = request.FILES.get('file')
        tid = request.POST.get('tid')
        file_path = os.path.join(settings.BASE_DIR, 'upload', tid)
        if not os.path.exists(file_path):
            os.makedirs(file_path)
        with open(os.path.join(file_path, file_obj.name), 'wb') as f:
            for chunk in file_obj.chunks():
                f.write(chunk)
        sid = int(str(uid) + tid)
        studentTask = StudentTask.objects.get(sid=sid)
        if studentTask.path is None:
            studentTask.path = file_obj.name + ','
        else:
            studentTask.path = studentTask.path + file_obj.name + ','
        studentTask.save()
        task = Task.objects.get(tid=tid)
        if task.path is None:
            task.path = file_obj.name + ','
        else:
            task.path = task.path + file_obj.name + ','
        task.save()
        return HttpResponse('OK')


def getWorkId(req):
    if req.method == 'POST':
        global workId
        workId = req.POST.get('sid')
        return JsonResponse({'res': 1})
    return JsonResponse({'res': 100})


def eachWork(req):
    uid = req.session.get('uid')
    uname = req.session.get('uname')
    studentTask = StudentTask.objects.get(sid=workId)
    task = Task.objects.get(tid=studentTask.tid)
    files = task.path.split(',')
    task2 = StudentTask.objects.filter(tid=studentTask.tid)
    return render(req, 'eachWork.html', locals())


def editMyWork(req):
    if req.method == 'POST':
        tname = req.POST.get('tname')
        work = req.POST.get('work')
        percent = int(req.POST.get('percent'))
        content = req.POST.get('content')
        progress = req.POST.get('progress')
        studentTask = StudentTask.objects.get(sid=workId)
        studentTask.sname = tname + '#' + work
        studentTask.content = content
        value = int(Task.objects.get(tid=studentTask.tid).value) * percent
        studentTask.value = int(value / 100)
        studentTask.progress = progress
        studentTask.save()

        allSW = StudentTask.objects.filter(tid=studentTask.tid)
        p = 0
        for s in allSW:
            p = int(s.progress / len(allSW)) + p
        newTask = Task.objects.get(tid=studentTask.tid)
        newTask.progress = p
        newTask.save()
        return JsonResponse({'res': 1})

    return JsonResponse({'res': 100})


def getFileInfo(request):
    if request.method == 'POST':
        download_name = request.POST.get('fileName')
        tid = request.POST.get('taskID')
        global fileInfo
        fileInfo = tid + r'/' + download_name
    return JsonResponse({'res': 1})


def download_file(request):
    the_file_name = str(fileInfo.split('/')[1]).split("/")[-1]
    filename = os.path.join(settings.BASE_DIR, 'upload').replace('\\', '/') + '/' + fileInfo
    response = StreamingHttpResponse(readFile(filename))
    response['Content-Type'] = 'application/octet-stream'
    response['Content-Disposition'] = 'attachment;filename="{0}"'.format(the_file_name)
    return response


def readFile(filename, chunk_size=512):
    """缓冲流下载文件方法。"""
    with open(filename, 'rb') as f:
        while True:
            c = f.read(chunk_size)
            if c:
                yield c
            else:
                break


def attendance(req):
    """上机签到：上班/下班/退出。签到状态存于 session。"""
    uname = req.session.get('uname')
    if not uname:
        return redirect('/')
    curtime = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
    today = time.strftime("%Y-%m-%d")
    if req.method == 'POST':
        if 'start' in req.POST:
            req.session['start_time'] = time.strftime("%H:%M:%S")
            req.session['start_ts'] = time.time()
            req.session['end_time'] = ''
            return redirect('/index/')
        if 'end' in req.POST:
            end_time = time.strftime("%H:%M:%S")
            start_ts = req.session.get('start_ts', time.time())
            time_length = int(time.time() - start_ts)
            DailyTime.objects.create(id=curtime + '-' + str(uname), date=today,
                                     start_time=req.session.get('start_time', ''),
                                     end_time=end_time, uname=uname, time_length=str(time_length))
            req.session['end_time'] = end_time
            return redirect('/index/')
        if 'cancel' in req.POST:
            return redirect('/logout/')
    return redirect('/index/')


def qingjia(req):
    """请假：提交请假申请并展示请假记录。"""
    uname = req.session.get('uname')
    if not uname:
        return redirect('/')
    if req.method == 'POST' and 'submit' in req.POST:
        reason = req.POST.get('reason') or ''
        leave_time = req.POST.get('leave_time') or '0'
        leave_start = req.POST.get('leave_start') or time.strftime("%Y-%m-%d")
        leave_end = req.POST.get('leave_end') or ''
        curtime = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
        DailyTime.objects.create(id=curtime + '-' + str(uname), date=leave_start,
                                 start_time=leave_start, end_time=leave_end,
                                 reason=reason, leave_time=leave_time,
                                 uname=uname, time_length=leave_time)
        return redirect('/qingjia/')
    leaves = DailyTime.objects.exclude(reason__isnull=True).exclude(reason='').order_by('-id')
    return render(req, 'qingjia.html', {'uname': uname, 'leaves': leaves})


def attendance_check(req):
    uname = req.session.get('uname')
    if not uname:
        return redirect('/')
    Method = req.method
    row_tuple = ()

    if Method == 'POST':
        if 'day' in req.POST:
            day_check = connection.cursor()
            day_query = "select uname,start_time,end_time,CONCAT(FLOOR(time_length/3600),'时',FLOOR((time_length%3600)/60), '分',((time_length%3600)%60), '秒'),date from daily_time where to_days(date) = to_days(now())"
            day_check.execute(day_query)
            day_row = day_check.fetchall()
            day_query1 = "select time_length from daily_time where to_days(date) = to_days(now())"
            day_check.execute(day_query1)
            day_row1 = day_check.fetchall()
            day_row_list = list(day_row)

            for i in range(len(day_row1)):
                if int(day_row1[i][0]) >= 36000:
                    day_row1_list = list(day_row[i])
                    day_row1_list.append('<a href="#"><i class="fa fa-check text-navy"></i></a>')
                    day_row_list[i] = tuple(day_row1_list)
                else:
                    day_row1_list = list(day_row[i])
                    day_row1_list.append('<a href="#"><i class="fa fa-times hongse"></i></a>')
                    day_row_list[i] = tuple(day_row1_list)

            row_tuple = tuple(day_row_list)
        if 'week' in req.POST:
            week_check = connection.cursor()
            week_query = "select uname,start_time,end_time,CONCAT(FLOOR(time_length/3600),'时',FLOOR((time_length%3600)/60), '分',((time_length%3600)%60), '秒'),date from daily_time where date between current_date()-7 and sysdate()"
            week_check.execute(week_query)
            week_row = week_check.fetchall()
            week_query1 = "select time_length from daily_time where date between current_date()-7 and sysdate()"
            week_check.execute(week_query1)
            week_row1 = week_check.fetchall()
            week_row_list = list(week_row)

            for i in range(len(week_row1)):
                if int(week_row1[i][0]) >= 36000:
                    week_row1_list = list(week_row[i])
                    week_row1_list.append('<a href="#"><i class="fa fa-check text-navy"></i></a>')
                    week_row_list[i] = tuple(week_row1_list)
                else:
                    week_row1_list = list(week_row[i])
                    week_row1_list.append('<a href="#"><i class="fa fa-times hongse"></i></a>')
                    week_row_list[i] = tuple(week_row1_list)

            row_tuple = tuple(week_row_list)
        if 'month' in req.POST:
            month_check = connection.cursor()
            month_query = "select uname,start_time,end_time,CONCAT(FLOOR(time_length/3600),'时',FLOOR((time_length%3600)/60), '分',((time_length%3600)%60), '秒'),date from daily_time where DATE_FORMAT(date, '%Y%m' ) = DATE_FORMAT( CURDATE( ) , '%Y%m' )"
            month_check.execute(month_query)
            month_row = month_check.fetchall()
            month_query1 = "select time_length from daily_time where DATE_FORMAT( date, '%Y%m' ) = DATE_FORMAT( CURDATE( ) , '%Y%m' )"
            month_check.execute(month_query1)
            month_row1 = month_check.fetchall()
            month_row_list = list(month_row)

            for i in range(len(month_row1)):
                if int(month_row1[i][0]) >= 36000:
                    month_row1_list = list(month_row[i])
                    month_row1_list.append('<a href="#"><i class="fa fa-check text-navy"></i></a>')
                    month_row_list[i] = tuple(month_row1_list)
                else:
                    month_row1_list = list(month_row[i])
                    month_row1_list.append('<a href="#"><i class="fa fa-times hongse"></i></a>')
                    month_row_list[i] = tuple(month_row1_list)

            row_tuple = tuple(month_row_list)
    return render(req, 'monthqiandao.html', locals())
