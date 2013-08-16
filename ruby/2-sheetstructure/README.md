Sheet Structure: Betty's Bake Sale (Ruby)
===
See our <b>Hellosmartsheet</b> interactive script for a basic introduction to the Smartsheet API.  The second in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by creating a sheet from scratch, populating it with data, and finally changing the sheet structure by moving around rows and columns.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet APi, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

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
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>sheetstructure.rb</b> script.  The script follows a simple storyline to make it more relevant and easier to understand.

Betty is running a fundraising bakesale and needs to track her project status and inventory.  First, she needs to set up her project dashboard by creating a sheet with all the columns required to track the key attributes:  

	name: sheet_name,
    columns: [
      { title: "Baked Good", type: "TEXT_NUMBER", primary: true },
      { title: "Baker", type: "CONTACT_LIST" },
      { title: "Price Per Item", type: "TEXT_NUMBER" },
      { title: "Gluten Free?", type: "CHECKBOX", symbol: "FLAG" },
      { title: "Status", type: "PICKLIST", options: ["Started", "Finished", "Delivered"] }

	
Turns out, she missed an important column to track "Delivery Date" - so, she adds it to the end of the sheet:
	
	[…] 
    index: 5, title: column_name, type: "DATE"
	[…] 
	ss_connection.request('post', "/sheet/#{sheet_id}/columns", options)

Betty needs to figure out who is preparing what for the sale, and when the items are going to be ready.  She inserts several rows to track that information: 
	
    toBottom: true,
    rows: [
      { cells: [
        { columnId: sheet_columns[0]['id'], value: "Brownies" },
        { columnId: sheet_columns[1]['id'], value: "julieanne@example.com", strict: false },
        { columnId: sheet_columns[2]['id'], value: "$1", strict: false },
        { columnId: sheet_columns[3]['id'], value: true },
        { columnId: sheet_columns[4]['id'], value: "Finished" }


Ned Barnes, who is making Ginger Snaps, is often late.  Betty moves his cookies to the top of the list so that she can keep an eye on them:

	[…] 
	toTop: true
	[…] 
	ss_connection.request('put', "/row/#{rows1['result'][5]['id']}", options)
	
Betty realizes that a few of the items have already been delivered.  It would be handy to see them all in one place, so Betty takes advantage of Smartsheet's row hierarchy feature.  She creates a "Delivered" section and moves all the delivered items there, making them children of "Delivered" (using the <code>parentId</code> attribute) so that they appear indented.
	
As more people volunteer, Betty keeps adding new baked goods to the list as siblings of existing items (using the <code>siblingId</code>).

The bake sale list is coming together.  Looking at the sheet, Betty decides that the "Status" column ought to be moved up (to index 1)to make it easier to identify delinquent items:

	[…] 
    sheetId: sheet_id,
    index: 1,
	[…] 
	ss_connection.request('put', "/column/#{status_column['id']}", options)

Betty is pleased with the outcome - not only she is able to track the project items, but also the impromptu "dashboard" is conveniently laid out and easy to understand.
	
Congratulations!  You just completed your second Smartsheet API Ruby walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
