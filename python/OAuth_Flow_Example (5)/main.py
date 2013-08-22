#!/usr/bin/python
#
#   Copyright 2013 Smartsheet
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
#Written Python 2.7.3

#5th app for Smartsheet API OAuth flow
import jinja2
import webapp2
import os
import re
import urllib2
import json
import hashlib
from webapp2_extras import sessions

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)))

global client_id 
global app_secret
global baseURL
client_id = 'PUT_YOUR_CLIENT_ID_HERE'
app_secret = 'PUT_YOUR_APP_SECRET_HERE'
baseURL = 'http://localhost:8080'

config = {}
config['webapp2_extras.sessions'] = {
    'secret_key': 'Utoken',
    'secret_key': 'Name',
    'secret_key': 'RefreshToken',
}

def refresh_token(refreshToken):
    req = urllib2.Request('https://api.smartsheet.com/1.1/token')
    SHA256 = hashlib.sha256(app_secret +'|'+ refreshToken)
    _hash = SHA256.hexdigest()
    data = ('grant_type=refresh_token&refresh_token=' + refreshToken + '&client_id=' + client_id + 
    		'&redirect_uri=' + baseURL + '/redirect&hash=' + _hash)
    req.add_data(data)
    resp = urllib2.urlopen(req).read()
    resp = json.loads(resp)
    return resp['refresh_token'], resp['access_token']

def getToken(URI):
	strip = URI.replace('http://localhost:8080/redirect?code=', '')
	auth_code = re.sub('\&.*', '', strip)
	SHA256 = hashlib.sha256(app_secret +'|'+ auth_code)
	hash_ = SHA256.hexdigest()
	data = 'grant_type=authorization_code&code={}&client_id={}&redirect_uri={}/redirect&hash={}'.format(
			auth_code,client_id,baseURL, hash_)

	req = urllib2.Request('https://api.smartsheet.com/1.1/token')
	req.add_data(data)
	resp = urllib2.urlopen(req).read()
	return json.loads(resp)

class BaseHandler(webapp2.RequestHandler):   

    def dispatch(self):                                 
        self.session_store = sessions.get_store(request=self.request)
        try:
            webapp2.RequestHandler.dispatch(self)      
        finally:
            self.session_store.save_sessions(self.response)

    @webapp2.cached_property
    def session(self):
        # Returns a session using the default cookie key.
        return self.session_store.get_session()

class LoginPage(BaseHandler):
	
	def get(self):
		getauth = str('https://www.smartsheet.com/b/authorize?response_type=code&client_id=' + client_id 
		+ '&redirect_uri=' + baseURL + '/redirect&scope=READ_SHEETS,WRITE_SHEETS')
		template = jinja_environment.get_template('index.html')
		if self.session.get("Utoken"):
			email = self.session.get("Name")
			token = self.session.get("Utoken")
			template_values = {'Login_text': 'You are logged in as ' + email,
								'link': '/user',
								'Link_Name': 'Click here to view your user information'}
		else:
			template_values = { 'Login_text' : "You aren't logged into Smartsheet yet",
								'link' : getauth,
								'Link_Name': 'Login With Smartsheet'}
		self.response.out.write(template.render(template_values))


class AuthCode(BaseHandler):

	def get(self):
		if not self.session.get('Utoken'):
			code = str(self.request.uri)
			parsed = getToken(code)
			self.session['Utoken'] = parsed['access_token']
			self.session['RefreshToken'] = parsed['refresh_token']

		self.redirect('/user')
				
class LoggedIn(BaseHandler):

	def get(self):
		def getname():
			getname = urllib2.Request('https://api.smartsheet.com/1.1/user/me')
			getname.add_header("Authorization", " Bearer "+ self.session.get('Utoken'))
			respName = urllib2.urlopen(getname)
			return respName
		parsedName = getname()

		if parsedName.getcode() == 401: #Look at HTTP response code to determine if token is expired
			refresh_access = refresh_token(self.session.get('RefreshToken'))
			self.session['Utoken'] = refresh_access[0]
			self.session['RefreshToken'] = refresh_access[1]
			parsedName = getname()

		parsedName = json.loads(parsedName.read())
		Email = parsedName['email']
		self.session['Name'] = Email
		if 'firstName' in parsedName:
			First = parsedName['firstName']
		else:
			First = ''
		if 'lastName' in parsedName:
			Last = parsedName['lastName']
		else:
			Last = ''
		template = jinja_environment.get_template('user.html')
		template_values = {'First': First,
							'Last' : Last,
							'Email' : Email,
							'link' : '/logout',
							'Link_Name' : 'Log out of Python OAuth2-flow App'}
		self.response.out.write(template.render(template_values))

class Logout(BaseHandler):

	def get(self):
		self.session.clear()
		self.redirect('/')


app = webapp2.WSGIApplication(routes = [
	('/', LoginPage), # Main Welcome Page
	('/redirect', AuthCode ), #Redirect page built for URI that user doesn't see
	('/user', LoggedIn), # page that dispalys the user's info
	('/logout', Logout)], debug=True, config= config)

def main():
    from paste import httpserver
    httpserver.serve(app, host='127.0.0.1', port='8080')

if __name__ == '__main__':
    main()



