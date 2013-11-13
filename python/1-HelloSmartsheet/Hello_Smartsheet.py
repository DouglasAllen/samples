#!/usr/bin/python
#
#   Copyright 2013 Smartsheet, Inc.
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
#   Written in Python 2.7.5

import urllib2
import json

API_URL = "https://api.smartsheet.com/1.1"

token = " Bearer " + str(raw_input("Enter Smartsheet API access token:")) #Get token from user, we're not validating any user input in this example

sheetURL = API_URL + "/sheets" 
r = urllib2.Request(sheetURL) #Create request object
r.add_header("Authorization", token) #Add request properties
i = urllib2.urlopen(r) 
all_sheets = json.loads(i.read()) #Read and parse response from server

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
sheet_number = input("Enter the number of the sheet you want to share:") #Get email address from user
sheet_name = all_sheets[sheet_number-1]['name']
sheet_id = all_sheets[sheet_number-1]['id'] #Gets sheet id from response object

shareURL = API_URL +'/sheet/' + str(sheet_id) + '/shares?sendEmail=true' #URL used to share a sheet

shareTo = str(raw_input("Enter an email address to share {} to:".format(sheet_name))) 
accessLevel = str(raw_input("Choose an access level (VIEWER, EDITOR, EDITOR_SHARE, ADMIN) for {}:".format(shareTo)))

print "Sharing {} to {} as {}.".format(sheet_name, shareTo, accessLevel)

request = urllib2.Request(shareURL)
request.add_header("Authorization", token)
request.add_header("Content-Type", " application/json") #Make sure to specify the data type
data = json.dumps({"email":shareTo, "accessLevel":accessLevel}) #Add more request properties
request.add_data(data)
resp = urllib2.urlopen(request)

resp_parsed = json.loads(resp.read())

print "Sheet shared successfully, share ID {}".format(resp_parsed["result"]["id"])
print "ENDING HelloSmartsheet..."
print "Press any key to quit"
raw_input()
quit()
