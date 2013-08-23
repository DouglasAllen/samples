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

#Smartsheet API Admin Access

import urllib2
import json

#create Developer Account
baseUrl = 'https://api.smartsheet.com/1.1'
token = 'INSERT_YOUR_TOKEN_HERE'

class SmartsheetAPI(object):
    """Template for making calls to the Smartsheet API"""
    def __init__(self,url,token):
        self.baseURL = url
        self.token = " Bearer " + str(token)


    def _raw_request(self, url, extra_header = None, data = None, method = None):
        request_url = self.baseURL + url
        req = urllib2.Request(request_url)
        req.add_header("Authorization", self.token)

        if extra_header:
            for i in extra_header:
                req.add_header(i[0], i[1])
        if data:
            req.add_data(data)
        if method:
        	if method == 'PUT':
        		req.get_method = lambda: 'PUT'
        	elif method == 'DELETE': 
        		req.get_method = lambda: 'DELETE'

		resp = urllib2.urlopen(req).read()
        return resp

API = SmartsheetAPI(baseUrl, token)
contHeader = [("Content-Type", " application/json")]

#create users
user1 = json.dumps({'firstName': 'Sal', 'lastName': 'Tilman', 'email': 'YOUR_GMAIL_HERE+sal@gmail.com',
					'admin': False, 'licensedSheetCreator': True})

createUser1 = API._raw_request('/users?sendEmail=false', contHeader,user1) 
resp1 = json.loads(createUser1)
userIDSal = resp1['result']['id']

user2 = json.dumps({'firstName': 'Calvin', 'lastName': 'Broadus', 'email': 'YOUR_GMAIL_HERE+sd@gmail.com',
					'admin': False, 'licensedSheetCreator': True})

createUser2 = API._raw_request('/users?sendEmail=false', contHeader, user2)
resp2 = json.loads(createUser2)
userIDCalvin = resp2['result']['id']

raw_input('Make sure to accept both invitations to join the team')

#list users
listUsers = API._raw_request('/users')

users = json.loads(listUsers)
print users

#create sheets
columns = [{"title":"This is Column 1","primary":True, "type":"TEXT_NUMBER"},
           {"title":"Good Column?", "type":"PICKLIST", "options":["Yes","No","Maybe"]}]

adminSheet = json.dumps({"name": "Admin Sheet", "columns": columns}) 
createSheet = json.loads(API._raw_request('/sheets', contHeader, adminSheet))
sheet_id = createSheet['result']['id']

column_info = json.loads(API._raw_request('/sheet/{}/columns'.format(sheet_id)))
row_Insert1 =  json.dumps({"toTop":True, "rows":[ {"cells": [ {"columnId":column_info[0]['id'], "value":"Perhaps"},
                                                    {"columnId":column_info[1]['id'], "value":"Maybe"}
                                                    		]
                                                   }
                                                 ]
                           })
insert_Rows = API._raw_request('/sheet/{}/rows'.format(sheet_id), contHeader, row_Insert1)
insertRowResp = json.loads(insert_Rows)

sheetSal = json.dumps({"name": "Sal's Sheet", "columns": columns})
sheetCalvin = json.dumps({"name": "Calvin's Sheet", "columns": columns})

#Assume user
UserSal = [("Assume-User", ' '+urllib2.quote('YOUR_GMAIL_HERE+sal@gmail.com'))]
assumeUserSal = contHeader + UserSal

createSheet = json.loads(API._raw_request('/sheets', assumeUserSal, sheetSal))
sheet_idSal = createSheet['result']['id']


UserCalvin = [("Assume-User", ' '+ urllib2.quote('YOUR_GMAIL_HERE+sd@gmail.com'))]
assumeUserCalvin = contHeader + UserCalvin

createSheet = json.loads(API._raw_request('/sheets', assumeUserCalvin, sheetCalvin))
sheet_idCalvin = createSheet['result']['id']

#Add Row via assume user
column_info = json.loads(API._raw_request('/sheet/{}'.format(sheet_idSal), UserSal))
row_Insert2 =  json.dumps({"toTop":True, "rows":[ {"cells": [ {"columnId":column_info['columns'][0]['id'], "value":"Inserting Data as Sal"},
                                                    {"columnId":column_info['columns'][1]['id'], "value":"Success", "strict": False}
                                                    		]
                                                   }
                                                 ]
                           })

insert_Rows = API._raw_request('/sheet/{}/rows'.format(sheet_idSal),assumeUserSal, row_Insert2)
insertRowResp = json.loads(insert_Rows)
rowIDSal = insertRowResp['result'][0]['id']

discussionText = json.dumps({"title": "For Sal", "comment": {"text":"Call me when you see this"}})
discussion = json.loads(API._raw_request("/sheet/{}/discussions".format(sheet_idSal), assumeUserSal, discussionText))

updatedRows = json.dumps([{"columnId": column_info['columns'][0]['id'], "value": "Updating the row as Sal"},
							])
update_Row = json.loads(API._raw_request('/row/{}/cells'.format(rowIDSal), assumeUserSal, updatedRows, 'PUT'))

column_info = json.loads(API._raw_request('/sheet/{}'.format(sheet_idCalvin), UserCalvin))
row_Insert3 =  json.dumps({"toTop":True, "rows":[ {"cells": [ {"columnId":column_info['columns'][0]['id'], "value":"Inserting Data as Calvin"},
                                                    {"columnId":column_info['columns'][1]['id'], "value":"Go it!","strict": False}
                                                    		]
                                                   }
                                                 ]
                           })

insert_Rows = API._raw_request('/sheet/{}/rows'.format(sheet_idCalvin), assumeUserCalvin, row_Insert3)
insertRowResp = json.loads(insert_Rows)
rowIDCalvin = insertRowResp['result'][0]['id']

discussionText = json.dumps({"title": "For Calvin", "comment": {"text":"Let's talk soon"}})
discussion = json.loads(API._raw_request("/sheet/{}/discussions".format(sheet_idCalvin), assumeUserCalvin, discussionText))

updatedRows = json.dumps([{"columnId": column_info['columns'][0]['id'], "value": "Updating the row as Calvin"},
							])
update_Row = json.loads(API._raw_request('/row/{}/cells'.format(rowIDCalvin), assumeUserCalvin, updatedRows, 'PUT'))


#list all sheets
listSheets = json.loads(API._raw_request('/users/sheets'))
print listSheets

#Delete user and specify transfer of sheets
deleteSal = API._raw_request('/user/{}?transferTo={}&removeFromSharing=true'.format(userIDSal, userIDCalvin), method = 'DELETE')

listSheets = json.loads(API._raw_request('/users/sheets'))
print listSheets


