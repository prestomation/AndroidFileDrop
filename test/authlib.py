#http://code.activestate.com/recipes/577217-routines-for-programmatically-authenticating-with-

## {{{ http://code.activestate.com/recipes/577217/ (r2)
"""
Routines for programmatically authenticating with the Google Accounts system at
Google App-Engine.

This takes two calls, one to the ClientLogin service of Google Accounts,
and then a second to the login frontend of App Engine.

User credentials are provided to the first, which responds with a token.
Passing that token to the _ah/login GAE endpoint then gives the cookie that can
be used to make further authenticated requests.

Give the ACSID cookie to the client so it stays logged in with the GAE integrated users
system. 

One last issue, after succesful authentication the current user's ID is still 
missing; User(email).user_id() won't work.  Here I think a HTTP redirect 
should make the client re-request (using the cookie) and login, but the client 
would need to support that. Alternatively the ID can be fetched within the 
current request by a r/w round trip to the datastore, see: 
http://stackoverflow.com/questions/816372/how-can-i-determine-a-user-id-based-on-an-email-address-in-app-engine

See also: http://markmail.org/thread/tgth5vmdqjacaxbx
"""
import logging, md5, urllib, urllib2, cookielib

#This import is from easy_install or
#http://pipe.scs.fsu.edu/PostHandler/MultipartPostHandler.py
import MultipartPostHandler


def get_google_authtoken(appname, service, email_address, password):
    """
    Make secure connection to Google Accounts and retrieve an authorisation
    token for the stated appname.

    The token can be send to the login front-end at appengine using
    get_gae_cookie(), which will return a cookie to use for the user session.
    """
    opener = get_opener()

    # get an AuthToken from Google accounts
    auth_uri = 'https://www.google.com/accounts/ClientLogin'
    authreq_data = urllib.urlencode({ "Email":   email_address,
                                      "Passwd":  password,
                                      "service": service,
                                      "source":  appname,
                                      "accountType": "HOSTED_OR_GOOGLE" })
    req = urllib2.Request(auth_uri, data=authreq_data)
    try:
        response = opener.open(req)
        response_body = response.read()
        response_dict = dict(x.split("=")
                             for x in response_body.split("\n") if x)
        return response_dict["Auth"]
    except urllib2.HTTPError, e:
        if e.code == 403:
            body = e.read()
            response_dict = dict(x.split("=", 1) for x in body.split("\n") if x)
            raise AuthError(req.get_full_url(), e.code, e.msg,
                                   e.headers, response_dict)
        else:
            raise

class AuthError(urllib2.HTTPError):
    """Raised to indicate there was an error authenticating."""

    def __init__(self, url, code, msg, headers, args):
        urllib2.HTTPError.__init__(self, url, code, msg, headers, None)
        self.args = args
        self.reason = args["Error"]

def get_opener(cookiejar=None, redirect=False ):
    opener = urllib2.OpenerDirector()
    opener.add_handler(urllib2.ProxyHandler())
    opener.add_handler(urllib2.UnknownHandler())
    opener.add_handler(urllib2.HTTPHandler())
    opener.add_handler(urllib2.HTTPDefaultErrorHandler())
    if redirect:
        opener.add_handler(urllib2.HTTPRedirectHandler())
    opener.add_handler(urllib2.HTTPErrorProcessor())
    opener.add_handler(urllib2.HTTPSHandler())
    opener.add_handler(MultipartPostHandler.MultipartPostHandler())
    if cookiejar:
        opener.add_handler(urllib2.HTTPCookieProcessor(cookiejar))
    return opener
## end of http://code.activestate.com/recipes/577217/ }}}


def testC2DM(authtoken, c2dmRegistrationID, collapseKey):
    if c2dmRegistrationID is None:
        return None
    
    values = {'registration_id' : c2dmRegistrationID,

              'collapse_key' : collapseKey}       

     # BUILD REQUEST

    headers = {'Authorization': 'GoogleLogin auth=' + authtoken}

    data = urllib.urlencode(values)

    request = urllib2.Request("http://android.apis.google.com/c2dm/send", data, headers)

    # POST

    try:
        response = urllib2.urlopen(request)

        responseAsString = response.read()



        return responseAsString

    except urllib2.HTTPError, e:

          print 'HTTPError ' + str(e)




class AppEngineClient():

    def __init__(self, appname, user, password, dev=False, admin=False):
        self.ahCookie = self.do_auth(appname, "ah", user, password, dev, admin)
        self.appname = appname
        self.user = user
        self.dev = dev
         

    def do_auth(self, appname, service, user, password, dev=False, admin=False):
        "This is taken from bits of appcfg, specifically: "
        " google/appengine/tools/appengine_rpc.py "
        "It returns the cookie send by the App Engine Login "
        "front-end after authenticating with Google Accounts. "

        if dev:
            return self.do_auth_dev_appserver(user, admin)

        # get the token
        try:
            auth_token = get_google_authtoken(appname, service, user, password)
        except AuthError, e:
            if e.reason == "BadAuthentication":
                logging.error( "Invalid username or password." )
            if e.reason == "CaptchaRequired":
                logging.error( 
                    "Please go to\n"
                    "http://www.google.com/accounts/DisplayUnlockCaptcha\n"
                    "and verify you are a human.  Then try again.")
            if e.reason == "NotVerified":
                logging.error( "Account not verified.")
            if e.reason == "TermsNotAgreed":
                logging.error( "User has not agreed to TOS.")
            if e.reason == "AccountDeleted":
                logging.error( "The user account has been deleted.")
            if e.reason == "AccountDisabled":
                logging.error( "The user account has been disabled.")
            if e.reason == "ServiceDisabled":
                logging.error( "The user's access to the service has been "
                                     "disabled.")
            if e.reason == "ServiceUnavailable":
                logging.error( "The service is not available; try again later.")
            raise

        # now get the cookie
        cookie = self.get_gae_cookie(appname, auth_token)
        assert cookie
        return cookie

    def do_auth_dev_appserver(self, email, admin):
        """Creates cookie payload data.

        Args:
        email, admin: Parameters to incorporate into the cookie.

        Returns:
        String containing the cookie payload.
        """
        admin_string = 'False'
        if admin:
            admin_string = 'True'
        if email:
            user_id_digest = md5.new(email.lower()).digest()
            user_id = '1' + ''.join(['%02d' % ord(x) for x in user_id_digest])[:20]
        else:
            user_id = ''
        return 'dev_appserver_login="%s:%s:%s"; Path=/;' % (email, admin_string, user_id)

    def get_gae_cookie(self, appname, auth_token):
        """
        Send a token to the App Engine login, again stating the name of the
        application to gain authentication for. Returned is a cookie that may be used
        to authenticate HTTP traffic to the application at App Engine.
        """

        continue_location = "http://localhost/"
        args = {"continue": continue_location, "auth": auth_token}
        host = "%s.appspot.com" % appname
        url = "http://%s/_ah/login?%s" % (host,
                                   urllib.urlencode(args))

        self.cookies = cookielib.CookieJar()
        opener = get_opener(self.cookies) # no redirect handler!
        req = urllib2.Request(url)
        try:
            response = opener.open(req)
        except urllib2.HTTPError, e:
            response = e

        if (response.code != 302 or 
                response.info()["location"] != continue_location):
            raise urllib2.HTTPError(req.get_full_url(), response.code, 
                    response.msg, response.headers, response.fp)

        cookie = response.headers.get('set-cookie')
        assert cookie and cookie.startswith('ACSID')
        return cookie.replace('; HttpOnly', '')



    def registerDevice(self, devID):
        opener = get_opener()
        opener.addheaders.append(('Cookie', self.ahCookie))
        data = {}
        data['devregid'] = devID
        
        params = urllib.urlencode(data) 
        if self.dev:
            url = "http://localhost:8888/register"
        else:
            url = "http://androidfiledrop.appspot.com/register"



        f = opener.open(url, params)
        return f

    def getUploadUrl(self):
        opener = get_opener()
        opener.addheaders.append(('Cookie', self.ahCookie))

        if self.dev:
            url = "http://localhost:8888/upload/geturl"
        else:
            url = "http://androidfiledrop.appspot.com/upload/geturl"

        return opener.open(url).read()


    
    def uploadFile(self, filepath):

        uploadpath = self.getUploadUrl()

        #cookies = cookielib.CookieJar()
        

        #opener = urllib2.build_opener(MultipartPostHandler.MultipartPostHandler) 
        opener = get_opener(self.cookies, False)
        opener.addheaders.append(('Cookie', self.ahCookie))

        params = { "myFile" : open(filepath, "rb")}
                

        try:
            returnval =opener.open(uploadpath, params)
            print returnval.read()
        except urllib2.HTTPError, e:
            print e.info()
            print e.msg
            print dir(e)
            if e.getcode() != 302:
                raise



        return self.notify()

    def notify(self):

        opener = get_opener()
        opener.addheaders.append(('Cookie', self.ahCookie))
        return opener.open("http://androidfiledrop.appspot.com/notify")


                    

