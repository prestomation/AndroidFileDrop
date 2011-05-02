import urllib
import sys
import authlib

user = "prestomation"
password = "gnwwclwrxjehttrx"

if user.find("@") == -1:
    user = user + "@gmail.com"



afdConn = authlib.AppEngineClient("androidfiledrop", user, password)


avar = afdConn.uploadFile(sys.argv[1])
print avar.read()
