class SheetsController < ApplicationController
  def index
    @sheets = connection.request('get', '/sheets')
  end


  private

    def connection 
      @connection ||= Smartsheet.new(session[:smartsheet_token])
    end
end
