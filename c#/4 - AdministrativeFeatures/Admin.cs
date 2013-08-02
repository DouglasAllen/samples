//  Copyright 2013 Smartsheet, Inc.

//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at

//       http://www.apache.org/licenses/LICENSE-2.0

//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.


using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;
using System.Web.Script.Serialization;

namespace ManageUsers
{
    class ManageUsers
    {
        static void Main(string[] args)
        {
            JavaScriptSerializer js = new JavaScriptSerializer();

            //Instantiates a couple of user objects, with parameters that can be referenced later in the script
            //Replace the default string values with your own
            User userA = new User { firstName = "User", lastName = "A", email = "youremail+1@gmail.com", admin = true, licensedSheetCreator = true };
            User userB = new User { firstName = "User", lastName = "B", email = "youremail+2@gmail.com", admin = true, licensedSheetCreator = true };

            //Add the users to the Team account based on their email address
            SmartsheetRequest addUser = new SmartsheetRequest { method = "POST", callURL = "/users?sendEmail=true", contentType = "application/json" };
            addUser.MakeRequest(js.Serialize(userA));
            addUser.MakeRequest(js.Serialize(userB));

            //This sends a confirmation email to the addresses 
            //The users need to click the accept button in their emails to finalize the action, enabling you to make "assume user" requests to Smartsheet
            Console.Write("Your invitations have been sent. Wait for users to accept the invitation to the team via email.\n Press any key to continue when this is complete\n");
            Console.ReadKey();

            //After the invitations have been accepted, list the org users to confirm that this has been completed
            SmartsheetRequest listOrgUsers = new SmartsheetRequest { method = "GET", callURL = "/users", contentType = "application/json" };
            var listUsersResponse = listOrgUsers.MakeRequest("null");

            Console.Write("A full list of users on my Team account are displayed below:\n");
            for (int i = 0; i < listUsersResponse.Length; i++)
            {
                Console.Write((i + 1) + ". " + listUsersResponse[i]["email"] + "\n");

                //Store the user ID to the user objects, so you can easily refer to them later in the code
                if (listUsersResponse[i]["email"] == userA.email)
                {
                    userA.id = listUsersResponse[i]["id"].ToString();
                }
                if (listUsersResponse[i]["email"] == userB.email)
                {
                    userB.id = listUsersResponse[i]["id"].ToString();
                }
            }

            //Create a new sheet as yourself
            string mySheet = "{\"name\":\"My Sheet\",\"columns\":[{\"title\":\"First Column\",\"primary\":true, \"type\":\"TEXT_NUMBER\"}]}]}";
            SmartsheetRequest createSheet = new SmartsheetRequest { method = "POST", callURL = "/sheets", contentType = "application/json" };
            createSheet.MakeRequest(mySheet);

            //Pass "assume user" to the class to create a new sheet as User A
            string userASheet = "{\"name\":\"User A's Sheet\",\"columns\":[{\"title\":\"First Column\",\"primary\":true, \"type\":\"TEXT_NUMBER\"}]}]}";
            //Uri.EscapeDateString to URi-decode the email address
            SmartsheetRequest createSheetAsUserA = new SmartsheetRequest { method = "POST", callURL = "/sheets", contentType = "application/json", assumeUser = Uri.EscapeDataString(userA.email) };
            createSheetAsUserA.MakeRequest(userASheet);

            //Pass "assume user" with different parameters to create a new sheet as User B
            string userBSheet = "{\"name\":\"User B's Sheet\",\"columns\":[{\"title\":\"First Column\",\"primary\":true, \"type\":\"TEXT_NUMBER\"}]}]}";
            //Uri.EscapeDateString to URi-encode the email address
            SmartsheetRequest createSheetAsUserB = new SmartsheetRequest { method = "POST", callURL = "/sheets", contentType = "application/json", assumeUser = Uri.EscapeDataString(userB.email) };
            createSheetAsUserB.MakeRequest(userBSheet);

            Console.Write("\nNew Sheets have been created...\n");

            //List all sheets owned by users in the org
            //Confirms that the "assume user" requests were successful
            SmartsheetRequest listOrgSheets = new SmartsheetRequest { method = "GET", callURL = "/users/sheets", contentType = "application/json" };
            var orgSheets = listOrgSheets.MakeRequest("null");

            Console.Write("\nA full list of all sheets owned by users on my Team account are displayed below: \n");
            for (int i = 0; i < orgSheets.Length; i++)
            {
                Console.Write((i + 1) + ". " + orgSheets[i]["name"] + ", owned by " + orgSheets[i]["owner"] + "\n");
            }

            //To delete a user, access their user ID
            //Optionally transfer their owned sheets to another user on the account - in this case, User B
            //Optionally remove the user's access from sharing all sheets owned by users on the Team/Enterprise account
            string userToDelete = userA.id;
            SmartsheetRequest deleteUser = new SmartsheetRequest { method = "DELETE", callURL = "/user/" + userToDelete + "?transferTo=" + userB.id + "&removeFromSharing=true", contentType = "application/json" };
            var deleteUserResponse = deleteUser.MakeRequest("null");

            Console.Write("\nUser with ID " + userToDelete + " has been deleted.\n");
            Console.Read();
        }
    }

    class User
    {
        public string firstName { get; set; }
        public string lastName { get; set; }
        public string email { get; set; }
        public bool admin { get; set; }
        public bool licensedSheetCreator { get; set; }
        public string id { get; set; }
    }

    class SmartsheetRequest
    {
        public string assumeUser { get; set; }
        public string method { get; set; }
        public string callURL { get; set; }
        public string contentType { get; set; }

        JavaScriptSerializer js = new JavaScriptSerializer();

        //Replace the text with your Smartsheet API token, generated in-app
        string token = "insert token here";
        string baseURL = "https://api.smartsheet.com/1.1";

        public dynamic MakeRequest(string json)
        {
            WebRequest request = WebRequest.Create(baseURL + callURL);
            request.ContentType = contentType;
            request.Method = method;
            request.Headers.Add("Authorization: Bearer " + token);

            //Only add the assumeUser header if the argument was passed
            if (String.IsNullOrEmpty(assumeUser) == false)
            {
                request.Headers.Add("Assume-User: " + assumeUser);
            }

            if (json != "null")
            {
                using (var streamWriter = new StreamWriter(request.GetRequestStream()))
                {
                    streamWriter.Write(json);
                }
            }
            Stream responseStream = request.GetResponse().GetResponseStream();
            StreamReader readStream = new StreamReader(responseStream);
            var jsonResponse = js.Deserialize<dynamic>(readStream.ReadToEnd());

            return jsonResponse;
        }
    }
}
