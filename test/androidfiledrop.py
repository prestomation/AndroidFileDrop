#!/usr/bin/env python

import pygtk
pygtk.require('2.0')
import gtk

class HelloWorld2:

    # Our new improved callback.  The data passed to this method
    # is printed to stdout.
    def callback(self, widget, data):

        if data == "cancel":
            gtk.main_quit()
            return

        import urllib
        import sys
        import authlib


        user = self.usernameentry.get_text()
        password = self.passwordentry.get_text() 
        if user.find("@") == -1:
            user = user + "@gmail.com"

        afdConn = authlib.AppEngineClient("androidfiledrop", user, password)


        avar = afdConn.uploadFile(sys.argv[1])
        gtk.main_quit()
        return


    # another callback
    def delete_event(self, widget, event, data=None):
        gtk.main_quit()
        return False

    def __init__(self):
        # Create a new window
        self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)

        # This is a new call, which just sets the title of our
        # new window to "Hello Buttons!"
        self.window.set_title("AndroidFileDrop")

        # Here we just set a handler for delete_event that immediately
        # exits GTK.
        self.window.connect("delete_event", self.delete_event)

        # Sets the border width of the window.
        self.window.set_border_width(10)

        # We create a box to pack widgets into.  This is described in detail
        # in the "packing" section. The box is not really visible, it
        # is just used as a tool to arrange widgets.
        self.vbox = gtk.VBox(False, 0)
        self.buttonbox = gtk.HBox(False, 0)
        self.textentrybox = gtk.HBox(False, 0)

        # Put the box into the main window.
        self.window.add(self.vbox)


        self.usernameentry = gtk.Entry(50)
        self.passwordentry = gtk.Entry(50)
        self.passwordentry.set_visibility(False)
        self.textentrybox.pack_start(self.usernameentry, True, True, 0)
        self.textentrybox.pack_start(self.passwordentry, True, True, 0)

        # Creates a new button with the label "Button 1".
        self.button1 = gtk.Button("Authenticate")

        # Now when the button is clicked, we call the "callback" method
        # with a pointer to "button 1" as its argument
        self.button1.connect("clicked", self.callback, "authenticate")
        self.button1.set_flags(gtk.CAN_DEFAULT)

        # Instead of add(), we pack this button into the invisible
        # box, which has been packed into the window.
        self.buttonbox.pack_start(self.button1, True, True, 0)

        # Always remember this step, this tells GTK that our preparation for
        # this button is complete, and it can now be displayed.
        self.button1.show()

        # Do these same steps again to create a second button
        self.button2 = gtk.Button("Cancel")

        # Call the same callback method with a different argument,
        # passing a pointer to "button 2" instead.
        self.button2.connect("clicked", self.callback, "cancel")

        self.buttonbox.pack_start(self.button2, True, True, 0)

        # The order in which we show the buttons is not really important, but I
        # recommend showing the window last, so it all pops up at once.
        self.vbox.pack_start(self.textentrybox, True, True, 0)
        self.vbox.pack_start(self.buttonbox, True, True, 0)
        self.window.set_default(self.button1)

        self.button2.show()
        self.usernameentry.show()
        self.passwordentry.show()
        self.buttonbox.show()
        self.textentrybox.show()
        self.vbox.show()
        self.window.show()

def main():
    gtk.main()

if __name__ == "__main__":
    hello = HelloWorld2()
    main()
