Sheet Structure: Betty's Bake Sale (PHP)
===
This is a sample PHP script that demonstrates various actions that can be performed on a sheet with the Smarthseet API. See our <b>HelloSmartsheet</b> interactive script for a basic introduction to the Smartsheet API.  The second in the series, this non-interactive script walks you through the more advanced Smartsheet API calls. 

The topics covered in this sample are:

* creating a sheet from scratch
* populating a sheet with data
* moving rows 
* moving columns

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Dependencies
---
This script has been tested running 64-bit PHP version 5.3.15. To make this compatible with 32-bit PHP you'll need to change the ids to strings.

Usage
---
[Generate a Smartsheet Access Token](http://www.smartsheet.com/developers/api-documentation#h.5osh0dl59e5m) and insert it into the script:

	// Insert your Smartsheet API Token here
    $accessToken = " ";


Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>SheetStructure.php</b> script.  The script follows a simple storyline to make it more relevant and easier to understand.

Betty is running a fundraising bakesale and needs to track her project status and inventory.  First, she needs to set up her project dashboard by creating a sheet with all the columns required to track the key attributes:  

	$sheetName = "Betty's Bake Sale";

    $theSheet = new Sheet();
    $theSheet->name = $sheetName;

    $columns = array();

    $columnOne = new Column();
    $columnOne->title = "Baked Goods";
    $columnOne->type = "TEXT_NUMBER";
    $columnOne->primary = true;
    array_push($columns, $columnOne);

    $columnTwo = new Column();
    $columnTwo->title = "Baker";
    $columnTwo->type = "CONTACT_LIST";
    array_push($columns, $columnTwo);

    $columnThree = new Column();
    $columnThree->title = "Price Per Item";
    $columnThree->type = "TEXT_NUMBER";
    array_push($columns, $columnThree);

    $columnFour = new Column();
    $columnFour->title = "Gluten Free?";
    $columnFour->type = "CHECKBOX";
    $columnFour->symbol = "FLAG";
    array_push($columns, $columnFour);

    $columnFive = new Column();
    $columnFive->title = "Status";
    $columnFive->type = "PICKLIST";
    $columnFive->options = array("Started", "Finished", "Delivered");
    array_push($columns, $columnFive);

    $theSheet->columns = $columns;

    $postfields = json_encode($theSheet);
	
Turns out, she missed an important column to track "Delivery Date" - so, she adds it to the end of the sheet:
	
	[…] 
    $newColumnName = "Delivery Date";
    […]
    $newColumn = new Column();
    $newColumn->title = $newColumnName;
    $newColumn->type = "DATE";
    $newColumn->index = 5;
    […] 
	$postfields = json_encode($newColumn);

    $curlSession = curl_init($columnURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $newColumnResponse = curl_exec($curlSession);

Betty needs to figure out who is preparing what for the sale, and when the items are going to be ready.  She inserts several rows to track that information: 
	
    $rows = array();
    $rowOne = new Row();
    $rowOneCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Brownies";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowOneCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "julieann@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowOneCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$1";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowOneCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = TRUE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowOneCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Finished";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowOneCells, $cellFive);

    $rownOne->cells = $rowOneCells;
    array_push($rows, $rownOne);


Ned Barnes, who is making Ginger Snaps, is often late.  Betty moves his cookies to the top of the list so that she can keep an eye on them:

	[…] 
	$putBody = '{"toTop":true}';
	[…] 
	
Betty realizes that a few of the items have already been delivered.  It would be handy to see them all in one place, so Betty takes advantage of Smartsheet's row hierarchy feature.  She creates a "Delivered" section and moves all the delivered items there, making them children of "Delivered" (using the <code>parentId</code> attribute) so that they appear indented.
	
As more people volunteer, Betty keeps adding new baked goods to the list as siblings of existing items (using the <code>siblingId</code>).

The bake sale list is coming together.  Looking at the sheet, Betty decides that the "Status" column ought to be moved up (to index 1) to make it easier to identify delinquent items:

	[…] 
    $movingCol = new ColumnModify();
    $movingCol->title = $column->title;
    $movingCol->type = $column->type;
    $movingCol->index = 1;
    $movingCol->sheetId = $theSheet->id;
    $movingColId = $column->id;	
    […] 
	$columnURL = $baseURL. "/column/". $movingColId;
    $putBody = json_encode($movingCol);
    […]
    $curlSession = curl_init($columnURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_PUT, 1);
    curl_setopt($curlSession, CURLOPT_INFILE, $fp);
    curl_setopt($curlSession, CURLOPT_INFILESIZE, strlen($putBody));
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $moveColumnResponse = curl_exec($curlSession);
    
Betty is pleased with the outcome - not only she is able to track the project items, but also the impromptu "dashboard" is conveniently laid out and easy to understand.
	
Congratulations!  You just completed your second Smartsheet API PHP walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
