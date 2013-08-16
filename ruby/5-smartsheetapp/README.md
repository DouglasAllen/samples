Third-party app (Ruby/Rails)
===
See our <b>Hellosmartsheet</b>, <b>SheetStructure</b>, <b>Attachments</b> and <b>Admin</b> scripts for a hands-on introduction to the Smartsheet API.  The fifth in the series, this sample third-party RoR app shows how to use OAuth 2.0 to authenticate with Smartsheet and access data in a Smartsheet account on behalf of a user.

##Smartsheet API
Familiarize yourself with the Smartsheet API. For information on the Smartsheet APi, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

The Smartsheet API documentation has a detailed section on third-party applications which takes you step by step trough the Smartsheet OAuth 2.0 flow.  Please review it and familiarize yourself with the flow prior to implementing this example app.

## Notes and Caveats
* This example app has been tested with Rails 4.0 only.
* This example app does not make use of persistent storage.

What's not addressed in this walkthrough:

* Devise integration.
* Handling of the Smartsheet API rate limit.
* Smartsheet refresh token management.






## Code
This walkthrough highlights only some parts of the code.  For the full code, please see the complete app.

The goal of this walkthrough to help you understand how to use OAuth 2.0 to authenticate with Smartsheet.  Let's get started.

Create a new RoR app:

	rails new APP_NAME
	
Create "home" controller and its default view so we have a landing page:

	rails g controller home index
	
Edit `app/views/home/index.html.erb` to customize your landing page.

Edit `config/routes.rb` to set the default root to the landing page:

	root to: "home#index"

Now, let's create a custom class to handle our connection with the Smartsheet API.  This adds a level of abstraction, simplifies maintenance and troubleshooting.  The class will live in the `smartsheet.rb` file in the application's `lib` directory.  The class will use the `HTTParty` gem to manage HTTP requests to the Smartsheet API.  Edit the `Gemfile` to include the `HTTParty` gem:

	gem "httparty"
	
Edit the `Gemfile` to require three `omniauth` gems.  The `omniauth-smartsheet` gem (source code available on [Github](https://github.com/smartsheet-platform/omniauth-smartsheet)) - implements the Omniauth strategy for Smartsheet:

	gem 'omniauth'
	gem 'omniauth-oauth2'
	gem 'omniauth-smartsheet'
	
Please note that there is one important difference in the way Smartsheet manages the OAuth 2.0 flow.  To add another level of security, Smartsheet requires that third party apps do not send the application secret over the wire in clear text.  Instead, third party apps are required to provide a SHA-256 hash of the app secret concatenated with a pipe and your access code.  The `omniauth-smartsheet` gem will transparently take care of this for you.

[Register](http://smartsheet.com/developers/register) for Smartsheet Developer Tools so you can create third-party apps.  Once you have access to Developer Tools, register a new application and make note of the generated app ID and secret.

Create `config/initializers/omniauth.rb` to instruct OmniAuth to use the Smartsheet strategy:

	Rails.application.config.middleware.use OmniAuth::Builder do
        provider :smartsheet, 'APP_CLIENT_ID', nil, :smartsheet_secret => 'APP_SECRET', :scope => 'READ_SHEETS'
	end
        
Replace APP_CLIENT_ID and APP_SECRET with the values generated when your registered your app with Smartsheet.  You may need to customize the scope for your app.  See the [Smartsheet API 
documentation](http://smartsheet.com/developers) for details. 

Now, make sure the `Smartsheet` class is always loaded.  Edit `app/controllers/application_controller.rb` to require `smartsheet.rb` and to add a convenience `logged_in` method:

* `require smartsheet.rb`
* define the `logged_in?` method and expose it via `helper_method`

It's time to add some basic user session management logic.  Create `sessions` controller:

	rails g controller sessions
	
Edit `config/routes.rb` to create the `session` resource:

	resource :session
	
Edit `app/views/layouts/application.html.erb` to add a global page header and login/logout menu - to add some basic navigation and session management UI.

Back to `config/routes` to map the OmniAuth callback to the session management controller:

	match '/auth/:provider/callback' => 'sessions#create', via: [:get, :post]

Create 'sheets' controller and default view so we can display some basic information about user's Smartsheet sheets:

	rails g controller sheets index
	
Edit `sheets` controller to add `create` and `destroy` methods, and update the `index` view to show sheet count and list the top 3 sheets.

Make the final touches to `config/routes.rb` - add the `sheets` resource:

	resources :sheets


## User experience
Smartsheet users can view the list of third-party apps they approved to access their account by going to Account > Personal Settings > My Apps and Mobile Devices.  Users can revoke an app at any time.


Congratulations!  You just completed your fifth Smartsheet API Ruby walkthrough.  We encourage you to play with the app, change it around, and enhance it to get better acquainted with the Smartsheet API.  Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)

