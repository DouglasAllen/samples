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

### HELPERS ###
baseURL = "https://api.smartsheet.com/1.1"
token = "Please Enter Your Access Token Here"

###HTTP Request Abstraction###

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
            req.add_header(extra_header[0], extra_header[1])
        if data:
            req.add_data(data)
        if method:
            if method == 'PUT':
                req.get_method = lambda: 'PUT'

        self.resp = urllib2.urlopen(req).read()
        return self.resp

Call = SmartsheetAPI(baseURL,token)


#Initial sheet creation

columns = [{"title":"Baked Good","primary":True, "type":"TEXT_NUMBER"},
           {"title":"Baker", "type":"CONTACT_LIST"},
           {"title":"Price Per Item", "type":"TEXT_NUMBER"},
           {"title":"Gluten Free?", "type":"CHECKBOX", "symbol":"FLAG"},
           {"title":"Status", "type":"PICKLIST", "options":["Started","Finished","Delivered"]}]

sheet = json.dumps({"name":"Betty's Bake Sale","columns":columns})

header = "Content-Type", " application/json"

createSheet = Call._raw_request('/sheets', header, sheet) # Create sheet
createSheetResp = json.loads(Call.resp)
sheet_id = createSheetResp['result']['id']

#Add a column

column = json.dumps({"title":"Delivered", "type":"DATE", "index": 5})
insert_Column = Call._raw_request('/sheet/{}/columns'.format(sheet_id), header, column) # Add a column


#Add some rows

column_info = json.loads(Call._raw_request('/sheet/{}/columns'.format(sheet_id), header))
row_Insert1 =  json.dumps({"toTop":True, "rows":[ {"cells": [ {"columnId":column_info[0]['id'], "value":"Brownies"},
                                                    {"columnId":column_info[1]['id'], "value":"julieanne@smartsheet.com","strict": False},
                                                    {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                    {"columnId":column_info[3]['id'], "value":True},
                                                    {"columnId":column_info[4]['id'], "value":"Finished"},
                                                    {"columnId":column_info[5]['id'], "value": "None", "strict":False}]
                                                   },
                                                  {"cells":[ {"columnId":column_info[0]['id'], "value":"Snickerdoodles"},
                                                             {"columnId":column_info[1]['id'], "value":"stevenelson@smartsheet.com","strict":False},
                                                             {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                             {"columnId":column_info[3]['id'], "value":False},
                                                             {"columnId":column_info[4]['id'], "value":"Delivered"},
                                                             {"columnId":column_info[5]['id'], "value":"2013-09-04"}
                                                    ]},
                                                   {"cells":[ {"columnId":column_info[0]['id'], "value":"Rice Krispy Treats"},
                                                             {"columnId":column_info[1]['id'], "value":"rickthames@smartsheet.com","strict":False},
                                                             {"columnId":column_info[2]['id'], "value":"$.50", "strict":False},
                                                             {"columnId":column_info[3]['id'], "value":False},
                                                             {"columnId":column_info[4]['id'], "value":"Started"},
                                                             {"columnId":column_info[5]['id'], "value":"None", "strict":False},
                                                    ]},
                                                    {"cells":[ {"columnId":column_info[0]['id'], "value":"Muffins"},
                                                             {"columnId":column_info[1]['id'], "value":"sandrassmart@smartsheet.com","strict":False},
                                                             {"columnId":column_info[2]['id'], "value":"$1.50", "strict":False},
                                                             {"columnId":column_info[3]['id'], "value":False},
                                                             {"columnId":column_info[4]['id'], "value":"Finished"},
                                                             {"columnId":column_info[5]['id'], "value":"None", "strict":False},
                                                    ]},
                                                     {"cells":[ {"columnId":column_info[0]['id'], "value":"Chocolate Chip Cookies"},
                                                             {"columnId":column_info[1]['id'], "value":"janedaniels@smartsheet.com","strict":False},
                                                             {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                             {"columnId":column_info[3]['id'], "value":False},
                                                             {"columnId":column_info[4]['id'], "value":"Delivered"},
                                                             {"columnId":column_info[5]['id'], "value":"2013-09-05"},
                                                    ]},
                                                      {"cells":[ {"columnId":column_info[0]['id'], "value":"Ginger Snaps"},
                                                             {"columnId":column_info[1]['id'], "value":"nedbarnes@smartsheet.com","strict":False},
                                                             {"columnId":column_info[2]['id'], "value":"$.50", "strict":False},
                                                             {"columnId":column_info[3]['id'], "value":True},
                                                             {"columnId":column_info[4]['id'], "value":"Unknown", "strict":False},
                                                             {"columnId":column_info[5]['id'], "value":"None", "strict":False}

                                                             ]}
                                                             
                                                              
                                                    ]
                           })

insert_Rows = Call._raw_request('/sheet/{}/rows'.format(sheet_id), header, row_Insert1)
insertRowResp = json.loads(Call.resp)
rows = insertRowResp['result']




#Move rows
toTop = json.dumps({"toTop":True})
move_row_up = Call._raw_request('/row/{}'.format(rows[5]['id']), header, toTop, 'PUT')

Delivered_row_info = json.dumps({"toBottom":True, "rows":[
                                                    {"cells": [{"columnId":column_info[0]['id'], "value":"", "strict":False}] }, #Blank rows to create space to help break up the sheet
                                                    {"cells": [{"columnId":column_info[0]['id'], "value":"", "strict":False}] }, 
                                                    {"cells": [{"columnId":column_info[0]['id'], "value":"Delivered:", "strict":False},] } ]
                                 })


rowDelivered = json.loads(Call._raw_request('/sheet/{}/rows'.format(sheet_id), header, Delivered_row_info))

asChild = json.dumps({"parentId":rowDelivered['result'][2]['id']})



SnickerDoodles = rows[1]['id']
ChocolateChip = rows[4]['id']

moveSnickerdoodles = Call._raw_request('/row/{}'.format(SnickerDoodles), header, asChild, 'PUT') #column ID's instead of values
Snickerdoodles_Id = json.loads(moveSnickerdoodles)

asSibling = json.dumps({"siblingId":Snickerdoodles_Id['result'][0]['id']})
moveCCCookies = Call._raw_request('/row/{}'.format(ChocolateChip), header, asSibling, 'PUT') #column ID's instead of values                    

#Add some more rows
muffins = rows[3]['id']
row_Insert2 = json.dumps({"siblingId":muffins, "rows":[
                                                    {"cells": [{"columnId":column_info[0]['id'], "value":"Scones", "strict":False},
                                                    {"columnId":column_info[1]['id'], "value":"tomlively@smartsheet.com","strict": False},
                                                    {"columnId":column_info[2]['id'], "value":"$1.50", "strict":False},
                                                    {"columnId":column_info[3]['id'], "value":True},
                                                    {"columnId":column_info[4]['id'], "value":"Finished"},
                                                    {"columnId":column_info[5]['id'], "value": "None", "strict":False}
                                                               ] },
                                                    {"cells":[ {"columnId":column_info[0]['id'], "value":"Lemon Bars"},
                                                    {"columnId":column_info[1]['id'], "value":"rickthames@smartsheet.com","strict":False},
                                                    {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                    {"columnId":column_info[3]['id'], "value":False},
                                                    {"columnId":column_info[4]['id'], "value":"Started"},
                                                    {"columnId":column_info[5]['id'], "value":"None", "strict":False}
                                                               ] }
                                                    ]
                          })

insert_Rows2 = Call._raw_request('/sheet/{}/rows'.format(sheet_id), header, row_Insert2)


#Move column
get_columns = json.loads(Call._raw_request('/sheet/{}/columns'.format(sheet_id)))

for columns in get_columns:
    if columns['title'] == 'Status': #Move 'Status' to column 2
        columnID = columns['id']
        location = json.dumps({"title":'Status', "index":1, "sheetId":sheet_id})
        move_column = Call._raw_request('/column/{}'.format(columnID), header, location, 'PUT')

        
    elif columns['title'] == 'Delivered':#Move 'Delivered' to column 3
        columnID = columns['id']
        location1 = json.dumps({"title":'Delivered', "index":2, "sheetId":sheet_id})
        move_column = Call._raw_request('/column/{}'.format(columnID), header, location1, 'PUT')
