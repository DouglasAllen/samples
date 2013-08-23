Managing Users & Sheets on Team/Enterprise Accounts (C#)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b> and <b>Attachments</b> scripts for a hands-on introduction to the Smartsheet API. The fourth in the series, this semi-interactive script walks you through the Smartsheet API administrative calls and capabilities by managing team members (creating, updating and deleting), executing operations on behalf of other team members, and listing all sheets in a team account.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

.NET Framework
---
This script was built on .NET Framework 4.5. To set the target framework of the project in VS Express 2012, click Project → Console Application Properties → Application, and make a selection from the Target Framework drop-down list. Make sure it is not set to use the Client Profile.

References and Using Directives
---
In the Smartsheet API, request body data is expected to be in JSON, and the response body data is returned as JSON. This program uses the built-in JavaScriptSerializer class to serialize and deserialize JSON. Third-party libraries are available for this, but the built-in option is rather straight forward and easy to work with.

In order to use the JavaScriptSerializer class, add a reference to the program. In VS Express 2012, click Project → Add Reference → Framework. Select System.Web.Extensions to use JavaScriptSerializer in the application.

The following namespaces are used in the program to save time when typing out methods:

System.IO - for the [StreamReader](http://msdn.microsoft.com/en-us/library/system.io.streamreader.aspx) and [StreamWriter](http://msdn.microsoft.com/en-us/library/system.io.streamwriter.aspx) classes

System.Net - for the [WebRequest](http://msdn.microsoft.com/en-us/library/system.net.webrequest.aspx) class

System.Web.Script.Serialization - for the [JavaScriptSerializer](http://msdn.microsoft.com/en-us/library/system.web.script.serialization.javascriptserializer.aspx) class

Usage
---
Smartsheet has several subscription options, or plans, ranging from Basic to Enterprise - see the [Smartsheet pricing page](http://smartsheet.com/pricing) for more information. Administrative features are only available in multi-user plans (Team and Enterprise).

To execute any admin operations, you must have (1) a multi-user plan, and (2) System Admin privileges to that account. See this help article [http://help.smartsheet.com/customer/portal/articles/520100-user-types](http://help.smartsheet.com/customer/portal/articles/520100-user-types) to learn about Smartsheet user types.

While logged in as a SysAdmin of a Team or Enterprise account, generate a Smartsheet API access token and insert it into the Smartsheet Request class at the bottom of the script:

        string token = "insert token here";


You'll also notice that the email addresses passed to the User class contain default values. Both Gmail and Hotmail offer a useful "plus addressing" feature making it possible to create multiple email addresses that are all associated with the same account. Feel free to use this method to test the script with your own Gmail or Hotmail address, or replace the default values with the actual email addresses associated with the users you want to add to your Team/Enterprise account.

            User userA = new User { firstName = "User", lastName = "A", email = "youremail+1@gmail.com", admin = true, licensedSheetCreator = true };
            User userB = new User { firstName = "User", lastName = "B", email = "youremail+2@gmail.com", admin = true, licensedSheetCreator = true };


When the script runs, You should receive two emails at youremail@gmail.com inviting User A and User B to join your Team account. Accept the invitation emails before continuing - this is a required step; without it, the invited users will not become associated with your Team account based on Smartsheet's opt-in model.


Code
---
This walkthrough highlights only some parts of the code. For the full code, please see the complete <b>Admin.cs</b> script.

The goal of this walkthrough is to describe how to take advantage of the Smartsheet API administrative features.

When the script first runs, it will pause so that you can accept the invitation emails before continuing (as described above). Press any key when this step is complete.

            SmartsheetRequest addUser = new SmartsheetRequest { method = "POST", callURL = "/users?sendEmail=true", contentType = "application/json" };
            addUser.MakeRequest(js.Serialize(userA));
            addUser.MakeRequest(js.Serialize(userB));

            Console.Write("Your invitations have been sent. Wait for users to accept the invitation to the team via email.\n Press any key to continue when this is complete\n");
            Console.ReadKey();

Fetch the list of all members on your Team/Enterprise account - including the ones you just added:

            SmartsheetRequest listOrgUsers = new SmartsheetRequest { method = "GET", callURL = "/users", contentType = "application/json" };
            var listUsersResponse = listOrgUsers.MakeRequest("null");

Next, let's create sheets on behalf of our newly added users. This operation takes advantage of the "Assume-User" feature which allows SysAdmins on Team/Enterprise accounts to execute commands on behalf of other licensed users. There are numerous business scenarios (create, update, etc.) when this is extremely useful, but it should be used with care.  Make sure that the email address of the user whose identity we are assuming is URL-encoded (in this case, using Uri.EscapeDataString):

            string userASheet = "{\"name\":\"User A's Sheet\",\"columns\":[{\"title\":\"First Column\",\"primary\":true, \"type\":\"TEXT_NUMBER\"}]}]}";
            SmartsheetRequest createSheetAsUserA = new SmartsheetRequest { method = "POST", callURL = "/sheets", contentType = "application/json", assumeUser = Uri.EscapeDataString(userA.email) };
            createSheetAsUserA.MakeRequest(userASheet);
	
Now fetch the list of all sheets in your team account, including the ones we just created, regardless of whether you have been shared to them. This operation is only available to System Administrators:

            SmartsheetRequest listOrgSheets = new SmartsheetRequest { method = "GET", callURL = "/users/sheets", contentType = "application/json" };
            var orgSheets = listOrgSheets.MakeRequest("null");
	
If you render the result, you will see that although you executed the commands, the sheets are owned by the users on whose behalf the sheets were created.

Finally, remove a user from your team account. You can optionally choose to (1) have the user's owned sheets transferred to another licensed user on the Team/Enterprise account, and (2) remove the user from sharing all items owned by others on the Team/Enterprise account:

            string userToDelete = userA.id;
            SmartsheetRequest deleteUser = new SmartsheetRequest { method = "DELETE", callURL = "/user/" + userToDelete + "?transferTo=" + userB.id + "&removeFromSharing=true", contentType = "application/json" };
            var deleteUserResponse = deleteUser.MakeRequest("null");

If you refresh the list of users, you should see that the one you just removed is gone. If you refresh the list of sheets, you should now see that the sheet(s) previously owned by the removed user are now owned by the one to whom you transferred the sheet(s).
	
Congratulations!  You just completed your fourth Smartsheet API C# walkthrough. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
