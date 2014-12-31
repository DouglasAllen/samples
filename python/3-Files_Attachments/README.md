##File Upload and Attachment: Documentation (Python)##
==========
####This script will walk you through the more advanced Smartsheet API calls and capabilities by attaching a file and a URL to a row, downloading a file attachment, and finally deleting a file attachment. For a more in-depth introduction to Smartsheet's API, see our `Hellomartsheet` and `Sheet Structure` walkthroughs. ####
-------
####Libraries####
First import the standard libraries needed for HTTP requests and JSON parsing. As always, when building a larger app in Python that makes frequent HTTP calls, I recommend using a library that supports caching and compression. <a href = "https://code.google.com/p/httplib2/"> Httplib2 </a> is a great option for this. 

	import urllib2
	import json
	import os
	
####Setup####

Generate a Smartsheet API access token and insert it into the script:

	token = 'INSERT_YOUR_TOKEN_HERE'
	
Find a file on your local computer to upload. 

	filename = #Enter path to file attachment here: 'path/cat-06.jpg'
	
####HTTP Request Abstraction####
I won't go into to much detail about how this works (you can see a more detailed explanation in `Sheet Structure`), but I did want to call attention to two pieces of the code. First, when uploading an attachment to Smartsheet extra headers are needed that specify the 'Content Disposition', 'Content Type' and 'Content Length.' I implemented this by taking in the headers as an array that I would iterate through and then add to the request object. 

	def _raw_request(self, url, extra_header = None, data = None, method = None):
		request_url = self.baseURL + url
		req = urllib2.Request(request_url)
		req.add_header("Authorization", self.token)

	if extra_header:
		for i in extra_header:
		req.add_header(i[0], i[1])

Also, because urllib2 doesn't support the 'PUT' or 'DELETE' methods by default, I added those manually as well.

	if method:
            if method == 'PUT':
                req.get_method = lambda: 'PUT'
            elif method == 'DELETE':
                req.get_method = lambda: 'DELETE'
                
####Create a Sheet####

Smartsheet allows attaching files to  sheets as well as rows and comments within sheets. In this walkthrough we'll attach files to only a sheet and a row. I create a sheet and add a row, then grab the sheet ID and the row ID in order to POST attachments to them later.

	sheet_id = createSheetResp['result']['id']
	…
	rows = insertRowResp['result']
	rowID = rows[0]['id']
	

####Upload an Attachment####

To upload attachments, make a POST call to either the sheet or a row. Before doing this, first get the size of the file to attach. Fortunately in Python this is easy!

	fileSize = os.path.getsize(filename)
	
Next you'll need to specify the 'Content Disposition', 'Content Type' and 'Content Length.' I put them in an array that I'll pass into my HTTP abstraction

	disposition = 'Content-Disposition', ' attachment; filename="{}"'.format('Kitteh-Kitteh.jpg')
	conType = 'Content-Type', 'image/jpeg'
	length = 'Content-Length', ' '+ str(fileSize)
	headers = [disposition, conType, length]
	
Open the file and then make the call to the API including the extra headers and file handle. Notice that the call is made to `https://api.smartsheet.com/1.1/row/{rowID}/attachments`.

	file_ = open(filename)
	uploadFile = Call._raw_request('/row/{}/attachments'.format(rowID), headers, file_)
	file_.close()

Don't forget to close the file when done using it!

####Attach a File From a URL####

Attach a URL to a sheet or row by specifying some information about the attachment (Name, Description, Attachment Type, URL). And then make the call to the same URL. Currently the Smartsheet API doesn't support Multipart Upload, so you'll want to ensure that file is sent up as a single chunk of data. 

	link1 = json.dumps({"name":"This is a cat", "description": "What a feline!", "attachmentType":"LINK", "url":"http://imgur.com/jmbzioZ"})
	
	attachURL_1 = json.loads(Call._raw_request('/row/{}/attachments'.format(rowID), content, link1))


####Delete an Attachment From a Sheet####

Deleting an attachment is easy - just change the request method to DELETE (all caps), and pass in the AttachmentId that you want to delete.

	deleteAttachment = json.loads(Call._raw_request('/attachment/{}'.format(attachmentID_file), method = 'DELETE'))
	deleteURL = json.loads(Call._raw_request('/attachment/{}'.format(attachmentID_url), method = 'DELETE'))

Congratulations! You just completed your third Smartsheet API Python walkthrough. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. For more information, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers). Also, feel free to contact us at api@smartsheet.com with any questions or suggestions. 

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
