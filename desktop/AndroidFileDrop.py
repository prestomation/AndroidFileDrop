#!/usr/bin/python

"""
2011 Preston Tamkin
This is a simple Python script to create a GUI uploader for AndroidFileDrop
Its sole dependency beyond Python itself is PySide for Qt bindings

It is ran with one argument, the path to the file to upload, and prompts the user for login info
"""

 
import sys
import urllib, urllib2
import authlib
from PySide import  QtCore, QtGui
 
class AuthDialog(QtGui.QDialog):
    def __init__(self, app,filename, authtoken):
        super(AuthDialog, self).__init__(None)

        if filename is None:
            QtGui.QMessageBox.critical(None,"AndroidFileDrop",  "Please provide a filename on the commandline")
            app.exit()
            return

        self.filename = filename
        userLabel = QtGui.QLabel("Google Login:")
        self.userInput = QtGui.QLineEdit()

        passwordLabel = QtGui.QLabel("Password:")
        self.passwordInput = QtGui.QLineEdit()

        self.authtoken = authtoken

        self.passwordInput.setEchoMode(self.passwordInput.Password)
        self.busyBar = QtGui.QProgressBar()
        self.busyBar.setRange(0,0)
        self.busyBar.hide()

        buttonBox = QtGui.QDialogButtonBox(QtGui.QDialogButtonBox.Ok | QtGui.QDialogButtonBox.Cancel)

        buttonBox.accepted.connect(self.accept)
        buttonBox.rejected.connect(self.reject)

        mainLayout = QtGui.QVBoxLayout()
        mainLayout.addWidget(userLabel)
        mainLayout.addWidget(self.userInput)
        mainLayout.addWidget(passwordLabel)
        mainLayout.addWidget(self.passwordInput)
        mainLayout.addWidget(self.busyBar)
        mainLayout.addWidget(buttonBox)

        self.setLayout(mainLayout)
        self.setWindowTitle("AndroidFileDrop")

    def accept(self):
        self.busyBar.show()
        user = self.userInput.text()
        if user.find("@") == -1:
            user = user + "@gmail.com"
        password = self.passwordInput.text() 
        self.uploadthread = UploadThread(user, password, self.filename)
        self.uploadthread.uploadComplete.connect(self.uploadCallback, QtCore.Qt.QueuedConnection)
        self.uploadthread.start()
       


    def uploadCallback(self, e):

        self.busyBar.hide()
        if e is None:
            QtGui.QMessageBox.information(None, "AndroidFileDrop", "File uploaded! Your phone should download your file shortly.")
            self.close()

        if isinstance(e, authlib.AuthError):
            QtGui.QMessageBox.critical(None,"AndroidFileDrop",  "Invalid username or password")
            return
        if isinstance(e, urllib2.HTTPError):
            QtGui.QMessageBox.critical(None,"AndroidFileDrop",  "There was an error.\nIs AndroidFileDrop configured on your mobile device?")
            return

            

class UploadThread(QtCore.QThread):
    uploadComplete = QtCore.Signal(BaseException)
    
    def __init__(self, user, password, filename):
        QtCore.QThread.__init__(self)
        self.user = user
        self.password = password
        self.filename = filename
    def run(self):
        try:
            afdConn = authlib.AppEngineClient("androidfiledrop", self.user, self.password)
            avar  = afdConn.uploadFile(self.filename)
            self.uploadComplete.emit(None)
        except BaseException, e:
            self.uploadComplete.emit(e)


    
# Create a Qt application 
app = QtGui.QApplication(sys.argv)
filename = None
if len(sys.argv) >= 2:
    filename = sys.argv[1]

settings = QtCore.QSettings("AndroidFileDrop", "Prestomation")
authtoken = settings.value("authtoken",None)
authdialog = AuthDialog(app,filename, authtoken)
# Enter Qt application main loop
authdialog.exec_()
sys.exit()
