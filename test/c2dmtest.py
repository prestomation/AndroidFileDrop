#!/usr/bin/python

import authlib


ACCOUNT_EMAIL= 'droidfiledrop@gmail.com'
ACCOUNT_PASS = "androidfiledrop"

C2DM_ID = "APA91bG_wjhPPoM1SHfNWE-yWs2sUCiHEv0osbDx2xv1hlayuu6hS6iW3gqb_MT9ISAof1uRAypQQ2B55zMoJVzp6omEPs7RYwpGoXpA9bnuQ_b4VVI0q2wVRV3X1S_CR9qRw6hhtfAJ"


c2dmtoken = authlib.get_google_authtoken("androidfiledrop", "ac2dm", ACCOUNT_EMAIL, ACCOUNT_PASS)

print authlib.testC2DM(c2dmtoken, C2DM_ID, "something")

