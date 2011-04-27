import authlib

DEV_ID = "APA91bG_wjhPPoM1SHfNWE-yWs2sUCiHEv0osbDx2xv1hlayuu6hS6iW3gqb_MT9ISAof1uRAypQQ2B55zMoJVzp6omEPs7RYwpGoXpA9bnuQ_b4VVI0q2wVRV3X1S_CR9qRw6hhtfAJ"
afdConn = authlib.AppEngineClient("androidfiledrop", "prestomation@gmail.com", sys.argv[1], True)
print afdConn.registerDevice(DEV_ID)


