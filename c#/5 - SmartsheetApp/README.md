 Third-party app (C#/.Net)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b>, <b>SmartsheetAttachments</b> and <b>AdministrativeFeatures</b> scripts for a hands-on introduction to the Smartsheet API.  The fifth in the series, this sample third-party .Net app demonstrates how you can integrate with the Smartsheet API and obtain a user's access token using OAuth2.


What's not addressed in this walkthrough:

* Handling of the Smartsheet API rate limit
* Smartsheet refresh token management

Register your App with Smartsheet
---
Third party app registration and management is available via the Developer Tools within the Smartsheet application. 



1. First [register as a Smartsheet developer](http://smartsheet.com/developers/register). Registering enables you to either add Developer Tools to an existing Smartsheet account or create a new Smartsheet developer account at no cost.


2. An email will be sent to your address to confirm the registration. 

3. After confirming, [login](http://app.smartsheet.com) to Smartsheet with the registered email address and open the Developer Tools menu by clicking Account (upper left corner) → Developer Tools.

4. Fill out your developer profile to see an option to **Create New App**. Click this to set up the app name, description, and support contact information, all of which are publicly accessible. Alse set up the redirect URL for your app - it is important that your redirect URL matches the URL that your app is expecting to handle. In this program, the redirect URL is locally hosted for testing purposes (http://localhost:xxxxx/HelloSmartsheet).

5. Click **Save** to generate your App client id and App secret which will both be used in the oAuth flow. You can access these in the future if needed from the Developer Tools by clicking on **View/Edit** next to the name of your app.

More detailed instructions on third party app registration are available in our [API documentation](http://www.smartsheet.com/developers/api-documentation#h.opcwlo3avvxk).

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

Web Framework & Libraries
---
This blank MVC 4 project was built on .NET Framework 4.5 in Visual Studio Express 2012 for Web. To create this type of project in VS for Web, click  New Project → Templates → Visual C# → Web → ASP.NET MVC 4 Web Application. Name the project, and select an Empty Template.

The [DevDefined.OAuth](https://github.com/bittercoder/DevDefined.OAuth) project is a library for creating both OAuth consumers and providers on the .Net Framework, and is used in this project. Other libraries are available for this as well, so feel free to use what is comfortable and makes the most sense for your project.

Install [NuGet Package Manager](http://visualstudiogallery.msdn.microsoft.com/27077b70-9dad-4c64-adcf-c7cf6bc9970c) to quickly and easily import this library. You can then simply type the following into your Package Manager Console to work with the DevDefined library:

**Install-Package DevDefined.OAuth**

Using Directives
---

The following namespaces are used in the HelloSmartsheet controller:

DevDefined.OAuth.Consumer and 
DevDefined.OAuth.Framework - to access the DevDefined library

System.Security.Cryptography - for the [SHA256Managed](http://msdn.microsoft.com/en-us/library/system.security.cryptography.sha256managed.aspx) class, used to encode a hash string during authentication

System.IO - for the [StreamReader](http://msdn.microsoft.com/en-us/library/system.io.streamreader.aspx) and [StreamWriter](http://msdn.microsoft.com/en-us/library/system.io.streamwriter.aspx) classes

System.Net - for the [WebRequest](http://msdn.microsoft.com/en-us/library/system.net.webrequest.aspx) class

System.Web.Script.Serialization - for the [JavaScriptSerialize](http://msdn.microsoft.com/en-us/library/system.web.script.serialization.javascriptserializer.aspx)r class

Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete app.

The goal of this walkthrough to help you understand how to use oAuth 2.0 to authenticate with Smartsheet. Let's first discuss the various classes in the app:


**1. RouteConfig.cs:** Instructs the program to launch the HomeController when run.

**2. HomeController.cs**: Returns the Home/Index.cshtml View, which simply offers a clickable link that launches the Smartsheet authentication screen.

**3. HelloSmartsheetController.cs**: This is the redirect URL for this application, and the bulk of the work is done here. The controller is associated with 2 views: Index.cshtml which displays the list of sheets on successful authentication, and Failure.cshtml which is returned when any error is encountered.

In the HelloSmartsheet controller, use the BuildSession method from the DevDefined library to store the clientId, appSecret, and redirect uri that were generated when you registered your app in Smartsheet. 

Set the scope based on the access levels described in the [API documentation](http://www.smartsheet.com/developers/api-documentation#h.89hb3ivv7eum). The scope won't override the permission levels that are set up in Smartsheet. You can also optionally define a "state" variable which is returned in the query string on successful authentication.

The session is built each time a request is made to Smartsheet, and cleared on logout, so you can continually access the variables stored here.


        OAuthSession BuildSession()
        {
            Session["clientId"] = "insert your clientID";
            Session["appSecret"] = "insert your appSecret";
            Session["redirectUri"] = "http://localhost:xxxx/HelloSmartsheet";
            Session["scope"] = "READ_SHEETS,WRITE_SHEETS";
            //State is an optional variable
            Session["state"] = "MY_STATE";

            var consumerContext = new OAuthConsumerContext
            {
                SignatureMethod = SignatureMethod.HmacSha1,
                ConsumerKey = Session["clientId"].ToString(),
                ConsumerSecret = Session["appSecret"].ToString(),
                UseHeaderForOAuthParameters = false
            };

            var session = new OAuthSession(consumerContext,
                requestTokenUrl: "https://www.smartsheet.com/b/authorize?response_type=code&client_id=" + Session["clientId"] + "&redirect_uri=" + Session["redirectUri"] + "&scope=" + Session["scope"] + "&state=" + Session["state"],
                userAuthorizeUrl: "https://www.smartsheet.com/b/authorize", /* not used in this flow */
                accessTokenUrl: "https://api.smartsheet.com/1.1/token")
            {
                CallbackUri = new Uri(GetCallbackUrl())
            };

            return session;
        }



The ActionResult Index method is run when the user has successfully authenticated themselves to Smartsheet. The query string that is returned on successful authentication contains parameters for a code, the expiration time of the code, and the optional "state" that was passed in. 

To capture the access token, concatenate the code with a pipe and the appSecret that was declared in the session. Run this through the SHA256 hash, which will be included in a POST request to Smartsheet. The POST request exchanges a successful hash with the access token.

    string toHash = Session["appSecret"].ToString() + "|" + code;
    byte[] bytes = System.Text.Encoding.UTF8.GetBytes(toHash);
    SHA256Managed hashstring = new SHA256Managed();
    byte[] hash = hashstring.ComputeHash(bytes);
    string hashString = string.Empty;
    foreach (byte x in hash)
    {
    hashString += String.Format("{0:x2}", x);
    }
    
    SmartsheetRequest getToken = new SmartsheetRequest { method = "POST", callURL = "/token?grant_type=authorization_code&code=" + code + "&client_id=" + Session["clientId"] + "&redirect_uri=" + Session["redirectUri"] + "&hash=" + hashString, contentType = "application/x-www-form-urlencoded" };
    var getTokenResponse = getToken.MakeRequest();
    Session["accessToken"] = getTokenResponse["access_token"].ToString();

The access token is stored in the session.

Now that you have the access token, you can use it to make calls to the Smartsheet API as that user. In this project, we make a GET request /user/me to obtain the user's name and email address, and then a separate GET request to /sheets to get a list of the user's sheets.

These variables are stored to the ViewBag so they can be displayed in the HelloSmartsheet/Index.cshtml view like so:

    <table>
    <tr><th>Sheet Name</th>
        <th>Sheet ID</th>
        <th>Access Level</th>
    </tr>
    @if(ViewBag.jsonResponse.Length > 0)
    {
       for (int i = 0; i < ViewBag.jsonResponse.Length;i++)
    {
       <tr><td>@(i+1)
    .&nbsp;
    @ViewBag.jsonResponse[i]["name"]
       &nbsp;
       </td>
       <td>@ViewBag.jsonResponse[i]["id"]
       &nbsp;
       </td>
       <td>@ViewBag.jsonResponse[i]["accessLevel"]</td>
    </tr>
    }
    }

Congratulations! You just completed your fifth Smartsheet API C# walkthrough. We encourage you to play with the app, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team.