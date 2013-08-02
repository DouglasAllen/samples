File Operations (Ruby)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b> and <b>Attachments</b> scripts for a hands-on introduction to the Smartsheet API.  The fourth in the series, this semi-interactive script walks you through the Smartsheet API administrative calls and capabilities by managing team members (creating, updating and deleting), executing operations on behalf of other team members, and listing all sheets in a team account.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet APi, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Dependencies
---
This script has been tested with Ruby 1.9.3 only.
The following gems are required:

1. HTTParty
2. Active Support
3. JSON  

To install:

	gem install httparty
	gem install activesupport
	gem install json

In addition to the Net:HTTP library that comes with Ruby, there are several other HTTP client libraries.  HTTParty is one of them, and it was chosen for this walkthrough because of its relatively painless syntax.

You don't need to load the entire Active Support gem.  If you want to keep your script lightweight, require the <code>deep_merge</code> extension only.

Usage
---
Smartsheet has several subscription options, or plans, ranging from Basic to Enterprise - see the [Smartsheet pricing page](http://smartsheet.com/pricing) for more information.  Administrative features are only available in multi-user plans - Team and above.

To execute any admin operations, you must be (1) a member of a multi-user plan, and (2) must have administrative privileges. See this help article [http://help.smartsheet.com/customer/portal/articles/520100-user-types](http://help.smartsheet.com/customer/portal/articles/520100-user-types) to learn about Smartsheet user types.

	ss_token = 'INSERT_YOUR_TOKEN_HERE'


Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>admin.rb</b> script.

The goal of this walkthrough to help you understand how to take advantage of the Smartsheet API administrative features.

Let's add a coulpe of users to get started.  Both Gmail and Hotmail offer a useful "plus addressing" feature making it possible to create multiple email addresses without registering for multiple email accounts.  Let's use that:

	[…]
	options = {
	  headers: { 'Content-Type' => 'application/json' },
	  body: {
	      firstName: 'John101',
	      lastName: 'Smith101',
	      email: 'YOUR_GMAIL_ADDRESS+101@gmail.com',
	      admin: false,
	      licensedSheetCreator: true
	  }.to_json
	}
	ss_connection.request('post', '/users', options)
	[…]

You should receive an email at YOUR_GMAIL_ADDRESS+101@gmail.com inviting John101 Smith101 to joint your team account.  Accept the invitation - this is a required step, without it the invited user will not become a member of your team account.  This is because of Smartsheet's opt-in model; there are some exceptions but those are out of scope of this walkthrough.

Fetch the list of all your team account members - including the ones you just added:

	ss_connection.request('get', '/users')

Next, let's create sheets on behalf of our newly added users.  This operation takes advantage of the "Assume-User" feature which allows team account administrators to execute commands on behalf of other users.  There are numerous business scenarios (create, update, etc.) when this is extremely useful, but it should be used with care.  Make sure that the email address of the user whose identity we are assuming is URL-encoded (in this case, using CGI.escape):

	[…]
	options = {
	  headers: {
	    'Content-Type' => 'application/json',
	    'Assume-User' => CGI.escape(user1['email'])
	  },
	  body: {
	    name: sheet_name,
	    columns: [ { title: "Column1", type: "TEXT_NUMBER", primary: true } ]
	  }.to_json
	}
	ss_connection.request('post', '/sheets', options)
	[…]
	
Now fetch the list of all sheets in your team account, regardless of whether you have been shared to them.  This operation is only available to account administrators:

	ss_connection.request('post', '/sheets', options)
	
If you render the result, you will see that although you executed the commands, the sheets are owned by the users on whose behalf the sheets were created.

Finally, remove the user from your team account.  You can optionally choose to (1) have the user's own sheets be transferred to someone else in your team account, and (2) remove the user from any previously shared documents:

	[…]
	options = {
	  query: {
	    transferTo: user2['id'],
	    removeFromSharing: true
	  }
	}
	ss_connection.request('delete', "/user/#{user1['id']}", options)
	[…]
	
If you refresh the list of users, you should see that the one you just removed is gone.  If you refresh the list of sheets, you should now see that the sheet(s) previously owned by the removed user are now owned by the one to whom you transferred the sheet(s).
	
Congratulations!  You just completed your fourth Smartsheet API Ruby walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 
