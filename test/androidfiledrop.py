import urllib
import sys
import authlib

user = ""
password = ""


if user.find("@") == -1:
    user = user + "@gmail.com"



print user
afdConn = authlib.AppEngineClient("androidfiledrop", user, password)


avar = afdConn.uploadFile("authlib.py")
print avar.read()
