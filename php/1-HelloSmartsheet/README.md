Hello Smartsheet (PHP)
===
This is a simple introduction to the Smartsheet API for PHP developers.  Hello Smartsheet is an interactive PHP Command Line Interface (CLI) script that walks you through a basic Smartsheet API integration by establishing a connection, fetching a list of sheets, and sharing one of the sheets with a collaborator.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers). Also be sure to [Generate an Access Token](http://www.smartsheet.com/developers/api-documentation#h.5osh0dl59e5m) in order for your applications access the API.

Dependencies
---
This script has been tested running 64-bit PHP version 5.3.15. To make this compatible with 32-bit PHP you'll need to change the ids to strings.

Data validation
---
Please note that no validation is performed on any data entered by the user.  We strongly encourage you to add data validation to any script you intend to use or distribute.


Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete HelloSmartsheet.php script.
	
Specify the base Smartsheet API URL:
	
	$baseURL = "https://api.smartsheet.com/1.1";

Prompt the user for API access token. Visit the API Documentation for instructions on how to [Generate an Access Token](http://www.smartsheet.com/developers/api-documentation#h.5osh0dl59e5m) in the Smartsheet UI.

	echo "Enter Smartsheet API access token: ";
	$handle = fopen ("php://stdin","r");
	$inputToken = trim(fgets($handle));
	
Fetch the list of your sheets:

	// Create Headers Array for Curl
	$headers = array(
		"Authorization: Bearer " .$inputToken
	);

	// Connect to Smartsheet API to get Sheet List
    $curlSession = curl_init($sheetsURL);
	curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

	$smartsheetData = curl_exec($curlSession);

To share a sheet with a collaborator, set the right headers and specify the recipient and her desired access level:

	array_push($headers, "Content-Type: application/json");
	array_push($headers, "Content-Length: ". strlen($postfields));
	
	// Assign values to postfields variable
	$postfields = '{"email":"' .$inputEmail. '","accessLevel":"' .$inputAccessLevel. '"}';

Finally, share the sheet:

	// Connect to Smartsheet API to share sheet
	$curlSession = curl_init($shareSheetURL);
	curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($curlSession, CURLOPT_POST, 1);
	curl_setopt($curlSession, CURLOPT_POSTFIELDS, $postfields);
	curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

	$shareResponseData = curl_exec($curlSession);	
Congratulations!  You just completed your first Smartsheet API PHP walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  

If you have any questions or suggestions about this document, the application, or about the Smartsheet API in general please contact us at api@smartsheet.com. Development questions can also be posted to [Stackoverflow](http://stackoverflow.com/) with the tag [smartsheet-api](http://stackoverflow.com/questions/tagged/smartsheet-api).

The Smartsheet Platform team.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
