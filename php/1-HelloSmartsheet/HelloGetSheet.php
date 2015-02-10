<?php
	/**
	Copyright 2014 Smartsheet, Inc.

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
			change ids to Strings 

		ALSO NOTE: for simplicity, error handling and input validation has been neglected.
	**/

	$baseURL = "https://api.smartsheet.com/1.1";
	$sheetsURL = $baseURL. "/sheets/";
	$shareSheetURL = $baseURL. "/sheet/{{SHEETID}}/shares";
	$getSheetURL = $baseURL. "/sheet/{{SHEETID}}";

	// Prompt user for access token
	echo "Enter Smartsheet API access token: ";
	$handle = fopen ("php://stdin","r");
	$inputToken = trim(fgets($handle));

	if ($inputToken != "") {
		// Create Headers Array for Curl
		$headers = array(
			"Authorization: Bearer " .$inputToken
		);

		// Connect to Smartsheet API to get Sheet List
		$curlSession = curl_init($sheetsURL);
		curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

		$smartsheetData = curl_exec($curlSession);
		// Assign response to PHP object 
		$sheetsObj = json_decode($smartsheetData);

		if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {	
			echo "Oh No! Could not grab sheet list. Error: (". $sheetsObj->errorCode .") ". $sheetsObj->message ."\n"; 
		} else { 
			// close curlSession 
		curl_close($curlSession); 

			// List Sheets
			if (count($sheetsObj) > 0) {    		
				$i = 1;

				// Output numbered list of sheets
				foreach ($sheetsObj as $sheet){
					echo $i++ .": ". $sheet->name ."\n";
				}

				// Output total number of sheets
				echo "\nTotal Sheets: ". count($sheetsObj) ."\n\n";

				// Prompt user to select a sheet to share
				echo "Enter the number of the sheet you wish to view: ";
				$handle = fopen ("php://stdin","r");
				$inputSheetNum = trim(fgets($handle));

				// Set chosenSheet object
				$chosenSheet = $sheetsObj[$inputSheetNum-1];
		
				// Call Smartsheet API to share sheet
				$getSheetURL = str_replace('{{SHEETID}}', $chosenSheet->id, $getSheetURL); 
				array_push($headers, "Content-Type: application/json");

				// Connect to Smartsheet API to get Selected Sheet
				$curlSession = curl_init($getSheetURL);
				curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
				curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

				$getSheetResponseData = curl_exec($curlSession);
				// Assign response to variable 
				$sheetObj = json_decode($getSheetResponseData);

				if (curl_getinfo($curlSession, CURLINFO_HTTP_CODE) != 200) {	
					echo "Whoops! The following error occured.\n"; 
					echo "Error: (". $sheetObj->errorCode .") ". $sheetObj->message ."\n"; 
				} else { 
					echo "\n";
					echo "Sheet name: ". $sheetObj->name ."\n";
					echo "Columns: " ;
					foreach ($sheetObj->columns as $column) {
						echo $column->title ."\n";
					} 
					echo "\n";
					echo "Rows: ";
					foreach ($sheetObj->rows as $row) {
						foreach ($row->cells as $cell) {
							echo $cell->value .", ";
						}
						echo "\n";
					}
					echo "Have a nice day!\n\n";

					// close curlSession 
					curl_close($curlSession); 
				}
			} else {		
				echo "No sheets for you!";
			}
		}
		echo "Goodbye!\n\n";
	}
?>