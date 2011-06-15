#This script updates the stored token for the C2DM notification user
#run this with the first arg as the account password to update the token
import urllib
import sys
import authlib



token = authlib.get_google_authtoken("androidfiledrop", "ac2dm", "droidfiledrop@gmail.com", sys.argv[1])
print "c2dmtoken is: " + token

afdConn = authlib.AppEngineClient("androidfiledrop", "droidfiledrop@gmail.com", sys.argv[1])

print "appengine token is: " + afdConn.ahToken

opener = authlib.get_opener()
opener.addheaders.append(('Cookie', afdConn.ahToken))
data = {}
data['token'] = token

params = urllib.urlencode(data) 
url = "https://androidfiledrop.appspot.com/admin/updatetoken"



f = opener.open(url, params)


print f



