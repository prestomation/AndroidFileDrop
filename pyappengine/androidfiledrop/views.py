#from django.conf import settings
import logging
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect
from django.utils import simplejson
from django.shortcuts import get_object_or_404

from django.core.urlresolvers import reverse


from google.appengine.api import users



class RequireGAEAuth(object):
    #Wrap a View to require authentication and pass the current GAE user
    def __init__(self, func):
        self.func = func

    def __call__(self, request, **kwargs):
        user = users.get_current_user()
        if not user:
            #make them login
            return HttpResponseRedirect(users.create_login_url(request.get_full_path()))
        else:
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
        user = User.objects.get(userid = currentuser.user_id())
        userdevices = Device.objects.filter(user = user)
        result = {}
        for dev in userdevices:
            result[dev.nickname] = dev.deviceid

        return HttpResponse(simplejson.dumps([result]), mimetype="application/json")

    elif request.method == "POST":
        deviceID = request.POST.get("deviceRegID", None)
        if not deviceID:
            #Must register with C2DM ID
            return HttpResponse("Bad Request", content_type="text/plain")
        nickname = request.POST.get("nickname", "MyDevice")

        try:
            user = User.objects.get(userid = currentuser.user_id())
        except User.DoesNotExist:
            user = User(userid = currentuser.user_id(), nickname = currentuser.nickname())
            user.save()
        try:
            device = Device.objects.get(deviceid = deviceID)
            device.nickname = nickname
        except Device.DoesNotExist:
            device = Device(user = user, nickname = nickname, deviceid = deviceID)
        device.save()

        return HttpResponse( "User: %s Nickname: %s\nDevice: %s C2DM: %s" % (user.userid, user.nickname, device.nickname, device.deviceid))

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
        devID = Device.objects.get(user= currentuser, nickname = params['devname']).deviceid
    except Device.DoesNotExist:
        return HttpResponseNotFound("No such device")

    logging.info("User %s notifying device %s" % (currentuser.nickname, devID))
    try:
        filename = File.objects.get(user=currentuser).filename
    except File.DoesNotExist:
        return HttpResponseNotFound("You don't have a file!")

    authtoken = C2DMInfo.objects.get(user="droidfiledrop@gmail.com").authtoken

    values = {
            'registration_id' : devID,
            'collapse_key' : "akey", #doesn't mean anything
            'File' : "filename"
            }
    body = urllib.urlencode(values)
    request = urllib2.Request('http://android.clients.google.com/c2dm/send', body)
    request.add_header('Authorization', 'GoogleLogin auth=' + authtoken)

    response = urllib2.urlopen(request)
    logging.info("Response Code: " + response.code)
    if (response.code == 200):
        logging.info("Success for user %s and file %s" % ( currentuser.nickname, filename))
        return HttpResponse("OK!")
    return HttpResponse("Not ok :(")








