##Sheet Structure: Documentation##
=======
### This walk through will demonstrate how to create a new sheet, add columns and rows, and then move those columns and rows within the sheet. In this example Betty is having a bake sale. We'll use the data from her bakesale to help illustrate how data is created in Smartsheet's API. ###
--------------------

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

####Libraries####
First import the standard libraries needed for HTTP requests and JSON parsing. When building a larger app in Python that makes frequent HTTP calls, I recommend using a library that will do caching and compression. <a href = "https://code.google.com/p/httplib2/"> Httplib2 </a> is a great option for this.

	import urllib2
	import json
	
#### Helpers ####
Store the base URL for all API calls to Smartsheet and ask the user for their access token.

	baseURL = "https://api.smartsheet.com/1.1"
	token = "Please Put Access Token Here"
	
####HTTP Request Abstraction####
Create a class in Python that allows you to abstract the HTTP calls made throughout the script. Notice that you'll only need to specify the call method when using 'PUT.' urllib2 will automatically use 'GET' if no data is specified and a 'POST' if data is included. When using the 'PUT' method, pass it in as an argument when using the <code> _raw_request </code> method.

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
------------
####Create a Sheet  
Betty is running a fundraising bakesale and needs to track her project status and inventory.  First, she needs to set up her project dashboard by creating a sheet with all the columns required to track the key attributes:  
	
	columns = [{"title":"Baked Good","primary":True, "type":"TEXT_NUMBER"},
    	       {"title":"Baker", "type":"CONTACT_LIST"},
        	   {"title":"Price Per Item", "type":"TEXT_NUMBER"},
           	   {"title":"Gluten Free?", "type":"CHECKBOX", "symbol":"FLAG"},
           	   {"title":"Status", "type":"PICKLIST", "options":["Started","Finished","Delivered"]}]

	sheet = json.dumps({"name":"Betty's Bake Sale","columns":columns})

	header = "Content-Type", " application/json"

	Call = SmartsheetAPI(baseURL, token)
	createSheet = Call._raw_request('/sheets', header, sheet) # Create sheet
	createSheetResp = json.loads(Call.resp)
	sheet_id = createSheetResp['result']['id']
	
####Add a column
Turns out, she missed an important column to track "Delivery Date" - so she adds it to the end of the sheet. 

	column = json.dumps({"title":"Delivered", "type":"DATE", "index": 5})
	insert_Column = Call._raw_request('/sheet/{}/columns'.format(sheet_id), header, column)
	
####Add some rows
Betty needs to figure out who is preparing what for the sale, and when the items are going to be ready.  She inserts several rows to track that information:


	column_info = json.loads(Call._raw_request('/sheet/{}/columns'.format(sheet_id), header))
	row_Insert1 =  json.dumps({"toTop":True, "rows":[ {"cells": [ 
												    {"columnId":column_info[0]['id'], "value":"Brownies"},
                                                    {"columnId":column_info[1]['id'], "value":"julieanne@smartsheet.com","strict": False},
                                                    {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                    {"columnId":column_info[3]['id'], "value":True},
                                                    {"columnId":column_info[4]['id'], "value":"Finished"},
                                                    {"columnId":column_info[5]['id'], "value": "None", "strict":False}]
                                                   },{"cells":[ {"columnId":column_info[0]['id'], "value":"Snickerdoodles"},
                                                   ]}, 
                                                   {"columnId":column_info[1]['id'], "value":"stevenelson@smartsheet.com","strict":False},
                                                    {"columnId":column_info[2]['id'], "value":"$1", "strict":False},
                                                    {"columnId":column_info[3]['id'], "value":False},
                                                    {"columnId":column_info[4]['id'], "value":"Delivered"},
                                                    {"columnId":column_info[5]['id'], "value":"2013-09-04"}
                                                   ]}, …remaing rows here…          
                                                    ] 
                             })
	insert_Rows = Call._raw_request('/sheet/{}/rows'.format(sheet_id), header, row_Insert1)

####Move rows
Ned Barnes, who is making Ginger Snaps, is often late.  Betty moves his cookies to the top of the list so that she can keep an eye on them:

	toTop = json.dumps({"toTop":True})
	move_row_up = Call._raw_request('/row/{}'.format(rows[5]['id']), header, toTop, 'PUT')
                                  
                         
Betty realizes that a few of the items have already been delivered.  It would be handy to see them all in one place, so Betty takes advantage of Smartsheet's row hierarchy feature. She creates a "Delivered" section and moves all the delivered items there, making them children of "Delivered" (using the <code>parentId</code> attribute) so that they appear indented.

As more people volunteer, Betty keeps adding new baked goods to the list as siblings of existing items (using the <code>siblingId</code>).

	asChild = json.dumps({"parentId":rowDelivered['result'][2]['id']})

	SnickerDoodles = rows[1]['id']
	ChocolateChip = rows[4]['id']

	Snickerdoodles_Id = json.loads(moveSnickerdoodles)

	asSibling = json.dumps({"siblingId":Snickerdoodles_Id['result'][0]['id']})
	moveCCCookies = Call._raw_request('/row/{}'.format(ChocolateChip), header, asSibling, 'PUT') #column ID's instead of values                    


	
####Move column
The bake sale list is coming together.  Looking at the sheet, Betty decides that the "Status" column ought to be moved up (to index 1)to make it easier to identify delinquent items:

	get_columns = json.loads(Call._raw_request('/sheet/{}/columns'.format(sheet_id)))

	for columns in get_columns:
    	if columns['title'] == 'Status': #Move 'Status' to column 2
        	columnID = columns['id']
        	location = json.dumps({"title":'Status', "index":1, "sheetId":sheet_id})
        	move_column = Call._raw_request('/column/{}'.format(columnID), header, location, 'PUT')
        	
Congratulations! Betty's Bake Sale was well planned and went off without a hitch. You've now completed the walkthrough for creating and manipulating data in a sheet. Please feel free to contact us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
