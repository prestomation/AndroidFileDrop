from django.db import models

# Create your models here.


class UserManager(models.Manager):
    def get_by_natural_key(self, nickname):
        return self.get(nickname=nickname)

class User(models.Model):
    objects = UserManager()

    userid = models.CharField(primary_key=True, max_length=100)
    #fileblobkey = models.CharField(max_length=200, null=True)
    nickname = models.CharField(max_length=400)

    def natural_key(self):
        return self.nickname

class Device(models.Model):
    user = models.ForeignKey(User)
    nickname = models.CharField(max_length=64)
    deviceid = models.CharField(max_length=200, unique=True)

    def natural_key(self):
        return self.nickname

    class Meta:
        unique_together = (("user", "nickname"),)


class File(models.Model):
    user = models.OneToOneField(User, unique=True)
    #title = models.CharField(max_length=256, blank=True)
    file = models.FileField(upload_to='uploads/%Y/%m/%d/%H/%M/%S/')
    

    @property
    def filename(self):
        return self.file.name.rsplit('/', 1)[-1]

class C2DMInfo(models.Model):
    user = models.CharField(max_length=100, primary_key = True)
    authtoken = models.CharField(max_length = 200)
    password = models.CharField(max_length = 64)

