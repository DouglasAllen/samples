Sheet Structure: Betty's Bake Sale (Java)
===
See our Hellosmartsheet interactive script for a basic introduction to the Smartsheet API.  The second in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by creating a sheet from scratch, populating it with data, and finally changing the sheet structure by moving around rows and columns.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Dependencies
---
This script has been tested with Java 7.
You must also include the Jackson JSON parser jars, found here: http://wiki.fasterxml.com/JacksonHome. This has been tested with version 2.2.0.

To run, cd to the folder where HelloSmartsheet.java is:
	javac -classpath [folder with Jackson jars] HelloSmartsheet2.java
	java -classpath [folder with Jackson jars];. HelloSmartsheet2
	
Usage
---
Generate a Smartsheet API access token and inserted it into the script:

	String accessToken = "";//Insert your access token here.


Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete HelloSmartsheet2.java.  The script follows a simple storyline to make it more relevant and easier to understand.

Betty is running a fundraising bakesale and needs to track her project status and inventory.  First, she needs to set up her project dashboard by creating a sheet with all the columns required to track the key attributes:  

	String sheetName = "Betty's Bake Sale";
	[…]
	Sheet newSheet = new Sheet();
	newSheet.setName(sheetName);
	newSheet.setColumns(Arrays.asList(
			new Column("Baked Goods", 	"TEXT_NUMBER", 	null, 	true, 	null),
			new Column("Baker", 		"CONTACT_LIST", null, 	null, 	null),
			new Column("Price Per Item","TEXT_NUMBER", 	null, 	null, 	null),
			new Column("Gluten Free?", 	"CHECKBOX", 	"FLAG",	null, 	null),
			new Column("Status", 		"PICKLIST", 	null, 	null, 	Arrays.asList("Started", "Finished", "Delivered"))
		));

	
Turns out, she missed an important column to track "Delivery Date" - so she adds it to the end of the sheet:

	String columnName = "Delivery Date";
	[…]
	Column newColumn = new Column(columnName, "DATE", 5);
	[…]
	mapper.writeValue(connection.getOutputStream(), newColumn);
	
Betty needs to figure out who is preparing what for the sale, and when the items are going to be ready.  She inserts several rows to track that information: 
	[…]
    List<Row>rows = new ArrayList<Row>();
	rows.add(new Row(Arrays.asList(
		new Cell(allColumns.get(0).id, "Brownies"), 
		new Cell(allColumns.get(1).id, "julieann@example.com"), 
		new Cell(allColumns.get(2).id, "$1"), 
		new Cell(allColumns.get(3).id, Boolean.TRUE), 
		new Cell(allColumns.get(4).id, "Finished"))));
	[…]
	RowWrapper rowWrapper = new RowWrapper();
	rowWrapper.setToBottom(true);
	rowWrapper.setRows(rows);
	[…]
	
Ned Barnes, who is making Ginger Snaps, is often late.  Betty moves his cookies to the top of the list so that she can keep an eye on them:

	[…] 
	RowWrapper moveToTop = new RowWrapper();
	moveToTop.setToTop(true);
	connection = (HttpURLConnection) new URL(ROW_URL.replace(ID, ""+newRowsResult.getResult().get(5).getId())).openConnection();
	connection.setRequestMethod("PUT");
	[…] 
	mapper.writeValue(connection.getOutputStream(), moveToTop);
	[…] 
	
	
Betty realizes that a few of the items have already been delivered.  It would be handy to see them all in one place, so Betty takes advantage of Smartsheet's row hierarchy feature.  She creates a "Delivered" section and moves all the delivered items there, making them children of "Delivered" (using the <code>parentId</code> attribute)so that they appear indented.
	
As more people volunteer, Betty keeps adding new baked goods to the list as siblings of existing items (using the <code>siblingId</code>).

The bake sale list is coming together.  Looking at the sheet, Betty decides that the "Status" column ought to be moved up (to index 1)to make it easier to identify delinquent items:

	[…] 
	moveColumn.setIndex(1);
	moveColumn.setTitle(statusColumn.title);
	moveColumn.setSheetId(newSheet.getId());
	moveColumn.setType(statusColumn.getType());
	connection = (HttpURLConnection) new URL(COLUMN_URL.replace(ID, "" + statusColumn.getId())).openConnection();
	[…]
	connection.setRequestMethod("PUT");		
	mapper.writeValue(connection.getOutputStream(), moveColumn);
	[…] 

Betty is pleased with the outcome - not only she is able to track the project items, but also the impromptu "dashboard" is conveniently laid out and easy to understand.
	
Congratulations!  You just completed your second Smartsheet API Java walkthrough.  We encourage you to play with the app, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
