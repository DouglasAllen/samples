require 'httparty'
require 'active_support/core_ext/hash/deep_merge'
require 'json'

class Smartsheet
  include HTTParty
  base_uri @@ss_uri = 'https://api.smartsheet.com/1.1'
  
  def initialize(token)
    @auth_options = {headers: {"Authorization" => 'Bearer ' + token}}
  end

  def self.return_ss_uri
    @@ss_uri
  end

  def request(method, uri, options={})
    options.deep_merge!(@auth_options)
    response = self.class.send(method, uri, options)
    json = JSON.parse(response.body)
  end
end
