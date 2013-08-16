class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  require 'smartsheet.rb'

  helper_method :logged_in?

  protected

    def logged_in?
      session[:smartsheet_token] ? true : false
    end

end
