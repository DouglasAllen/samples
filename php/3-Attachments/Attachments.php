<?php
    /**
    Copyright 2015 Smartsheet, Inc.

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

            NOTE: for simplicity, error handling and input validation has been neglected.

            NOTE: Tested used PHP version 5.3.15
    **/

    // Initialize URL Variables
    $baseURL = "https://api.smartsheet.com/1.1";
    $sheetsURL = $baseURL ."/sheets/";
    $rowsURL = $baseURL. "/sheet/{{SHEETID}}/rows";
    $rowAttachmentsURL = $baseURL. "/sheet/{{SHEETID}}/row/{{ROWID}}/attachments";
    $getAttachmentURL = $baseURL ."/sheet/{{SHEETID}}/attachment/{{ATTACHMENTID}}";

    // Insert your Smartsheet API Token here
    $accessToken = "";
    
    // Create Headers Array for Curl
    $headers = array(
    "Authorization: Bearer ". $accessToken,
    "Content-Type: application/json"
    );

    echo "Starting HelloSmartsheet3: Attachments...\n\n";

    // Create new sheet
    $sheetName = "Attachment Example";

    $theSheet = new Sheet();
    $theSheet->name = $sheetName;

    $columns = array();

    $columnOne = new Column();
    $columnOne->title = "Column 1";
    $columnOne->type = "TEXT_NUMBER";
    $columnOne->primary = true;
    array_push($columns, $columnOne);

    $columnTwo = new Column();
    $columnTwo->title = "Column 2";
    $columnTwo->type = "TEXT_NUMBER";
    array_push($columns, $columnTwo);

    $columnThree = new Column();
    $columnThree->title = "Column 3";
    $columnThree->type = "TEXT_NUMBER";
    array_push($columns, $columnThree);

    $theSheet->columns = $columns;

    $postfields = json_encode($theSheet);

    // Connect to Smartsheet API to create sheet
    $curlSession = curl_init($sheetsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $createResponse = curl_exec($curlSession);
    // Assign response to variable 
    $createObj = json_decode($createResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        echo "Oh No! Could not create sheet. Error: (". $createObj->errorCode .") ". $createObj->message ."\n";
        if ($createObj->errorCode == 1004) {
            echo "You may want to check your access token.\n";
        }
        exit();
        
    } else { 
        $theSheet->id = $createObj->result->id;

        // Tell the user!
        echo "Woo hoo! Sheet '". $createObj->result->name ."' created, id: ".  $theSheet->id ."\n"; 

        // close curlSession 
        curl_close($curlSession); 
    }

    // Add a row to attach a document to
    $theRow = new Row();
    $rowCells = array();
    $rows = array();

    $cellOne = new Cell();
    $cellOne->columnId = $createObj->result->columns[0]->id;
    $cellOne->value = "Value1";
    array_push($rowCells, $cellOne);

    $cellTwo = new Cell();
    $cellTwo->columnId = $createObj->result->columns[1]->id;
    $cellTwo->value = "Value2";
    array_push($rowCells, $cellTwo);    

    $cellThree = new Cell();
    $cellThree->columnId = $createObj->result->columns[2]->id;
    $cellThree->value = "Value3";
    array_push($rowCells, $cellThree);    

    $theRow->cells = $rowCells;
    array_push($rows, $theRow);

    $theWrapper = new RowWrapper();
    $theWrapper->toBottom = TRUE;
    $theWrapper->rows = $rows;
    
    $postfields = json_encode($theWrapper);
    $rowsURL = str_replace('{{SHEETID}}', $theSheet->id, $rowsURL);
    
    // Connect to Smartsheet API to add rows of data to the sheet
    $curlSession = curl_init($rowsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $rowsResponse = curl_exec($curlSession);
    // Assign response to variable 
    $addRowsObj = json_decode($rowsResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        exit("Oh No! Could not add rows to sheet. Error: (". $addRowsObj->errorCode .") ". $addRowsObj->message ."\n");
    } else { 
        // Inform the user
        echo "Added ". count($addRowsObj->result) ." rows of data to '".  $theSheet->name ."'\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Get document to attach
    $filename = "smartsheet.png";
    $fileToAttach = realpath($filename);

    $fileStream = fopen($fileToAttach, 'r') or die($filename ."file won't open");

    // Attach 
    $rowAttachTags = array('{{ROWID}}', '{{SHEETID}}');
    $rowAttachVals = array($addRowsObj->result[0]->id, $theSheet->id);
    $rowAttachmentsURL = str_replace($rowAttachTags, $rowAttachVals, $rowAttachmentsURL);

    array_pop($headers); // Remove json content-type from headers
    array_push($headers, 'Content-Disposition: attachment; filename="'. $filename .'"');

    // Connect to Smartsheet API to post file attachment
    $curlSession = curl_init($rowAttachmentsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);   
    curl_setopt($curlSession, CURLOPT_HEADER, true);   
    curl_setopt($curlSession, CURLOPT_INFILE, $fileStream);
    curl_setopt($curlSession, CURLOPT_INFILESIZE, filesize($fileToAttach));
    curl_setopt($curlSession, CURLOPT_UPLOAD, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, '');
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);
    curl_setopt($curlSession, CURLOPT_CUSTOMREQUEST, "POST");

    $attachResponse = curl_exec($curlSession);
    // Assign response to variable 
    $attachObj = json_decode($attachResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        exit("Oh No! Could not attach file to sheet. Error: (". $attachObj->errorCode .") ". $attachObj->message ."\n");
    } else { 
        // Inform the user
        echo "Attached ". $filename ." file to row ". $addRowsObj->result[0]->id ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // List Row Attachments
    $curlSession = curl_init($rowAttachmentsURL);
    $headers = array(
        "Authorization: Bearer ". $accessToken,
        "Content-Type: application/json"
    );
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $getAttachmentsResponse = curl_exec($curlSession);
    // Assign response to variable 
    $attachments = json_decode($getAttachmentsResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        exit("Oh No! Could not get list of attachments. Error: (". $attachments->errorCode .") ". $attachments->message ."\n");
    }   

    // close curlSession 
    curl_close($curlSession); 
    $rowAttachTags = array('{{ROWID}}', '{{SHEETID}}','{{ATTACHMENTID}}');
    $rowAttachVals = array($addRowsObj->result[0]->id, $theSheet->id,$attachments[0]->id);
    $getAttachmentURL = str_replace($rowAttachTags, $rowAttachVals, $getAttachmentURL);
    $savePath = "savedSmartsheet.png";

    // Connect to Smartsheet API to download file attachment
    $curlSession = curl_init($getAttachmentURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $getAttachmentResponse = curl_exec($curlSession);
    // Assign response to variable 
    $getFileObj = json_decode($getAttachmentResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        echo "Oh No! Could not get attached file. Error: (". $getFileObj->errorCode .") ". $getFileObj->message ."\n";
    } else { 
        $ch = curl_init($getFileObj->url);
        $localFile = fopen($savePath, 'wb');
        curl_setopt($ch, CURLOPT_FILE, $localFile);
        curl_setopt($ch, CURLOPT_HEADER, 0);

        $downloadResponse = curl_exec($ch);

        // Inform the user
        echo "File ". $savePath ." downloaded and saved locally \n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Attach URL to row
    $urlAttachment = new Attachment();
    $urlAttachment->name = "Google";
    $urlAttachment->description = "A useful search engine";
    $urlAttachment->attachmentType = "LINK";
    $urlAttachment->url = "http://www.google.com";

    $postfields = json_encode($urlAttachment);
    $curlSession = curl_init($rowAttachmentsURL);

    // Connect to Smartsheet API to attach URL
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);   
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $attachResponse = curl_exec($curlSession);
    // Assign response to variable 
    $attachObj = json_decode($attachResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        echo "Oh No! Could not attach URL. Error: (". $attachObj->errorCode .") ". $attachObj->message ."\n";
    } else { 

        // Inform the user
        echo "Attached the ". $urlAttachment->name ." URL to row ". $addRowsObj->result[0]->id ."\n\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Connect to Smartsheet API to delete file attachment
    $curlSession = curl_init($getAttachmentURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);   
    curl_setopt($curlSession, CURLOPT_CUSTOMREQUEST, "DELETE");   
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $deleteResponse = curl_exec($curlSession);
    // Assign response to variable 
    $deleteObj = json_decode($deleteResponse);

    if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {
        echo "Oh No! Could not delete attachment. Error: (". $deleteObj->errorCode .") ". $deleteObj->message ."\n";
    } else { 

        // Inform the user
        echo "Attachment ". $attachObj->result->id ." deleted. \n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    echo "\nAll done\n";

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

    class Attachment{
        public $id;
        public $name;
        public $url;
        public $attachmentType;
        public $description;
    }
?>