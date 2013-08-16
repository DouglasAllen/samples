class SessionsController < ApplicationController
  def create
    token = request.env['omniauth.auth']['credentials']['token']
    email = request.env['omniauth.auth']['info']['email']

    session[:smartsheet_token] = token
    session[:smartsheet_email] = email
    redirect_to sheets_path
  end

  def destroy
    session[:smartsheet_token] = nil
    redirect_to root_path
  end
end
