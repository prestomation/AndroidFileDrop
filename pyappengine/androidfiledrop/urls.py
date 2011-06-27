from django.conf.urls.defaults import *

urlpatterns = patterns('androidfiledrop.views',
    ('api/devices$', 'devices'),
    ('^api/files$', 'files'),
    ('^api/upload/url$', 'uploadurl'),
    ('^api/upload$', 'upload'),
    ('^api/notify$', 'notify'),
    ('^api/notify/(?P<devname>.+)$', 'notify'),
    #('^upload$', 'upload'),
    ('^api/files/(?P<filename>.+)$', 'files'))
