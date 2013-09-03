File Operations (PHP)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b> and <b>Attachments</b> scripts for a hands-on introduction to the Smartsheet API.  The fourth in the series, this semi-interactive script walks you through the Smartsheet API administrative calls and capabilities by managing team members (creating, updating and deleting), executing operations on behalf of other team members, and listing all sheets in a team account.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet APi, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Dependencies
---
This script has been tested running 64-bit PHP version 5.3.15. To make this compatible with 32-bit PHP you'll need to change the ids to strings.


Usage
---
Smartsheet has several subscription options, or plans, ranging from Basic to Enterprise - see the [Smartsheet pricing page](http://smartsheet.com/pricing) for more information.  Administrative features are only available in multi-user plans - Team and above.

To execute any admin operations, you must be (1) a member of a multi-user plan, and (2) must have administrative privileges. See this help article [http://help.smartsheet.com/customer/portal/articles/520100-user-types](http://help.smartsheet.com/customer/portal/articles/520100-user-types) to learn about Smartsheet user types.

As with the other scripts in this series of samples, to run the script you will first need to [generate a Smartsheet Access Token](http://www.smartsheet.com/developers/api-documentation#h.5osh0dl59e5m) and insert it into the script:

	// Insert your Smartsheet API Token here
    $accessToken = " ";

You'll also need to set the email address for two users. Both Gmail and Hotmail offer a useful "plus addressing" feature making it possible to create multiple email addresses that are all associated with the same account. Feel free to use this method to test the script with your own Gmail or Hotmail address, or replace the default values with the actual email addresses associated with the users you want to add to your Team/Enterprise account.

	$user1Email = "api-user+1@gmail.com";
    $user2Email = "api-user+2@gmail.com";
  
When the script runs, You should receive two emails at api-user@gmail.com inviting User A and User B to join your Team account. Accept the invitation emails before continuing - this is a required step; without it, the invited users will not become associated with your Team account based on Smartsheet's opt-in model.

Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>Admin.php</b> script.

The goal of this walkthrough to help you understand how to take advantage of the Smartsheet API administrative features.

Let's add a coulpe of users to get started. 

	[…]
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
	[…]

You should receive an email at api-user+1@gmail.com inviting User One to joint your team account.  Accept the invitation - as mentioned above, this is a required step, without it the invited user will not become a member of your team account.

After you accept the invitations sent in the emails, you'll fetch the list of all your team account members - including the ones you just added:

	$usersURL = $baseURL ."/users";
	[…]
	$curlSession = curl_init($usersURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);
    $listUsersResponse = curl_exec($curlSession);

Next, let's create sheets on behalf of our newly added users.  This operation takes advantage of the "Assume-User" feature which allows team account administrators to execute commands on behalf of other users.  There are numerous business scenarios (create, update, etc.) when this is extremely useful, but it should be used with care.  Make sure that the email address of the user whose identity we are assuming is URL-encoded (in this case, using urlencode()):

	[…]
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
	[…]
	
Now fetch the list of all sheets in your team account, regardless of whether you have been shared to them.  This operation is only available to account administrators:
	
	$usersURL = $baseURL ."/users";    
	$usersSheetsURL = $usersURL ."/sheets";
	[…]
	$curlSession = curl_init($usersSheetsURL);
    $headers = array(
        "Authorization: Bearer ". $accessToken,
        "Content-Type: application/json"
    );

    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $sheetListResponse = curl_exec($curlSession);

	
If you render the result, you will see that although you executed the commands, the sheets are owned by the users on whose behalf the sheets were created.

Finally, remove a user from your team account.  You can optionally choose to (1) have the user's own sheets be transferred to someone else in your team account, and (2) remove the user from any previously shared documents:

	[…]
	$userURL = $baseURL ."/user/{{USERID}}";

	// Delete user1 and transfer sheets to user2
    $userURL = str_replace('{{USERID}}', $user1Obj->result->id, $userURL);
    $userURL.= "?transferTo=". $user2Obj->result->id;

    $curlSession = curl_init($userURL);
    curl_setopt($curlSession, CURLOPT_HTTPHEADER, $headers);   
    curl_setopt($curlSession, CURLOPT_CUSTOMREQUEST, "DELETE");   
    curl_setopt($curlSession, CURLOPT_RETURNTRANSFER, TRUE);

    $deleteResponse = curl_exec($curlSession);
	[…]
	
If you refresh the list of users, you should see that the one you just removed is gone.  If you refresh the list of sheets, you should now see that the sheet(s) previously owned by the removed user are now owned by the one to whom you transferred the sheet(s).
	
Congratulations!  You just completed your fourth Smartsheet API PHP walkthrough.  We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
