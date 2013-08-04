Hello Smartsheet (Ruby)
===
This is a simple introduction to the Smartsheet API for Ruby developers.  Hello Smartsheet is an interactive script that walks you through a basic Smartsheet API integration by establishing a connection, fetching a list of sheets, and sharing one of the sheets to a collaborator.

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

Data validation
---
Please note that no validation is performed on any data entered by the user.  We strongly encourage you to add data validation to any script you intend to use or distribute.

Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete hellosmartsheet.rb script.

Create an HTTParty class for Smartsheet:

	class Smartsheet
		include HTTParty
	[…] 
	
Specify the base Smartsheet API URI:
	
	[…] 
	base_uri 'https://api.smartsheet.com/1.1'
	[…] 

Abstract HTTP request handling to keep the code DRY:
	
	def request(method, uri, options={})
	  […] 
	end

Add some basic request error handling:

    if response.code.to_s !~ /^2/
      puts "* fatal error: #{json['errorCode']}: #{json['message']}"
      exit
    end

Initialize the HTTParty connection object, where <code>ss_token</code> is an API access token you must generate in the Smartsheet UI:

	ss_connection = Smartsheet.new(ss_token)
	
Fetch the list of your sheets:

	sheets = ss_connection.request('get', '/sheets')

To share a sheet to a collaborator, set the right headers and specify the recipient and her desired access level:

	options = {
	  headers: { 'Content-Type' => 'application/json' },
	  query: { sendEmail: true },
	  body: {
	    email: email_to_share,
	    accessLevel: level_to_share
	  }.to_json
	}

Finally, share the sheet:

	ss_connection.request('post', "/sheet/#{sheets[sheet_index_to_share]['id']}/shares", options)
	
Congratulations!  You just completed your first Smartsheet API Ruby walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples/ruby/1-hellosmartsheet)
