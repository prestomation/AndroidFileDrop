from django import forms
from .models import C2DMInfo

class UploadForm(forms.Form):
    file = forms.FileField()

class UpdateTokenForm(forms.ModelForm):
    class Meta:
        model = C2DMInfo

