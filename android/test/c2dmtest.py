#/usr/bin/python

import urllib, urllib2



#Borrowed from http://blog.boxedice.com/2010/10/07/android-push-notifications-tutorial/
class ClientLoginTokenFactory():

  _token = None



  def __init__(self):

      self.url = 'https://www.google.com/accounts/ClientLogin'

      self.accountType = 'GOOGLE'

      self.email = 'droidfiledrop@gmail.com'

      self.password = 'androidfiledrop'

      self.source = 'androidfiledroptest'

      self.service = 'ac2dm'



  def getToken(self):

      if self._token is None:



          # BUILD PAYLOAD

          values = {'accountType' : self.accountType,

                    'Email' : self.email,

                    'Passwd' : self.password,

                    'source' : self.source,

                    'service' : self.service}



          # BUILD REQUEST

          data = urllib.urlencode(values)

          request = urllib2.Request(self.url, data)



          # POST

          response = urllib2.urlopen(request)

          responseAsString = response.read()



          # FORMAT RESPONSE

          responseAsList = responseAsString.split('\n')



          self._token = responseAsList[2].split('=')[1]



      return self._token

class C2DM():
  def __init__(self):

      self.url = 'https://android.apis.google.com/c2dm/send'

      self.clientAuth = None

      self.registrationId = None

      self.collapseKey = None

      self.data = {}



  def sendMessage(self,data ):

      if self.registrationId == None or self.collapseKey == None:

          return False



      clientAuthFactory = ClientLoginTokenFactory()

      self.clientAuth = clientAuthFactory.getToken()



      # LOOP OVER ANY DATA WE WANT TO SEND

      for k, v in data.iteritems():

          self.data['data.' + k] = v



      # BUILD PAYLOAD

      values = {'registration_id' : self.registrationId,

                'collapse_key' : self.collapseKey}       



      # BUILD REQUEST

      headers = {'Authorization': 'GoogleLogin auth=' + self.clientAuth}

      data = urllib.urlencode(values)

      request = urllib2.Request(self.url, data, headers)



      # POST

      try:

          response = urllib2.urlopen(request)

          responseAsString = response.read()



          return responseAsString

      except urllib2.HTTPError, e:

            print 'HTTPError ' + str(e)


sender = C2DM()
sender.registrationId = "APA91bG_wjhPPoM1SHfNWE-yWs2sUCiHEv0osbDx2xv1hlayuu6hS6iW3gqb_MT9ISAof1uRAypQQ2B55zMoJVzp6omEPs7RYwpGoXpA9bnuQ_b4VVI0q2wVRV3X1S_CR9qRw6hhtfAJ"
sender.collapseKey = 1
data = {'message' : 'Hello there', 'hats' : 54}
response = sender.sendMessage(data)


