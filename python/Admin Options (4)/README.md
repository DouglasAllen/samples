##Admin Controls: Documentation (Python)##
==========
####See our `Hellosmartsheet`, `SheetStructure` and `Attachments` scripts for a hands-on introduction to the Smartsheet API.  The fourth in the series, this semi-interactive script walks you through the Smartsheet API administrative calls and capabilities by managing team members (creating, updating and deleting), executing operations on behalf of other team members, and listing all sheets in a team account.####

-------
####Libraries####
First import the standard libraries needed for HTTP requests and JSON parsing. As always, when building a larger app in Python that makes frequent HTTP calls, I recommend using a library that supports caching and compression. <a href = "https://code.google.com/p/httplib2/"> Httplib2 </a> is a great option for this. 

	import urllib2
	import json
####Setup####
Smartsheet has several subscription options, or plans, ranging from Basic to Enterprise - see the [Smartsheet pricing page](http://smartsheet.com/pricing) for more information.  Administrative features are only available in multi-user plans - Team and above.

To execute any admin operations, you must be (1) a member of a multi-user plan, and (2) must have administrative privileges. See this help article [http://help.smartsheet.com/customer/portal/articles/520100-user-types](http://help.smartsheet.com/customer/portal/articles/520100-user-types) to learn about Smartsheet user types.
	
	token = 'INSERT_YOUR_TOKEN_HERE'
	
####Code
	
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>admin.py</b> script.

The goal of this walkthrough is to help you understand how to take advantage of the Smartsheet API administrative features.

Let's add a coulpe of users to get started.  Both Gmail and Hotmail offer a useful "plus addressing" feature making it possible to create multiple email addresses without registering for multiple email accounts.  Let's use that:

	user1 = json.dumps({'firstName': 'Sal', 'lastName': 'Tilman', 'email': 'YOUR_GMAIL_HERE+sal@gmail.com',
					'admin': False, 'licensedSheetCreator': True})
					
	createUser1 = API._raw_request('/users?sendEmail=false', contHeader,user1) 
	resp1 = json.loads(createUser1)
	userIDSal = resp1['result']['id']
	
You should receive an email at YOUR_GMAIL_ADDRESS+101@gmail.com inviting John101 Smith101 to joint your team account.  Accept the invitation - this is a required step, without it the invited user will not become a member of your team account.  This is because of Smartsheet's opt-in model; there are some exceptions but those are out of scope of this walkthrough.

Fetch the list of all your team account members - including the ones you just added:

	listUsers = API._raw_request('/users')

Next, let's create sheets on behalf of our newly added users.  This operation takes advantage of the "Assume-User" feature which allows team account administrators to execute commands on behalf of other users.  There are numerous business scenarios (create, update, etc.) when this is extremely useful, but it should be used with care.  Make sure that the email address of the user whose identity we are assuming is URL-encoded (in this case, using urllib2.quote):


	UserSal = [("Assume-User", ' '+urllib2.quote('YOUR_GMAIL_HERE+sal@gmail.com'))]
	
	assumeUserSal = contHeader + UserSal
	sheetSal = json.dumps({"name": "Sal's Sheet", "columns": columns})
	createSheet = json.loads(API._raw_request('/sheets', assumeUserSal, sheetSal))
	
	
Now fetch the list of all sheets in your team account, regardless of whether you have been shared to them.  This operation is only available to account administrators:

	listSheets = json.loads(API._raw_request('/users/sheets'))
	
If you render the result, you will see that although you executed the commands, the sheets are owned by the users on whose behalf the sheets were created.

Finally, remove the user from your team account.  You can optionally choose to (1) have the user's own sheets be transferred to someone else in your team account, and (2) remove the user from any previously shared documents:

	deleteSal = API._raw_request('/user/{}?transferTo={}&removeFromSharing=true'.format(userIDSal, userIDCalvin), method = 'DELETE')

If you refresh the list of users, you should see that the one you just removed is gone.  If you refresh the list of sheets, you should now see that the sheet(s) previously owned by the removed user are now owned by the one to whom you transferred the sheet(s).
	
Congratulations!  You just completed your fourth Smartsheet API Ruby walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 