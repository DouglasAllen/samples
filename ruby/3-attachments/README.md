File Operations (Ruby)
===
See our <b>Hellosmartsheet</b> and <b>SheetStructure</b> scripts for a hands-on introduction to the Smartsheet API.  The third in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by attaching a file and a URL to a row, downloading a file attachment, and finally deleting a file attachment.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

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
Generate a Smartsheet API access token and insert it into the script:

	ss_token = 'INSERT_YOUR_TOKEN_HERE'


Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>attachments.rb</b> script.

The goal of this walkthrough to help you understand how to attach files and URLs to data containers in Smartsheet, and then access these attachments.  In Smartsheet, users can attach files to workspaces (not supported via the API as of 2013 07 22), sheets, rows, and discussion comments. 

<b>IMPORTANT</b>: Please note that as of this writing the Smartsheet API does not support multipart or chunked file upload. 

If you recall, we selected the HTTParty HTTP client library for this and other code walkthroughs.  Turns out HTTParty does not support file uploads.  Luckily, there is a patch available [on Github](https://github.com/jnunemaker/httparty/issues/77) to enable file uploads.  We have updated the patch here to make it work with the most recent (0.11) version of HTTParty.  Let's include the patch in the code - no need to modify the HTTParty gem itself:

	[…]
  	module HTTParty
	    class Request
	      private
	
	      def setup_raw_request
	        @raw_request = http_method.new(request_uri(uri))
	        if body
	          if body.respond_to?(:read)
	            @raw_request.body_stream = body
	          else
	            @raw_request.body = body
	          end
	        end
	        @raw_request.initialize_http_header(options[:headers])
	        @raw_request.basic_auth(username, password) if options[:basic_auth]
	        setup_digest_auth if options[:digest_auth]
	      end
	
	    end
  	end
	[…]

If you find yourself needing to debug the HTTParty calls, you can use the handy <code>debug_output</code> statement:

	[…]
	class Smartsheet
  		include HTTParty
  		debug_output $stderr
  		base_uri 'https://api.smartsheet.com/1.1'
	[…]
	 
First, create a sheet with a couple of rows so that we have something to work with.  Now that the rows are in place, let's upload a file to the top row.  A number of headers, including <code>Content-Length</code>, are required, so let's figure out the size of the file, in Kb, we are about to upload:

	filename = "screenshot-of-smartsheet.png"
	filesize = File.size(filename)

Set the required headers and load the actual file:

	[…]
	options = {
	  headers: {
	    'Content-Disposition' => "attachment; filename=\"#{filename}\"",
	    'Content-Type' => 'image/png',
	    'Content-Length' => "#{filesize}"
	  },
	  body: File.open(filename, 'r')
	}
	[…]

Upload the file:

	ss_connection.request('post', "/row/REPLACE_WITH_ROW_ID/attachments", options)

Attaching to sheets or discussion comments works similarly - see the API docs for endpoints or more information.

To download the attached file, first fetch the attachment object which contains the URL to the downloadable file:

	attached_file_object = ss_connection.request('get', "/attachment/REPLACE_WITH_ATTACHMENT_ID")
	
Extract the URL to the downloadable file:

	url = attached_file_object['url']
	
Save the file to disk (or you can skip saving the file if you just want to work with it in memory):

	[…]
	File.open("attached_file_contents", "w") do |stream|
	  stream << HTTParty.get(attached_file_object['url'])
	end
	[…]


Using Net:HTTP instead of HTTParty
---

Alternatively, you can you use the ruby's stock Net::HTTP library instead of HTTParty.  See the included file <b>attach-file-nethttp.rb</b> for sample code.

All the Smartsheet API calls must go over HTTPS, so you must remember to include two things in your code when using Net:HTTP.  First, require the Net::HTTPS library in addition to Net::HTTP:

	require 'net/https'

And make sure to set <code>use_ssl</code> to <code>true</code>:

	req.use_ssl = true

	
Congratulations!  You just completed your third Smartsheet API Ruby walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
