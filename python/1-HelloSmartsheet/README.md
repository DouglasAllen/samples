###'Hello Smartsheet' documentation: Python###
========
####This is a step by step walkthrough of how I built this simple Python script. This script will grab a list of the sheets accessible to you and allow you to share those with a specified user.####

-------
First things first:

Import  built in libraries:

	import urllib2

This is the default HTTP client for Python. It was chosen for its ubiquity across platforms. When building a larger app in Python that makes HTTP calls, I recommend using a library that will do caching and compression. <a href = "https://code.google.com/p/httplib2/"> Httplib2 </a> is a great option for this.

	import json

All responses from the Smartsheet API are in JSON. You'll also need this library to serialize and deserialize the data.

	API_URL = "https://api.smartsheet.com/1.1"
The base URL is hardcoded into the script in this example

	token = " Bearer " + str(input("Enter Smartsheet API access token:")) #Get token from user, we're not validating any user input in this example
	
To authenticate any calls made the Smartsheet API, you must include the Access Token. Note that we are not validating any user input in this example.

	sheetURL = API_URL + "/sheets" 
	r = urllib2.Request(sheetURL) #Create request object
	r.add_header("Authorization", token) #Add request properties
	i = urllib2.urlopen(r) 
	all_sheets = json.loads(i.read()) #Read and parse response from server
	
Concatenate the base URL with '/sheets' when making a call to Smartsheet's API to get a list of all the sheets accesible to the user. Next, create the request object and add the header to it. After opening the URL, parse the response object (JSON) into something useable by Python. This walk through doesn't include error handling for unexpected responses. In your own implementation, you'll want to handle errors or unsuccessful responses. 

	print "Fetching list of your sheets..."
	if all_sheets:
    	print "Total sheets:" + str(len(all_sheets))
    	print "Showing the first five sheets..."
    	x = 0
    	while x < 5:
        	print str(x+1) + ": " + str(all_sheets[x]['name'])
        	x += 1
	else:
    	print "You don't have any sheets. Goodbye!"
    	quit()

Iterate through the response and print out a list of the first 5 sheet names. If the user doesn't have any sheets, then the program quits.

	sheet_number = input("Enter the number of the sheet you want to share:") #Get email address from user
	sheet_name = all_sheets[sheet_number-1]['name']
	sheet_id = all_sheets[sheet_number-1]['id'] #Gets sheet id from response object

Get the sheet number from the user and, then grab the sheet name and id from the response object. 

	shareurl = API_URL +'/sheet/' + str(sheet_id) + '/shares?sendEmail=true' #URL used to share a sheet

When creating the URL for the share sheet call, make sure to use '/sheet' (instead of sheets) followed by the sheet id and '/shares?sendEmail=true' 

	shareto = str(input("Enter an email address to share {} to:".format(sheet_name))) 
	print "Choose an access level (VIEWER, EDITOR, EDITOR_SHARE, ADMIN) for {}:".format(shareto)
	
	print "Sharing {} to {} as {}.".format(sheet_name, shareto, accessLevel)
	
	accessLevel = str(input())
	
Get the access level from the user

	request = urllib2.Request(shareurl)
	request.add_header("Authorization", token)
	request.add_header("Content-Type", " application/json") #Make sure to specify the data type
	data = json.dumps({"email":shareto, "accessLevel":accessLevel}) #Add more request properties
	request.add_data(data)
	result = urllib2.urlopen(request)

Create another request object. You'll need to add another request property specifying the content type. Searliaze the data and add it to the request. urllib2 will automatically use the 'POST' method when data is attached to the request object, so you wont need to specify the method.

	result_parsed = json.loads(result.read()) 
	print "Sheet shared successfully, share ID {}".format(resp_parsed["result"]["id"])
	print "ENDING HelloSmartsheet..."
	print "Press any key to quit"
	input()
	quit()

    	
Lastly, parse the response and print a statement saying that the sheet was shared successfully. Then press any key to quit. 

Congratulations, you've completed the Python walkthrough. Please feel free to contact us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
