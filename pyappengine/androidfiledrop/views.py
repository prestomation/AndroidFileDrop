import logging
logging.getLogger().setLevel(logging.DEBUG)
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect, HttpResponseBadRequest, HttpResponseServerError
from django.utils import simplejson
from django.shortcuts import get_object_or_404
import uuid
from django.core.urlresolvers import reverse
from google.appengine.api import users

C2DMUSER = "droidfiledrop@gmail.com"


class RequireGAEAuth(object):
    #Wrap a View to require authentication and pass the current GAE user
    def __init__(self, func):
        self.func = func

    def __call__(self, request, **kwargs):
        user = users.get_current_user()
        if not user:
            #make them login
            logging.info("No user logged in: redirecting..")
            return HttpResponseRedirect(users.create_login_url(request.get_full_path()))
        else:
            logging.info("User %s logged in!" % user.nickname())
            return self.func(user, request, kwargs)

class RequireAFDUser(object):
    #Wrap a View to require authentication and pass the current AFD user
    def __init__(self, func):
        self.func = func

    def __call__(self,  request, **params):
        #TODO Eliminate duplication with RequireGAEAuth
        user = users.get_current_user()
        if not user:
            #make them login
            return HttpResponseRedirect(users.create_login_url(request.get_full_path()))
        else:
            try:
                user = User.objects.get(userid = user.user_id())
            except User.DoesNotExist:
                return HttpResponse("No devices registered")
            else:
                return self.func(user, request, params)

from models import User, Device
@RequireGAEAuth
def devices(currentuser, request, params):
    """
    Registers a device on POST
    Gets user's device list on GET
    Deletes device on DELETE
    """

    if request.method == "GET":
        logging.info("a device get")
        try:
            user = User.objects.get(userid = currentuser.user_id())
        except User.DoesNotExist:
            return HttpResponse("User does not exist!", content_type="text/plain")
        logging.info("got user %s" % user.nickname)
        userdevices = Device.objects.filter(user = user)
        result = {}
        for dev in userdevices:
            result[dev.nickname] = dev.c2dmid

        return HttpResponse(simplejson.dumps([result]), mimetype="application/json")

    elif request.method == "POST":
        logging.info("a device POST")
        c2dmID = request.POST.get("deviceRegID", None)
        if not c2dmID:
            #Must register with C2DM ID
            return HttpResponseBadRequest("Bad Request", content_type="text/plain")
        nickname = request.POST.get("nickname", "MyDevice")
        deviceID = request.POST.get("devid", uuid.uuid4().hex)
        try:
            #Get the user if they already exist
            user = User.objects.get(userid = currentuser.user_id())
            logging.info("user %s exists!" % user.nickname )
        except User.DoesNotExist:
            #Otherwise, create a new one
            user = User(userid = currentuser.user_id(), nickname = currentuser.nickname())
            logging.info("created new user %s!" % user.nickname )
            user.save()
        try:
            #If they are reregistering this ID, update the name/c2dm id
            #TODO: does Djano/DB enforce this for us?
            device = Device.objects.get(user = user, deviceid = deviceID)
            device.nickname = nickname
            device.c2dmid = c2dmID
            device.deviceid = deviceID
        except Device.DoesNotExist:
            device = Device(user = user, nickname = nickname, c2dmid = c2dmID, deviceid = deviceID)
            logging.info("Created device, nickname: %s c2dmid %s deviceid %s" % (device.nickname, device.c2dmid, device.deviceid ))
        device.save()

        return HttpResponse( "User: %s Nickname: %s\nDevice: %s C2DM: %s" % (user.userid, user.nickname, device.nickname, device.c2dmid))

    elif request.method == "DELETE":
        #delete given device
        #Delete user if last device
        pass

    else:
        return HttpResponse("Bad Request", content_type="text/plain")


from .forms import UploadForm
from .models import File
from django.core.urlresolvers import reverse
from django.views.generic.simple import direct_to_template

from filetransfers.api import prepare_upload, serve_file

@RequireAFDUser
def files(currentuser, request, params):
    """Upload and download files"""
    filename = params.get('filename', None)
    if request.method == "GET":
        try:
            upload = File.objects.get(user = currentuser)
        except File.DoesNotExist:
            return HttpResponseNotFound("File not found")

        
        #Return the file if they are asking for the right one AND they own it
        return serve_file(request, upload.file, save_as=True) if currentuser == upload.user and upload.filename == filename else HttpResponseNotFound("File not found")



    elif request.method == "POST":
        form = UploadForm(request.POST, request.FILES)
        if form.is_valid():
            newfile = File(user = currentuser, file = form.cleaned_data['file']) 
            newfile.save()
        return HttpResponseRedirect(reverse('androidfiledrop.views.upload'))


@RequireAFDUser
def upload(currentuser, request, params):

    form = UploadForm()
    view_url = reverse(files)
    upload_url, upload_data = prepare_upload(request, view_url)
    return direct_to_template(request, "api/upload.html", {'form' : form, 'upload_url' : upload_url, 'upload_data' : upload_data})

@RequireAFDUser
def uploadurl(currentuser, request, params):
    view_url = reverse(files)
    upload_url, upload_data = prepare_upload(request, view_url)
    return HttpResponse(upload_url)

from .models import C2DMInfo
import urllib
import urllib2

@RequireAFDUser
def notify(currentuser, request, params):
    try:
        devID = Device.objects.get(user= currentuser, nickname = params['devname']).c2dmid
    except Device.DoesNotExist:
        logging.warning("device does not exist: " + params['devname'])
        return HttpResponseNotFound("No such device")

    logging.info("User %s notifying device %s" % (currentuser.nickname, devID))
    try:
        filename = File.objects.get(user=currentuser).filename
    except File.DoesNotExist:
        logging.warning("User has no file")
        return HttpResponseNotFound("You don't have a file!")

    try:
        c2dmUser = C2DMInfo.objects.get(user=C2DMUSER)
        authToken = c2dmUser.authtoken
    except C2DMInfo.DoesNotExist:
        return HttpResponseServerError("Error!")

    try:
        return notifyHelper(devID, filename, authToken)
    except urllib2.HTTPError, e:
        #Token expired, get a new one
        if e.code == 401:
            logging.error("Auth expired")
            newToken = get_google_authtoken(c2dmUser.user, c2dmUser.password)
            logging.error("new token: " + newToken)
            c2dmUser.authtoken = newToken
            c2dmUser.save()
            return notifyHelper(devID, filename, newToken)
        else:
            raise



def notifyHelper(deviceID, filename, authtoken):
    values = {
            'data.filename' : filename, #"data." gets this into the message bundle
            'registration_id' : deviceID,
            'collapse_key' : "akey" #doesn't mean anything
            }
    body = urllib.urlencode(values)
    request = urllib2.Request('http://android.clients.google.com/c2dm/send', body)
    request.add_header('Authorization', 'GoogleLogin auth=' + authtoken)
    response = urllib2.urlopen(request) 
    logging.info("Response Code: " + str(response.code))
    if (response.code == 200):
        logging.info("Success for file %s" % ( filename))
        return HttpResponse("OK!")
    return HttpResponse("Not ok :(")






def get_google_authtoken(email_address, password):
    """
    Make secure connection to Google Accounts and retrieve an authorisation
    token for the stated appname.
    """
    #opener = get_opener()

    # get an AuthToken from Google accounts
    auth_uri = 'https://www.google.com/accounts/ClientLogin'
    authreq_data = urllib.urlencode({ "Email":   email_address,
                                      "Passwd":  password,
                                      "service": "ac2dm",
                                      "source":  "androidfiledroptest",
                                      "accountType": "HOSTED_OR_GOOGLE" })
    req = urllib2.Request(auth_uri, data=authreq_data)
    response = urllib2.urlopen(req)
    #response = opener.open(req)
    response_body = response.read()
    response_dict = dict(x.split("=")
                             for x in response_body.split("\n") if x)
    return response_dict["Auth"]


