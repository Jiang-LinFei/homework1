from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    class Role(models.TextChoices):
        ADMIN = 'admin', '管理员'
        TEACHER = 'teacher', '教师'
        STUDENT = 'student', '学生'

    name = models.CharField('姓名', max_length=50, blank=True)
    role = models.CharField('角色', max_length=10, choices=Role.choices,
                            default=Role.STUDENT)
    phone = models.CharField('电话', max_length=20, blank=True)

    @property
    def is_admin_role(self):
        return self.role == self.Role.ADMIN

    @property
    def is_teacher_role(self):
        return self.role == self.Role.TEACHER

    def __str__(self):
        return f'{self.name or self.username}({self.get_role_display()})'
