HelloSmartsheet2: Betty's Bake Sale (C#)
===
See our [first HelloSmartsheet interactive script](https://github.com/smartsheet-platform/samples/blob/master/c%23/Hello%20Smartsheet/README.md) for a basic introduction to the Smartsheet API. The second in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by creating a sheet from scratch, populating it with data, and finally changing the sheet structure by moving around rows and columns.
Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).
.NET Framework
---
This script was built on .NET Framework 4.5. To set the target framework of the project in VS Express 2012, click Project → Console Application Properties → Application, and make a selection from the Target Framework drop-down list. Make sure it is not set to use the Client Profile.
References and Using Directives
---
In the Smartsheet API, request body data is expected to be in JSON, and the response body data is returned as JSON. This program uses the built-in JavaScriptSerializer class to serialize and deserialize JSON. Third-party libraries are available for this, but the built-in option is rather straight forward and easy to work with.

In order to use the JavaScriptSerializer class, add a reference to the program. In VS Express 2012, click Project → Add Reference → Framework. Select System.Web.Extensions to use JavaScriptSerializer in the application. 

The following namespaces are used in the program to save time when typing out methods:

System.IO - for the [StreamReader](http://msdn.microsoft.com/en-us/library/system.io.streamreader.aspx) and [StreamWriter](http://msdn.microsoft.com/en-us/library/system.io.streamwriter.aspx) classes

System.Net - for the [WebRequest](http://msdn.microsoft.com/en-us/library/system.net.webrequest.aspx) class

System.Web.Script.Serialization - for the [JavaScriptSerializer](http://msdn.microsoft.com/en-us/library/system.web.script.serialization.javascriptserializer.aspx) class
Usage
---
Generate a Smartsheet API access token and insert it into the Smartsheet Request class at the bottom of the script:
            
	string token = "insert your token here";

Code
---
This walk through highlights only some parts of the code.  For the full code, please see the complete hellosmartsheet2.cs script.  The script follows a simple story line to make it more relevant and easier to understand.

Betty is running a fund raising bake sale and needs to track her project status and inventory.  First, she needs to set up her project dashboard by creating a sheet with all the columns required to track the key attributes:

            string newSheetName = "Betty's Bake Sale";
            string newSheetData = "{\"name\":\"" + newSheetName + "\",\"columns\":[{\"title\":\"Baked Good\",
			\"primary\":true, \"type\":\"TEXT_NUMBER\"},{\"title\":\"Baker\",\"type\":\"CONTACT_LIST\"},{\"title\":
			\"Price Per Item\", \"type\":\"TEXT_NUMBER\"}, {\"title\":\"Gluten Free?\", \"type\":\"CHECKBOX\", 
			\"symbol\":\"FLAG\"}, {\"title\":\"Status\", \"type\":\"PICKLIST\", \"options\":[\"Started\",\"Finished
			\",\"Delivered\"]}]}";

Note that back-slashes are used to escape the quotation marks required in the JSON. To build a request that creates a sheet, pass the URL and HTTP method to the Smartsheet Request Class. Pass the string of column names in JSON format as an argument to the Make Request method:

            SmartsheetRequest createNewSheet = new SmartsheetRequest { callURL = "/sheets", method = "POST" };
            var createSheetResponse = createNewSheet.MakeRequest(newSheetData);

The createNewSheet object can be used in the future to create additional sheets by changing the argument that is passed to the MakeRequest method.

After creating the sheet, Betty realizes she missed an important column to track the "Delivery Date" - so she adds it to the end of the sheet:

            string newColumn = "{\"title\":\"Delivery Date\", \"type\":\"DATE\", \"index\":5}";
            SmartsheetRequest addNewColumn = new SmartsheetRequest { callURL = "/sheet/" + sheetId + "/columns", method = "POST" };
            addNewColumn.MakeRequest(newColumn);

Betty needs to figure out who is preparing what for the sale, and when the items are going to be ready. She inserts several rows to track that information. Below is example JSON code for 1 row: 

            string addRow = 
			"{\"toTop\":true, 
			\"rows\": [
			{ \"cells\": [
				{\"columnId\":" + jsonColumnIds[0]["id"] + ", \"value\":\"Brownies\"}, 
				{\"columnId\":" + jsonColumnIds[1]["id"] + ",\"value\":\"julieanne@smartsheet.com\", \"strict\":false}, 
				{\"columnId\":" + jsonColumnIds[2]["id"] + ", \"value\": \"$1\", \"strict\":false}, 
				{\"columnId\":" + jsonColumnIds[3]["id"] + ",\"value\":true}, 
				{\"columnId\":" + jsonColumnIds[4]["id"] + ",\"value\":\"Finished\"}, 
				{\"columnId\":" + jsonColumnIds[5]["id"] + ",\"value\":\"None\", \"strict\":false}]}]}";
            
Ned Barnes, who is making Ginger Snaps, is often late. Betty moves his cookies to the top of the list so that she can keep an eye on them:

            SmartsheetRequest moveRowSix = new SmartsheetRequest { callURL = "/row/" + jsonRowIds["result"][5]["id"], method = "PUT" };
            moveRowSix.MakeRequest("{\"toTop\":true}");

Betty realizes that a few of the items have already been delivered. It would be handy to see them all in one place, so Betty takes advantage of Smartsheet's [row hierarchy](http://help.smartsheet.com/customer/portal/articles/504734-hierarchy-indenting-outdenting-rows) feature. She creates a "Delivered" section and moves all the delivered items underneath, indenting them as children using the <code>parentId</code> attribute.

The bake sale list is coming together. Looking at the sheet, Betty decides that the "Status" column ought to be moved up (to index 1) to make it easier to identify delinquent items:

            string statusColumn = "{\"title\":\"Status\",\"index\":1, \"sheetId\":" + sheetId + "}";

            for (int i = 0; i < jsonColumnIds.Length; i++)
            {
                SmartsheetRequest moveStatusColumn = new SmartsheetRequest { callURL = "/column/" + jsonColumnIds[i]["id"], method = "PUT" };
                if (jsonColumnIds[i]["title"] == "Status")
                {
                    var moveResponse = moveStatusColumn.MakeRequest(statusColumn);
                }
			}

Betty is pleased with the outcome - not only is she able to track the project items, but she now has an impromptu "dashboard" that is conveniently laid out and easy to understand.
	
Congratulations! You just completed your second Smartsheet API C# walk through. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
