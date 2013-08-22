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
using System.Web;
using System.Web.Mvc;
using DevDefined.OAuth.Consumer;
using DevDefined.OAuth.Framework;
using System.Security.Cryptography;
using System.Net;
using System.Web.Script.Serialization;
using System.IO;

namespace HelloSmartsheet.Controllers
{
    public class HelloSmartsheetController : Controller
    {
        //Launched when user clicks Get Sheets from Home view
        public ActionResult Start()
        {
            try
            {
                //From DevDefined library
                //Makes initial GET request to Smartsheet to display the authentication screen
                OAuthSession session = BuildSession();

                RequestDescription requestTokenRequest = session
                    .BuildRequestTokenContext("GET")
                    .GetRequestDescription();
                ViewBag.logout = false;

                return new RedirectResult(requestTokenRequest.Url.OriginalString);
            }

            catch (WebException e)
            {
                //Stores the error message to the ViewBag so it can be displayed in the Failure view
                ViewBag.error = e.Message;
                return View("Failure");
            }
        }

        //Launched when authentication to the app is successful
        //Code, expires_in, and state are all returned as query strings in the URL
        public ActionResult Index(string code, string expires_in, string state)
        {
            OAuthSession session = BuildSession();

            try
            {
                //Creates SHA256 hash of the AppSecret concatenated with the code
                string toHash = Session["appSecret"].ToString() + "|" + code;
                byte[] bytes = System.Text.Encoding.UTF8.GetBytes(toHash);
                SHA256Managed hashstring = new SHA256Managed();
                byte[] hash = hashstring.ComputeHash(bytes);
                string hashString = string.Empty;
                foreach (byte x in hash)
                {
                    hashString += String.Format("{0:x2}", x);
                }

                //POST request calls the authorization URL, exchanges hashstring for an access token
                SmartsheetRequest getToken = new SmartsheetRequest { method = "POST", callURL = "/token?grant_type=authorization_code&code=" + code + "&client_id=" + Session["clientId"] + "&redirect_uri=" + Session["redirectUri"] + "&hash=" + hashString, contentType = "application/x-www-form-urlencoded" };
                var getTokenResponse = getToken.MakeRequest();
                Session["accessToken"] = getTokenResponse["access_token"].ToString();
                
                //The resreshToken variable isn't used in this example, but can be used to automatically refresh a token when it expires
                Session["refreshToken"] = getTokenResponse["refresh_token"].ToString();

                //Call /user/me to get information about the user's account based on the access token
                SmartsheetRequest getUser = new SmartsheetRequest { method = "GET", callURL = "/user/me", contentType = "application/json", token = Session["accessToken"].ToString() };
                var getUserResponse = getUser.MakeRequest();
                ViewBag.email = getUserResponse["email"].ToString();
                ViewBag.fName = getUserResponse["firstName"].ToString();
                ViewBag.lName = getUserResponse["lastName"].ToString();

                //Get a list of the user's sheets, save the response to the ViewBag so it can be displayed in the View
                SmartsheetRequest getSheets = new SmartsheetRequest { method = "GET", callURL = "/sheets", contentType = "application/json", token = Session["accessToken"].ToString() };

                ViewBag.jsonResponse = getSheets.MakeRequest();

                return View();
            }

            catch (WebException e)
            {
                ViewBag.error = e.Message;
                return View("Failure");
            }
        }

        public ActionResult Logout()
        {
            Session.Clear();
            Session.Abandon();
            ViewBag.logout = true;
            return View("../Home/Index");
        }

        //Used when building the session
        string GetCallbackUrl()
        {
            string url = Request.Url.ToString();

            url = url.Substring(0, url.LastIndexOf("/")) + "/Callback";

            return url;
        }

        //From DevDefined library
        //Builds session, stores client information in memory
        //Replace session variables with those associated with your own 3rd party app
        OAuthSession BuildSession()
        {
            Session["clientId"] = "insert your app's clientID";
            Session["appSecret"] = "insert your app's Secret";
            Session["redirectUri"] = "insert your redirect Uri";
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
    }

    //Handles all requests made to Smartsheet from the app
    class SmartsheetRequest
    {
        public string method { get; set; }
        public string callURL { get; set; }
        public string contentType { get; set; }
        public string token { get; set; }

        JavaScriptSerializer js = new JavaScriptSerializer();

        string baseURL = "https://api.smartsheet.com/1.1";

        public dynamic MakeRequest()
        {
            WebRequest request = WebRequest.Create(baseURL + callURL);
            request.ContentType = contentType;
            request.Method = method;
            request.Headers.Add("Authorization: Bearer " + token);

            Stream responseStream = request.GetResponse().GetResponseStream();
            StreamReader readStream = new StreamReader(responseStream);
            var jsonResponse = js.Deserialize<dynamic>(readStream.ReadToEnd());

            return jsonResponse;
        }
    }
}