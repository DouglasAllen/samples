Third-party app (PHP)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b>, <b>Attachments</b> and <b>Admin</b> scripts for a hands-on introduction to the Smartsheet API.  The fifth in the series, this sample third-party PHP app shows how to use OAuth 2.0 to authenticate with Smartsheet and access data in a Smartsheet account on behalf of a user.

##Smartsheet API
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

The Smartsheet API documentation has a detailed section on [third-party applications](http://www.smartsheet.com/developers/api-documentation#h.opcwlo3avvxk) which takes you step by step trough the Smartsheet OAuth 2.0 flow.  Please review it and familiarize yourself with the flow prior to implementing this example app.

## Notes and Caveats
* This example app has been tested running 64-bit PHP version 5.3.15.
* This example app does not make use of persistent storage.

What's not addressed in this walkthrough:

* Handling of the Smartsheet API rate limit.
* Smartsheet refresh token management.

## Code
This walkthrough highlights only some parts of the code.  For the full code, please see the complete app.

The goal of this walkthrough to help you understand how to use OAuth 2.0 to authenticate with Smartsheet.  Let's get started.

First, [register](http://smartsheet.com/developers/register) for Smartsheet Developer Tools so you can create third-party apps. Once you have access to Developer Tools, you will have a Developer Tools… option in the Account menu of the Smartsheet UI. In the Developer Tools area you'll see a a button to Create New App, which will bring up the App Profile form. As you create a profile for the sample application make note of the generated values for the app ID and secret, as well as the App redirect URL value that you enter. 

Those above values will be used in `index.php` for the values of `$_SESSION['clientID']` and `$_SESSION['appSecret']`, as seen below.  

	[…]
	// Use a value that matches your development configuration.
    $localDomain = "";
	[…]
	// Set these values to match your Third Party App configuration in Smartsheet
    $_SESSION['clientId'] = "";
    $_SESSION['appSecret'] = "";
    $_SESSION['redirectURI'] = $localDomain ."";
    
With those values entered, the application will present a page with a link to Login to Smartsheet. The address for that link utilizes `clientId` and `redirectURI` that we just entered into the session.

    $smartSheetURI = "https://www.smartsheet.com/b/authorize?response_type=code";
    $smartSheetURI .= "&client_id=". $_SESSION['clientId'] ;
    $smartSheetURI .= "&redirect_uri=". $_SESSION['redirectURI'];
    $smartSheetURI .= "&scope=READ_SHEETS,WRITE_SHEETS";
    
Clicking on the link will take the user to a page that requests access to the user's account on behalf of the third-party application. Clicking Allow will provide the necessary authorization code for the application to complete its tasks. Clicking Deny will return an `error=access_denied` message that will cause the application to stop. 

After clicking Allow, the important piece returned in the response from this call is the authorization code, which will be used in another call to retrieve the access token.     

When requesting the access token, one difference between this call and all others made against the Smartsheet API is that the `Content-Type` in the http header needs to be `application/x-www-form-urlencoded` rather than `application/json`.      

Below are the values sent to the API in the token request call.

	$postfields = array(
    	"grant_type"=>"authorization_code",
        "code"=>$authCode,
        "client_id"=>$clientId,
        "hash"=>$hashedSecret,
        "redirect_uri"=>urlencode($redirectURI) 
    );
    
The one value that hasn't been seen before is the `hashedSecret`. This is created by hashing the `appSecret` with the `authCode` in the following manner:

	 $hashedSecret = hash("sha256", $secret. "|" .$authCode);

Using the returned access_token we will make one last call to the API to retrieve the information for the user currently logged in, and display it to the page. 

## User experience
Smartsheet users can view the list of third-party apps they approved to access their account by going to Account > Personal Settings > My Apps and Mobile Devices.  Users can revoke an app at any time.


Congratulations!  You just completed your fifth Smartsheet API PHP walkthrough.  We encourage you to play with the app, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)

