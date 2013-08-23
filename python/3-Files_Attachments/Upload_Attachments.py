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

import urllib2
import json
import os

### HELPERS ###
baseURL = "https://api.smartsheet.com/1.1"
token = 'INSERT_YOUR_TOKEN_HERE'
filename = #Enter path to file attachment here: 'path/cat-06.jpg'

###Smartsheet API###
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

        self.resp = urllib2.urlopen(req).read()
        return self.resp

Call = SmartsheetAPI(baseURL,token)

#create sheet

columns = [{"title":"Attachment Name","primary":True, "type":"TEXT_NUMBER"},
           {"title":"Status", "type":"PICKLIST", "options":["Pending","Uploaded","Deleted"]}]

sheet = json.dumps({"name":"Attachment Sheet","columns":columns})

content = [("Content-Type", " application/json")]

createSheet = Call._raw_request('/sheets', content, sheet)
createSheetResp = json.loads(Call.resp)
sheet_id = createSheetResp['result']['id']


#insert row

column_info = json.loads(Call._raw_request('/sheet/{}/columns'.format(sheet_id)))
row_Insert1 =  json.dumps({"toTop":True, "rows":[ {"cells": [ {"columnId":column_info[0]['id'], "value":"Kitteh"},
                                                    {"columnId":column_info[1]['id'], "value":"Uploaded"}
                                                    		]
                                                   }
                                                 ]
                           })


insert_Rows = Call._raw_request('/sheet/{}/rows'.format(sheet_id), content, row_Insert1)
insertRowResp = json.loads(Call.resp)
rows = insertRowResp['result']
rowID = rows[0]['id']


#determine file size for attachment

fileSize = os.path.getsize(filename)

#attach 1 or more files to row

disposition = 'Content-Disposition', ' attachment; filename="{}"'.format('Kitteh-Kitteh.jpg')
conType = 'Content-Type', 'image/jpeg'
length = 'Content-Length', ' '+ str(fileSize)
headers = [disposition, conType, length]
file_ = open(filename)
uploadFile = Call._raw_request('/row/{}/attachments'.format(rowID), headers, file_)
file_.close()
resp = json.loads(uploadFile)
attachmentID_file = resp['result']['id']


#Attach file from URL


link1 = json.dumps({"name":"This is a cat", "description": "What a feline!", "attachmentType":"LINK", "url":"http://imgur.com/jmbzioZ"})

attachURL_1 = json.loads(Call._raw_request('/row/{}/attachments'.format(rowID), content, link1))

attachmentID_url = attachURL_1['result']['id']

link2 = json.dumps({"name":"Smartsheet BBQ fun", "description": "The Smartsheet team having fun on lake Washington", "attachmentType":"LINK", "url":"http://www.smartsheet.com/files/haymaker/Smartsheet_Team_2011.jpg"})
attachURL_2 = json.loads(Call._raw_request('/row/{}/attachments'.format(rowID), content, link2 ))


#download at least one attachment

downloadAttachment = json.loads(Call._raw_request('/attachment/{}'.format(attachmentID_file)))
attachResp = downloadAttachment
doc = urllib2.urlopen(attachResp['url'])
attachFile = 'INSERT DOWNLOAD PATH HERE'.format(attachResp['name']) 
fileloc = open(attachFile, 'w')
fileloc.write(doc.read())
fileloc.close()

#delete one attachment and one URL
deleteAttachment = json.loads(Call._raw_request('/attachment/{}'.format(attachmentID_file), method = 'DELETE'))
deleteURL = json.loads(Call._raw_request('/attachment/{}'.format(attachmentID_url), method = 'DELETE'))

