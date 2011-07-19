#!/usr/bin/python

import authlib


ACCOUNT_EMAIL= 'droidfiledrop@gmail.com'
ACCOUNT_PASS = ""

C2DM_ID = "APA91bEonT6sDi3s5k82VGSgGHb6vxUE6TjSyPiHD6c9qJS3OK1p4wretjOzW6M35W2agNqW2U6NX147xGyjRZOPVuvkd_cohFU9hwqoyliPGyqmwpSqBnIMWlf52W9IssS26Cgq6421"


c2dmtoken = authlib.get_google_authtoken("androidfiledrop", "ac2dm", ACCOUNT_EMAIL, ACCOUNT_PASS)
print c2dmtoken

#uncomment to send c2dm message
print authlib.testC2DM(c2dmtoken, C2DM_ID, "something")

