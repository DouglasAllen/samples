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
    $usersURL = $baseURL ."/users";
    $userURL = $baseURL ."/user/{{USERID}}";
    $sheetsURL = $baseURL ."/sheets";
    $usersSheetsURL = $usersURL ."/sheets";

    // Insert your Smartsheet API Token here
    $accessToken = "";
    $user1Email = "";
    $user2Email = "";
  
    // Create Headers Array for Curl
    $headers = array(
        "Authorization: Bearer ". $accessToken,
        "Content-Type: application/json"
    );

    echo "Starting HelloSmartsheet4: Administration...\n\n";
    
    echo "Adding user ". $user1Email ."\n";

    $user1 = new User();
    $user1->email = $user1Email;
    $user1->admin = false;
    $user1->licensedSheetCreator = true;
    $user1->firstName = "User";
    $user1->lastName = "One";
    $addUserURL = $usersURL ."?sendEmail=true";

    $postfields = json_encode($user1);

    $curlSession = curl_init($addUserURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);
    
    $addUser1Response = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $user1Obj = json_decode($addUser1Response);

        // Tell the user!
        echo "User ". $user1Obj->result->email ." added with userId ". $user1Obj->result->id ."\n";
        
        // close curlSession 
        curl_close($curlSession); 
    }
   echo "Adding user". $user2Email ."\n";

    $user2 = new User();
    $user2->email = $user2Email;
    $user2->admin = true;
    $user2->licensedSheetCreator = true;
    $user2->firstName = "User";
    $user2->lastName = "Two";

    $postfields = json_encode($user2);

    $curlSession = curl_init($addUserURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);
    
    $addUser2Response = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $user2Obj = json_decode($addUser2Response);

        // Tell the user!
        echo "User ". $user2Obj->result->email ." added with userId ". $user2Obj->result->id ."\n\n";
        
        // close curlSession 
        curl_close($curlSession); 
    }

    echo "Please visit the email inbox for the users ". $user1Email ." and ". $user2Email ." and confirm membership to the account.\n";
    echo "Press ENTER to continue\n";
    fgetc(STDIN);

    // Get User List 
    $curlSession = curl_init($usersURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);
    $listUsersResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        echo "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $userListObj = json_decode($listUsersResponse);

        // List Users
        if(count($userListObj) > 0){
            echo "The following are members of your account: \n";

            foreach($userListObj as $user){
                echo "\t". $user->email ." \n";
            }
        } else {
            echo "Do you feel lonely? Because, you have no users.\n" ;
        }

        // close curlSession 
        curl_close($curlSession); 
    }

    echo "\n";

    // Create Sheet as admin
    $adminSheet = new Sheet();
    $adminCols = array();
    $adminSheet->name = "Admin's Sheet";

    $adminCol1 = new Column();
    $adminCol1->title = "Column 1";
    $adminCol1->type = "TEXT_NUMBER";
    $adminCol1->primary = true;
    array_push($adminCols, $adminCol1);
    
    $adminCol2 = new Column();
    $adminCol2->title = "Column 2";
    $adminCol2->type = "TEXT_NUMBER";
    array_push($adminCols, $adminCol2);

    $adminCol3 = new Column();
    $adminCol3->title = "Column 3";
    $adminCol3->type = "TEXT_NUMBER";
    array_push($adminCols, $adminCol3);

    $adminSheet->columns = $adminCols;

    $postfields = json_encode($adminSheet);

    $curlSession = curl_init($sheetsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $createResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $createObj = json_decode($createResponse);
        $adminSheet->id = $createObj->result->id;

        // Tell the user!
        echo "Woo hoo! Sheet ". $createObj->result->name ." created, id: ".  $adminSheet->id ."\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }    

    // Create Sheet as user1
    $user1Sheet = new Sheet();
    $user1Cols = array();
    $user1Sheet->name = "User 1's Sheet";

    $user1Col1 = new Column();
    $user1Col1->title = "Column 1";
    $user1Col1->type = "TEXT_NUMBER";
    $user1Col1->primary = true;
    array_push($user1Cols, $user1Col1);
    
    $user1Col2 = new Column();
    $user1Col2->title = "Column 2";
    $user1Col2->type = "TEXT_NUMBER";
    array_push($user1Cols, $user1Col2);

    $user1Col3 = new Column();
    $user1Col3->title = "Column 3";
    $user1Col3->type = "TEXT_NUMBER";
    array_push($user1Cols, $user1Col3);

    $user1Sheet->columns = $user1Cols;

    $postfields = json_encode($user1Sheet);

    $headers = array(
        "Authorization: Bearer ". $accessToken,
        //Here is where the magic happens - Any action performed in this call will be on behalf of the
        //user provided. Note that this person must be a confirmed member of your org. 
        //Also note that the email address is url-encoded.
        "Assume-User: ". urlencode($user1Email),
        "Content-Type: application/json"
    );

    $curlSession = curl_init($sheetsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $createResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $createObj = json_decode($createResponse);
        $user1Sheet->id = $createObj->result->id;

        // Tell the user!
        echo "Woo hoo! Sheet ". $createObj->result->name ." created, id: ".  $user1Sheet->id ."\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // Create Sheet as user2
    $user2Sheet = new Sheet();
    $user2Cols = array();
    $user2Sheet->name = "User 2's Sheet";

    $user2Col1 = new Column();
    $user2Col1->title = "Column 1";
    $user2Col1->type = "TEXT_NUMBER";
    $user2Col1->primary = true;
    array_push($user2Cols, $user2Col1);
    
    $user2Col2 = new Column();
    $user2Col2->title = "Column 2";
    $user2Col2->type = "TEXT_NUMBER";
    array_push($user2Cols, $user2Col2);

    $user2Col3 = new Column();
    $user2Col3->title = "Column 3";
    $user2Col3->type = "TEXT_NUMBER";
    array_push($user2Cols, $user2Col3);

    $user2Sheet->columns = $user2Cols;

    $postfields = json_encode($user2Sheet);

    $headers = array(
        "Authorization: Bearer ". $accessToken,
        //Here is where the magic happens - Any action performed in this call will be on behalf of the
        //user provided. Note that this person must be a confirmed member of your org. 
        //Also note that the email address is url-encoded.
        "Assume-User: ". urlencode($user2Email),
        "Content-Type: application/json"
    );

    $curlSession = curl_init($sheetsURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_POST, 1);
    curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $createResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $createObj = json_decode($createResponse);
        $user2Sheet->id = $createObj->result->id;

        // Tell the user!
        echo "Woo hoo! Sheet ". $createObj->result->name ." created, id: ".  $user2Sheet->id ."\n"; 
        
        // close curlSession 
        curl_close($curlSession); 
    }

    // List all sheets in the org
    echo "\nThe following sheets are owned by members of your account: \n";

    $curlSession = curl_init($usersSheetsURL);
    $headers = array(
        "Authorization: Bearer ". $accessToken,
        "Content-Type: application/json"
    );

    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $sheetListResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $sheetListObj = json_decode($sheetListResponse);

        foreach($sheetListObj as $sheet){
            echo "\t". $sheet->name ." - ". $sheet->owner ."\n";
        } 

        // close curlSession 
        curl_close($curlSession); 
    }
    echo "\n";

    // Delete user1 and transfer sheets to user2
    $userURL = str_replace('{{USERID}}', $user1Obj->result->id, $userURL);
    $userURL.= "?transferTo=". $user2Obj->result->id;

    $curlSession = curl_init($userURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);   
    curl_setopt($curlSession, CURLOPT_CUSTOMREQUEST, "DELETE");   
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $deleteResponse = curl_exec($curlSession);

    if (curl_errno($curlSession)) { 
        print "Oh No! Error: " . curl_error($curlSession); 
    } else { 
        // Assign response to variable 
        $deleteObj = json_decode($deleteResponse);

        echo "\nUser 1 Deleted. Number of sheets transferred to User 2: ". $deleteObj->sheetsTransferred ."\n";

        // close curlSession 
        curl_close($curlSession); 
    }

    echo "\nGood-bye!\n";

    // Classes
    class User{
        public $firstName;
        public $lastName;
        public $email;
        public $admin;
        public $licensedSheetCreator;
    }

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

    class Row{
        public $id;
        public $cells;
    }

    class Cell{
        public $value;
        public $displayValue;
        public $columnId;
        public $strict;
    }
?>