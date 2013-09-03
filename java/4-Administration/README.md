Managing Users & Sheets on Team/Enterprise Accounts (Java)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b> and <b>Attachments</b> scripts for a hands-on introduction to the Smartsheet API. The fourth in the series, this semi-interactive script walks you through the Smartsheet API administrative calls and capabilities by managing team members (creating, updating and deleting), executing operations on behalf of other team members, and listing all sheets in a team account.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Dependencies
---
This script has been tested with Java 7.
You must also include the Jackson JSON parser jars, found here: [http://wiki.fasterxml.com/JacksonHome](http://wiki.fasterxml.com/). This has been tested with version 2.2.0.

To run, cd to the folder where AdminExample.java is located and then run the following commands:
		
		javac -classpath [folder with Jackson jars] AdminExample.java
		java -classpath [folder with Jackson jars];. AdminExample
	
Usage
---
Smartsheet has several subscription options, or plans, ranging from Basic to Enterprise - see the [Smartsheet pricing page](http://smartsheet.com/pricing) for more information. Administrative features are only available in multi-user plans (Team and Enterprise).

To execute any admin operations, you must have (1) a multi-user plan, and (2) System Admin privileges to that account. See this help article [http://help.smartsheet.com/customer/portal/articles/520100-user-types](http://help.smartsheet.com/customer/portal/articles/520100-user-types) to learn about Smartsheet user types.

While logged in as a SysAdmin of a Team or Enterprise account, [generate a Smartsheet API Access Token](http://www.smartsheet.com/developers/api-documentation#h.5osh0dl59e5m) and insert it into the Smartsheet Request class at the top of the script:

        String accessToken = "";


You'll also need to set the email address for two users. Both Gmail and Hotmail offer a useful "plus addressing" feature making it possible to create multiple email addresses that are all associated with the same account. Feel free to use this method to test the script with your own Gmail or Hotmail address, or replace the default values with the actual email addresses associated with the users you want to add to your Team/Enterprise account.

            String user1Email = "youremail+1@gmail.com"; 
            String user2Email = "youremail+2@gmail.com";

When the script runs, You should receive two emails at youremail@gmail.com inviting User A and User B to join your Team account. Accept the invitation emails before continuing - this is a required step; without it, the invited users will not become associated with your Team account based on Smartsheet's opt-in model.


Code
---
This walkthrough highlights only some parts of the code. For the full code, please see the complete <b>AdminExample.java</b> script.

The goal of this walkthrough is to describe how to take advantage of the Smartsheet API administrative features.

When the script first runs, it will add both users to your account and then pause so that you can accept the invitation emails before continuing (as described above). Press Enter when you have accepted the invitation.

Next, it will fetch the list of all members on your Team/Enterprise account and print the user's email.

Next, it will create sheets on behalf of our newly added users. This operation takes advantage of the "Assume-User" feature which allows SysAdmins on Team/Enterprise accounts to execute commands on behalf of other licensed users. There are numerous business scenarios (create, update, etc.) when this is extremely useful, but it should be used with care.  Make sure that the email address of the user whose identity we are assuming is URL-encoded:

            connection.addRequestProperty("Assume-User", URLEncoder.encode(user1Email, "UTF-8")); 
	
Now, it will fetch the list of all sheets in your team account, including the ones we just created, regardless of whether you have been shared to them. This operation is only available to System Administrators:

            connection = (HttpURLConnection) new URL(USERS_SHEETS_URL).openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + accessToken);
            connection.addRequestProperty("Content-Type", "application/json");
            List<Sheet> allSheets = mapper.readValue(connection.getInputStream(), new TypeReference<List<Sheet>>() {});
	
When the results print, you will see that although you executed the commands, the sheets are owned by the users on whose behalf the sheets were created.

Finally, remove a user from your team account. You can optionally choose to (1) have the user's owned sheets transferred to another licensed user on the Team/Enterprise account, and (2) remove the user from sharing all items owned by others on the Team/Enterprise account:

            connection = (HttpURLConnection) new URL(USER_URL.replace(ID, newUser1Result.getResult().getId() + "") + "?transferTo=" + newUser2Result.getResult().getId()).openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + accessToken);
            connection.addRequestProperty("Assume-User", URLEncoder.encode(user2Email, "UTF-8")); 
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("DELETE");
            Result<Object> resultObject = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Object>>() {});

If you refresh the list of users, you should see that the one you just removed is gone. If you refresh the list of sheets, you should now see that the sheet(s) previously owned by the removed user are now owned by the one to whom you transferred the sheet(s).
	
Congratulations!  You just completed your fourth Smartsheet API Java walkthrough. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
