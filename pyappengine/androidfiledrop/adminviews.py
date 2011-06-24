from django.core.urlresolvers import reverse

from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect
from .models import C2DMInfo
from .forms import UpdateTokenForm 
from django.views.generic.simple import direct_to_template

def updatetoken(request):
    view_url = reverse('androidfiledrop.adminviews.updatetoken')
    if request.method == "POST":
        form = UpdateTokenForm(request.POST)
        if form.is_valid():
            form.save()
        return HttpResponseRedirect(view_url)

    form = UpdateTokenForm()
    return direct_to_template(request, 'admin/updatetoken.html',
            {'form' : form, 'uploads' : C2DMInfo.objects.all()})
