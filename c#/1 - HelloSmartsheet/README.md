
**Install the IDE and set target framework**:

This program was created in Visual Studio Express 2012 for Windows desktop, which is available as a [free download](http://www.microsoft.com/visualstudio/eng/products/visual-studio-express-for-windows-desktop), but will work in any version that supports .NET Framework 4.

To get started, create a new Project → Console Application. Set the target framework of the project to .NET Framework 4 OR 4.5. In VS Express 2012, click Project → Console Application Properties → Application, and make a selection from the Target Framework drop-down list. Make sure it is not set to use the Client Profile.

**Add references to parse JSON:**

In the Smartsheet API, request body data is expected to be in JSON, and the response body data is returned as JSON. In this program we use the built-in JavaScriptSerializer class to deserialize responses from Smartsheet that are formatted as json strings, as well as serialize C# objects into json strings so we can post them to Smartsheet. 

In order to use the JavaScriptSerializer class, add a reference to the program. In VS Express 2012, click Project → Add Reference → Framework. Select the System.Web.Extensions to use JavaScriptSerializer in the application.

While other libraries are available for working with JSON, for this simple example we'll stick with what's built-in.

**Add using directives:**

The following namespaces are used in the program to save time when typing out methods:

System.IO - for the [StreamReader](http://msdn.microsoft.com/en-us/library/system.io.streamreader.aspx) and [StreamWriter](http://msdn.microsoft.com/en-us/library/system.io.streamwriter.aspx) classes

System.Net - for the [WebRequest](http://msdn.microsoft.com/en-us/library/system.net.webrequest.aspx) class

System.Web.Script.Serialization - for the [JavaScriptSerializer](http://msdn.microsoft.com/en-us/library/system.web.script.serialization.javascriptserializer.aspx) class

**Error Checking**

For simplicity sake, there is no error checking or data validation whatsoever included in this program. A simple try/catch statement is used so the application doesn't crash if the requests can't be processed. If you want to use this as a working program you're encouraged to build error checking and data validation as you see fit.

**Writing the code**

All of the code is executed in the Main() method of this Program. Use a try/catch statement to prevent the application from crashing on unsuccessful request. 

        static void Main(string[] args)
        {
            try
            {
            }
            catch (Exception e)
            {
                Console.Write("\n" + e.Message + "\n");
            }
    	}
    }
Within the Try block, store the bearer access token and Smartsheet base URL. They will both be included in the GET Request.

                Console.Write("\nEnter Smartsheet API access token\n");
                string token = Console.ReadLine();
                string baseURL = "https://api.smartsheet.com/1.1/sheet";

Build the GET request that will display a list of sheets that are available to the user based on their access token. Note the added "s" to the baseURL!

Add a Header property to the WebRequest to include the bearer access token. 

                WebRequest getRequest = WebRequest.Create(baseURL + "s");
                getRequest.ContentType = "application/json; charset=utf-8";
                getRequest.Method = "GET";
                getRequest.Headers.Add("Authorization: Bearer " + token);

Use StreamReader to read Smartsheet's json string response to your web request, and the JavaScriptSerializer class to parse the json string to a dynamic object. 

                Stream dataStream = getRequest.GetResponse().GetResponseStream();
                StreamReader objReader = new StreamReader(dataStream);

                JavaScriptSerializer js = new JavaScriptSerializer();
                var sheetList = js.Deserialize<dynamic>(objReader.ReadToEnd());


Check the length of the dynamic object to determine whether the user has any available sheets. If they don't, display an error message and enable them to try again by running the exitRestart method (created at the bottom). 

If they do, write the first 5 available sheets to the console. 

                if (sheetList.Length == 0)
                {
                    Console.Write("\nYou don't have any sheets.\nPress Esc to close or Enter to restart.");
                    Program.exitRestart();
                }
                else
                {
                    Console.WriteLine("\nTotal sheets: " + sheetList.Length + "\nShowing the first five sheets:\n ");
                    for (int i=0; i<sheetList.Length&i<5;i++)
                    {
                        Console.Write((i + 1).ToString() + ": " + sheetList[i]["name"] + "\n");
                    }
                    Console.Write("\nEnter the number of the sheet you want to share:\n");
                }

Next, offer a series of console prompts that enable the user to select which sheet to share, as well as enter the email address and the appropriate access level for the collaborator. The user input is stored in string variables.

                int id = int.Parse(Console.ReadLine()) - 1;

                Console.Write("\nEnter an email address to share " + sheetList[id]["name"] + " to:\n");
                string email = Console.ReadLine();

                Console.Write("\nChoose an access level (ADMIN, EDITOR, EDITOR_SHARE or VIEWER) for " + email + "\n");
                string accessLevel = Console.ReadLine();


Instantiate an object of the Collaborator class, which is created at the bottom. The collab object will have the user-input email address and access level strings as properties. Use JavaScriptSerializer again to serialize the object to a json string which you'll include in the POST request to Smartsheet.

                Collaborator collab = new Collaborator { email = email, accessLevel = accessLevel };
                string json = js.Serialize(collab);


Build the POST request that will be sent to Smartsheet. Add the json string to the postRequest using StreamWriter. Use StreamReader again to read Smartsheet's json string response to the web request, and store it to a dynamic object.

                WebRequest postRequest = WebRequest.Create(baseURL + "/" + sheetList[id]["id"].ToString() + "/shares?sendemail=false");
                postRequest.ContentType = "application/json; charset=utf-8";
                postRequest.Method = "POST";
                postRequest.Headers.Add("Authorization: Bearer " + token);

                using (var streamWriter = new StreamWriter(postRequest.GetRequestStream()))
                {
                    streamWriter.Write(json);
                }

                //Get response, display confirmation and share ID to user
                Stream responseStream = postRequest.GetResponse().GetResponseStream();
                StreamReader readStream = new StreamReader(responseStream);
                var jsonResponse = js.Deserialize<dynamic>(readStream.ReadToEnd());


If the request is successful, display a confirmation message, and the Share ID from the json Response. Run the exitRestart method (created at the bottom).

                Console.Write("\nSheet shared successfully! Share ID " + jsonResponse["result"]["id"] + "\nPress Esc to close or Enter to restart\n");
                Program.exitRestart();


The exitRestart method restart or exits the application depending on the user input. Run the method anytime an error message or successful response is displayed to the user so they can navigate through the program.
        
		static void exitRestart()
        {
            var key = Console.ReadKey();
            if (key.Key == ConsoleKey.Escape)
            {
                Environment.Exit(0);
            }
            else if (key.Key == ConsoleKey.Enter)
            {
                Console.Clear();
                Main();
            }
        }
Create a Collaborator class with string properties for email address and access level. An object of this class is instantiated when you create the post request to share a sheet. 

        class Collaborator
        {
            public string email { get; set; }
            public string accessLevel { get; set; }
        }

****

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
