"""LaboratoryManagementSystem URL Configuration"""
from django.contrib import admin
from django.urls import path, re_path
from django.conf import settings
from django.conf.urls.static import static

from LMS import views

urlpatterns = [
    path('admin/', admin.site.urls),
    re_path(r'^$', views.login, name='login'),
    re_path(r'^login/$', views.loginVerify, name='loginVerify'),
    re_path(r'^logout/$', views.logout, name='logout'),
    re_path(r'^index/$', views.index, name='index'),
    re_path(r'^register_show/$', views.inregister, name='show_register'),
    re_path(r'^register/$', views.register, name='register'),
    re_path(r'^allTask/$', views.allTask, name='allTask'),
    re_path(r'^acceptTask/$', views.acceptTask, name='acceptTask'),
    re_path(r'^abandonTask/$', views.abandonTask, name='abandonTask'),
    re_path(r'^myWork/$', views.myWork, name='myWork'),
    re_path(r'^attendance/$', views.attendance, name='attendance'),
    re_path(r'^qingjia/$', views.qingjia, name='qingjia'),
    re_path(r'^monthqiandao/$', views.attendance_check, name='monthqiandao'),
    re_path(r'^upload_ajax/$', views.upload_ajax, name='upload_ajax'),
    re_path(r'^eachWork/$', views.eachWork, name='eachWork'),
    re_path(r'^getWorkId/$', views.getWorkId, name='getWorkId'),
    re_path(r'^editMyWork/$', views.editMyWork, name='editMyWork'),
    re_path(r'^getFileInfo/$', views.getFileInfo, name='getFileInfo'),
    re_path(r'^download_file/$', views.download_file, name='download_file'),
]

if settings.DEBUG:
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)
