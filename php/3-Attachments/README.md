File Operations (PHP)
===
See our <b>Hellosmartsheet</b> and <b>SheetStructure</b> scripts for a hands-on introduction to the Smartsheet API.  The third in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by attaching a file and a URL to a row, downloading a file attachment, and finally deleting a file attachment.

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
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>Attachments.php</b> script.

The goal of this walkthrough to help you understand how to attach files and URLs to data containers in Smartsheet, and then access these attachments.  In Smartsheet, users can attach files to workspaces (not supported via the API as of 2013 07 22), sheets, rows, and discussion comments. 

<b>IMPORTANT</b>: Please note that as of this writing the Smartsheet API only supports file streaming upload and does not support multipart or chunked file upload. 

First, create a sheet with a couple of rows so that we have something to work with.  Now that the rows are in place, let's upload a file to the top row.

	$filename = "smartsheet.png";
    $fileToAttach = realpath($filename);

Set the required headers and upload the actual file:

	[…]
	array_push($headers, 'Content-Disposition: attachment; filename="'. $filename .'"');

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
	[…]

Attaching to sheets or discussion comments works similarly - see the [API docs](http://www.smartsheet.com/developers/api-documentation#id.4clt0m7fespn) for endpoints or more information.

To download the attached file, first fetch the attachment object which contains the URL to the downloadable file:

	$getAttachmentURL = $baseURL ."/attachment/{{ATTACHMENTID}}";

	
Extract the URL to the downloadable file and save the file to disk (or you can skip saving the file if you just want to work with it in memory):

	[…]
	$getFileObj = json_decode($getAttachmentResponse);

    $ch = curl_init($getFileObj->url);
    $localFile = fopen($savePath, 'wb');
    curl_setopt($ch, CURLOPT_FILE, $localFile);
    curl_setopt($ch, CURLOPT_HEADER, 0);

   	$downloadResponse = curl_exec($ch);
	[…]

	
Congratulations!  You just completed your third Smartsheet API PHP walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
