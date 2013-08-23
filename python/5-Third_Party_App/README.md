##OAuth2 Flow: Documentation (Python)##
==========
####This is the 5th example in our series of introductions to using the Smartsheet API. See our `Hellosmartsheet`, `Sheet_Structure`, `File Attachment` and `Admin Controls` scripts inside our samples project for additional hands-on introductions to the Smartsheet API and how to use it with Python. In this example, we demonstrate how you can integrate your 3rd party app with the Smartsheet API and obtain a user's access token using OAuth2. 
-------
####Web Framework and Libraries####
In order to demostrate how the OAuth2 flow works in a web application, we'll need to use a web framework to serve a login page and pass the credentials to the Smartseheet API. I chose <a href="http://webapp-improved.appspot.com/"> webapp2</a> for its lightweight and simplicity. I also use <a href="http://jinja.pocoo.org/docs/">jinja2</a> as a templating language for my html files. You can install both of these modules by dropping these lines into your terminal. 

	pip install webapp2
	pip install jinja2
	
Here are all of the modules you'll need for the app:

	import jinja2
	import webapp2
	import os
	import re
	import urllib2
	import json
	import hashlib
	from webapp2_extras import sessions

Aside from webapp2, jinja2 and the other usual libraries, I'd like to call attention to <b>hashlib</b> and <b>sessions.</b> You'll need <b>hashlib</b> to convert the authorizartion code received from Smartsheet to a SHA-256 hash. 



####Create a Developer Account####

You can register for a developer account in <a href="http://smartsheet.com/developers"> Smartsheet Developer Portal</a>. Here you can sign up for an account and then create a new third party app for Smartsheet. When you set this up, you'll need to specify the app name, description and redirect URI. It is important that your redirect URI matches the URI that your app is expecting to handle. You'll then be given an app client id and an app secret. You should not share the app secret with anyone. 

####Setup####

With the jinja2 template framework, you'll need to establish a jinja enviornment. 

	jinja_environment = jinja2.Environment(
    	loader=jinja2.FileSystemLoader(os.path.dirname(__file__)))

I put in my 'client_id', 'app_secret' and base URL (I used the localhost on port 8080) as global variables in this app. 

	client_id = 'PUT_YOUR_CLIENT_ID_HERE'
	app_secret = 'PUT_YOUR_APP_SECRET_HERE'
	baseURL = 'http://localhost:8080'
	
To set up session handling in webapp2 I established a secret_key for each value that I wanted to keep in the session. 


	config = {}
	config['webapp2_extras.sessions'] = {
    	'secret_key': 'Utoken',
    	'secret_key': 'Name',
    	'secret_key': 'RefreshToken',
	}

I also put in logic to store session information in the BaseHandler class

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
        	
####Getting the Authorization Code####

The first step in the Smartsheet OAuth2 flow is to redirect the user to the Smartsheet login page with your client_id, redirect_uri and scope. 

	getauth = str('https://www.smartsheet.com/b/authorize?response_type=code&client_id=' + client_id 
		+ '&redirect_uri=' + baseURL + '/redirect&scope=READ_SHEETS,WRITE_SHEETS')

I render a link in my main page (index.html) with all of this information.

- client_id: This is generated after you resgiter your third party app
- redirect_uri: This is the URI that Smartsheet will send your authorization code back to after the user completes the login process. Make sure that your app is handling this correctly
- scope: This lets you define what access your app is requesting for a user's sheets. More information about the different scopes in Smartsheet is available in our <a href= http://www.smartsheet.com/developers/api-documentation#h.89hb3ivv7eum> API Documentation</a>.

Smartsheet passes back the authorization code in the URI. I then pull the auth code from the URI and then put together the parameters that will let me obtain the user's access token.

####Obtaining the Access Token####

In this method I take the auth code and concatenate it with the app secret and a pipe. I then put it in a SHA-25 hash and return it as a hexidecimal. 

	SHA256 = hashlib.sha256(app_secret +'|'+ auth_code)
	hash_ = SHA256.hexdigest()

I then put together my URL with the follwoing parameters; grant_type, authorization code, client_id, redirect_uri and hash. I then make the call to https://api.smartsheet.com/1.1/token with the parameters saved in the variable 'data' added as data to the POST call that I make. 

	getToken(URI):
		...
		data = 'grant_type=authorization_code&code={}&client_id={}&redirect_uri={}/redirect&hash={}'.format(
			auth_code,client_id,baseURL, hash_)

		req = urllib2.Request('https://api.smartsheet.com/1.1/token')
		req.add_data(data)
		resp = urllib2.urlopen(req).read()
		return json.loads(resp)

####Saving the Access Token in the Session####

If the call is successful, I'll receive the access_token and refresh_token in the response. I save these two tokens in the session. Keep in mind that if the user ends the session, the values will be lost. You may consider storing a cookie on the user's browser that has a pointer to their access token held in your database. You would never want to store the access token itself in a cookie however. 


	parsed = getToken(code)
	self.session['Utoken'] = parsed['access_token'] 
	self.session['RefreshToken'] = parsed['refresh_token']


####Refreshing the Access Token####

Every access token that's issued has an expiration period of 7 days. After this point, you will need to use the refresh token to obtain a new access token for the user. This call is very similar to the one needed to obatin the access token in the first place. The difference is that the grant_type is 'refresh_token' and you pass in the refresh token instead of the authorization code. 

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

Once you have a user's access token, you will be able to fully access and change their Smartsheets on their behalf depending on the Scope that you request.


Congratulations! You just completed your fifth Smartsheet API Python walkthrough. We encourage you to play with the app, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
