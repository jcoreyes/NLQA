#!/usr/bin/python
import urllib
import cgi, cgitb 
cgitb.enable() 

data = cgi.FieldStorage()

print "Content-Type: text/html"
print data

filehandle = urllib.urlopen("https://www.google.com")