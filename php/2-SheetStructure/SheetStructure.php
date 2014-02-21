<?php
    /**
        Copyright 2013 Smartsheet, Inc.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

            NOTE: This sample is for 64-bit PHP. To make this compatible with 32-bit
    		change ids to Strings. 

            NOTE: For simplicity, error handling and input validation has been neglected.
            NOTE: Tested used PHP version 5.3.15
    **/

    // Initialize URL Variables
    $baseURL = "https://api.smartsheet.com/1.1";
    $sheetsURL = $baseURL ."/sheets/";
    $columnURL = $baseURL. "/sheet/{{SHEETID}}/columns";

    // Insert your Smartsheet API Token here
    $accessToken = "";

    // Create Headers Array for Curl
    $headers = array(
        "Authorization: Bearer ". $accessToken,
        "Content-Type: application/json"
    );

    // Welcome user
    echo "Starting HelloSmartsheet2: Betty's Bake Sale...\n\n";
    echo "mmmmmm....baked goods\n\n";

    // Create new sheet
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

    // Connect to Smartsheet API to get Sheet List
    $curlSession = curl_init($sheetsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $createResponse = curl_exec($curlSession);

    // Assign response to PHP object 
    $createObj = json_decode($createResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {    
            
    //if (curl_errno($curlSession)) { 
        echo "Oh No! Could not create sheet.\n";
        echo "Error: (". $createObj->errorCode .") ". $createObj->message ."\n"; 
        exit;
       // print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        //$createObj = json_decode($createResponse);
        $theSheet->id = $createObj->result->id;

        // Tell the user!
        echo "Woo hoo! Sheet ". $createObj->result->name ." created, id: ".  $theSheet->id ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Add column to new sheet
    $newColumnName = "Delivery Date";
    echo "Adding column ". $newColumnName ." to ". $theSheet->name ."\n";

    $newColumn = new Column();
    $newColumn->title = $newColumnName;
    $newColumn->type = "DATE";
    $newColumn->index = 5;

    $columnURL = str_replace('{{SHEETID}}', $theSheet->id, $columnURL);
    $postfields = json_encode($newColumn);

    $curlSession = curl_init($columnURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $newColumnResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $columnObj = json_decode($newColumnResponse);

        // Tell the user!
        echo "New column ". $columnObj->result->title ." added to ". $theSheet->name ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Get list of columns
    echo "Fetching column list for ". $theSheet->name ."\n";

    $curlSession = curl_init($columnURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $columnsResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $columnsObj = json_decode($columnsResponse);

        // Tell the user!
        echo "Columns retrieved!\n\n"; 

        // close curlSession 
        curl_close($curlSession); 
    }

    // Create rows with data
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

    $rowOne->cells = $rowOneCells;
    array_push($rows, $rowOne);

    $rowTwo = new Row();
    $rowTwoCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Snickerdoodles";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowTwoCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "stevenelson@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowTwoCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$1";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowTwoCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = FALSE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowTwoCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Delivered";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowTwoCells, $cellFive);

    $cellSix = new Cell();
    $cellSix->value = "2013-09-04";
    $cellSix->columnId = $columnsObj[5]->id;
    array_push($rowTwoCells, $cellSix);

    $rowTwo->cells = $rowTwoCells;
    array_push($rows, $rowTwo);

    $rowThree = new Row();
    $rowThreeCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Rice Krispy Treats";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowThreeCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "rickthames@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowThreeCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$.50";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowThreeCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = FALSE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowThreeCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Started";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowThreeCells, $cellFive);

    $rowThree->cells = $rowThreeCells;
    array_push($rows, $rowThree);

    $rowFour = new Row();
    $rowFourCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Muffins";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowFourCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "sandrassmart@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowFourCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$1.50";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowFourCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = FALSE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowFourCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Finished";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowFourCells, $cellFive);

    $rowFour->cells = $rowFourCells;
    array_push($rows, $rowFour);
    
    $rowFive = new Row();
    $rowFiveCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Chocolate Chip Cookies";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowFiveCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "janedaniels@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowFiveCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$1";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowFiveCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = FALSE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowFiveCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Delivered";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowFiveCells, $cellFive);

    $cellSix = new Cell();
    $cellSix->value = "2013-09-05";
    $cellSix->columnId = $columnsObj[5]->id;
    array_push($rowFiveCells, $cellSix);

    $rowFive->cells = $rowFiveCells;
    array_push($rows, $rowFive);

    $rowSix = new Row();
    $rowSixCells = array();

    $cellOne = new Cell();
    $cellOne->value = "Ginger Snaps";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($rowSixCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->value = "nedbarnes@example.com";
    $cellTwo->columnId = $columnsObj[1]->id;
    array_push($rowSixCells, $cellTwo);

    $cellThree = new Cell();
    $cellThree->value = "$.50";
    $cellThree->columnId = $columnsObj[2]->id;
    array_push($rowSixCells, $cellThree);

    $cellFour = new Cell();
    $cellFour->value = TRUE;
    $cellFour->columnId = $columnsObj[3]->id;
    array_push($rowSixCells, $cellFour);

    $cellFive = new Cell();
    $cellFive->value = "Unknown";
    $cellFive->columnId = $columnsObj[4]->id;
    array_push($rowSixCells, $cellFive);

    $rowSix->cells = $rowSixCells;
    array_push($rows, $rowSix);

    // Add rows array to the RowWrapper in preparations for sending to Smartsheet API
    $theWrapper = new RowWrapper();
    $theWrapper->toBottom = TRUE;
    $theWrapper->rows = $rows;
    
    $postfields = json_encode($theWrapper);
    $rowsURL = $baseURL. "/sheet/" .$theSheet->id. "/rows";
    
    $curlSession = curl_init($rowsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $rowsResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $addRowsObj = json_decode($rowsResponse);

        // Inform the user
        echo "Added ". count($addRowsObj->result) ." rows of data to ".  $theSheet->name ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Move row 6 to the top
    echo "Moving row Ned's Ginger Snaps to the top of the sheet.\n";

    $putBody = '{"toTop":true}';
    $rowURL = $baseURL. "/row/". $addRowsObj->result[5]->id;

    // Create temporary file to store the putBody
    $fp = fopen("php://temp/maxmemory:256", "w");
    
    if(!$fp){
        die(" Uh oh. Couldn't open temp memory data. ");
    }

    // writes the putBody to the temporary file
    fwrite($fp, $putBody);

    // Sets the file pointer to the begining of the temporary file
    fseek($fp, 0);

    $curlSession = curl_init($rowURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_PUT, 1);
    curl_setopt($curlSession, CURLOPT_INFILE, $fp);
    curl_setopt($curlSession, CURLOPT_INFILESIZE, strlen($putBody));
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $moveRowResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $moveRowObj = json_decode($moveRowResponse);

        // Tell the user!
        echo "Row 6 has reached the summit!\n";
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Insert empty rows for spacing
    $moreRows = array();

    $newBlankRow = new Row();
    $blankRowCells = array();
    $cellOne = new Cell();
    $cellOne->value = "";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($blankRowCells, $cellOne);
    $newBlankRow->cells = $blankRowCells;

    array_push($moreRows, $newBlankRow);
    
    $secondBlankRow = new Row();
    $secondBlankRowCells = array();
    $cellOne = new Cell();
    $cellOne->value = "";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($secondBlankRowCells, $cellOne);
    
    $secondBlankRow->cells = $secondBlankRowCells;
    array_push($moreRows, $secondBlankRow);

    $deliveredRow = new Row();
    $deliveredRowCells = array();
    $cellOne = new Cell();
    $cellOne->value = "Delivered";
    $cellOne->columnId = $columnsObj[0]->id;
    array_push($deliveredRowCells, $cellOne);
    
    $deliveredRow->cells = $deliveredRowCells;
    array_push($moreRows, $deliveredRow);

    $theWrapper = new RowWrapper();
    $theWrapper->toBottom = TRUE;
    $theWrapper->rows = $moreRows;
    
    $postfields = json_encode($theWrapper);
    $rowsURL = $baseURL. "/sheet/" .$theSheet->id. "/rows";
    
    $curlSession = curl_init($rowsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $rowsResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $deliveredRowsObj = json_decode($rowsResponse);

        // Tell the user!
        echo "Added ". count($deliveredRowsObj->result) ." rows to ".  $theSheet->name ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Move Delivered rows to be children of the Delivered row 
    echo "Moving delivered rows to Delivered section...\n";

    // Get rowId for newly added Delivered row
    $deliveredRowId = "";

    foreach ($deliveredRowsObj->result as $deliveredRow) {
        if($deliveredRow->cells[0]->value == "Delivered"){
            $deliveredRowId = $deliveredRow->id;
            break;
        }
    }

    foreach ($addRowsObj->result as $row) {
        if($row->cells[4]->value == "Delivered"){
            $putBody = '{"parentId":'. $deliveredRowId .'}';
            $rowURL = $baseURL. "/row/". $row->id;

            // Create temporary file to store the putBody
            $fp = fopen("php://temp/maxmemory:256", "w");
            
            if(!$fp){
                die(" Uh oh. Couldn't open temp memory data. ");
            }

            // writes the putBody to the temporary file
            fwrite($fp, $putBody);

            // Sets the file pointer to the begining of the temporary file
            fseek($fp, 0);

            $curlSession = curl_init($rowURL);
            curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($curlSession, CURLOPT_PUT, 1);
            curl_setopt($curlSession, CURLOPT_INFILE, $fp);
            curl_setopt($curlSession, CURLOPT_INFILESIZE, strlen($putBody));
            curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

            $moveRowResponse = curl_exec($curlSession);

            if (curl_errno($curlSession)) { 
                print "Oh No! Error: " . curl_error($curlSession); 
            } else { 
                // Assign response to variable 
                $moveRowObj = json_decode($moveRowResponse);

                // Tell the user!
                echo "Row id ". $row->id ." moved to Delivered section\n";
                
                // close curlSession 
                curl_close($curlSession); 
            }    
        }
    }

    echo "Adding a couple more rows...\n";

    $siblingRows = array();
    $siblingRowOne = new Row();
    $sibOneCells = array();

    $sibOneCellOne = new Cell();
    $sibOneCellOne->value = "Scones";
    $sibOneCellOne->columnId = $columnsObj[0]->id;
    array_push($sibOneCells, $sibOneCellOne);

    $sibOneCellTwo = new Cell();
    $sibOneCellTwo->value = "tomlively@example.com";
    $sibOneCellTwo->columnId = $columnsObj[1]->id;
    array_push($sibOneCells, $sibOneCellTwo);

    $sibOneCellThree = new Cell();
    $sibOneCellThree->value = "$1.50";
    $sibOneCellThree->columnId = $columnsObj[2]->id;
    array_push($sibOneCells, $sibOneCellThree);

    $sibOneCellFour = new Cell();
    $sibOneCellFour->value = TRUE;
    $sibOneCellFour->columnId = $columnsObj[3]->id;
    array_push($sibOneCells, $sibOneCellFour);

    $sibOneCellFive = new Cell();
    $sibOneCellFive->value = "Finished";
    $sibOneCellFive->columnId = $columnsObj[4]->id;
    array_push($sibOneCells, $sibOneCellFive);

    $siblingRowOne->cells = $sibOneCells;
    array_push($siblingRows, $siblingRowOne);
    
    $siblingRowTwo = new Row();
    $sibTwoCells = array();

    $sibTwoCellOne = new Cell();
    $sibTwoCellOne->value = "Lemon Bars";
    $sibTwoCellOne->columnId = $columnsObj[0]->id;
    array_push($sibTwoCells, $sibTwoCellOne);

    $sibTwoCellTwo = new Cell();
    $sibTwoCellTwo->value = "rickthames@example.com";
    $sibTwoCellTwo->columnId = $columnsObj[1]->id;
    array_push($sibTwoCells, $sibTwoCellTwo);

    $sibTwoCellThree = new Cell();
    $sibTwoCellThree->value = "$1";
    $sibTwoCellThree->columnId = $columnsObj[2]->id;
    array_push($sibTwoCells, $sibTwoCellThree);

    $sibTwoCellFour = new Cell();
    $sibTwoCellFour->value = TRUE;
    $sibTwoCellFour->columnId = $columnsObj[3]->id;
    array_push($sibTwoCells, $sibTwoCellFour);

    $sibTwoCellFive = new Cell();
    $sibTwoCellFive->value = "Started";
    $sibTwoCellFive->columnId = $columnsObj[4]->id;
    array_push($sibTwoCells, $sibTwoCellFive);

    $siblingRowTwo->cells = $sibTwoCells;
    array_push($siblingRows, $siblingRowTwo);

    $theWrapper = new RowWrapper();
    $theWrapper->siblingId = $addRowsObj->result[3]->id;
    $theWrapper->rows = $siblingRows;
    
    $postfields = json_encode($theWrapper);
    $rowsURL = $baseURL. "/sheet/" .$theSheet->id. "/rows";
    
    $curlSession = curl_init($rowsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $sibRowsResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $sibRowsObj = json_decode($sibRowsResponse);

        // Tell the user!
        echo "Added ". count($sibRowsObj->result) ." sibling rows of data to ".  $theSheet->name ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Move Status Column to Index 1
    echo "\nMoving Status column to index 1\n";

    // Get Status column id
    foreach($columnsObj as $column){
        if($column->title == "Status"){
            $movingCol = new ColumnModify();
            $movingCol->title = $column->title;
            $movingCol->type = $column->type;
            $movingCol->index = 1;
            $movingCol->sheetId = $theSheet->id;
            $movingColId = $column->id;
            break;
        }
    }

    $columnURL = $baseURL. "/column/". $movingColId;
    $putBody = json_encode($movingCol);

    // Create temporary file to store the putBody
    $fp = fopen("php://temp/maxmemory:256", "w");
    
    if(!$fp){
        die(" Uh oh. Couldn't open temp memory data. ");
    }

    // writes the putBody to the temporary file
    fwrite($fp, $putBody);

    // Sets the file pointer to the begining of the temporary file
    fseek($fp, 0);

    $curlSession = curl_init($columnURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_PUT, 1);
    curl_setopt($curlSession, CURLOPT_INFILE, $fp);
    curl_setopt($curlSession, CURLOPT_INFILESIZE, strlen($putBody));
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $moveColumnResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $moveColumnObj = json_decode($moveColumnResponse);

        // Tell the user!
        echo "Moved column id ". $moveColumnObj->result->id ."\n";
        
        // close curlSession 
        curl_close($curlSession); 
    }        
    echo "\nDING! Betty's Bake Sale sheet is all done.\n\n";

    // Classes
    class Sheet{
        public $id;
        public $name;
        public $columns;
    }

    class Column{
        public $id;
        public $sheetId;
        public $title;
        public $type;
        public $symbol;
        public $primary;
        public $options;
        public $index;
    } 

    class ColumnModify{
        public $index;
        public $title;
        public $sheetId;
        public $type;
        public $options;
        public $symbol;
        public $systemColumnType;
        public $autoNumberFormat;
    }

    class Row{
        public $id;
        public $cells;
    }

    class RowWrapper{
        public $toTop;
        public $toBottom;
        public $parentId;
        public $siblingId;
        public $rows;
    }

    class Cell{
        public $value;
        public $displayValue;
        public $columnId;
        public $strict;
    }
?>