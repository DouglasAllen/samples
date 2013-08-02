#!/usr/bin/env ruby
=begin
  Smartsheet Platform sample code
  attachments.rb (Ruby)

   Copyright 2013 Smartsheet, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
=end

# load third-party libraries and extensions
require 'httparty'
require 'active_support/core_ext/hash/deep_merge'
require 'json'
require 'cgi'

# define httparty class
class Smartsheet
  include HTTParty
  base_uri 'https://api.smartsheet.com/1.1'
 
  # initialize httparty object
  def initialize(token)
    @auth_options = {headers: {"Authorization" => 'Bearer ' + token}}
  end

  def request(method, uri, options={})
    # merge headers
    options.deep_merge!(@auth_options)

    # process response
    puts "* requesting #{method.upcase} #{uri}"
    response = self.class.send(method, uri, options)
    json = JSON.parse(response.body)

    # if response is anything other than HTTP 200, print error and quit
    if response.code.to_s !~ /^2/
      puts "* fatal error: #{json['errorCode']}: #{json['message']}"
      exit 
    end

    return json
  end
end

# Smartsheet API access token
ss_token = '38f0katut5h5ke89zhvj8hxng9'

# initializing Smartsheet connection object
ss_connection = Smartsheet.new(ss_token)

puts
puts "Starting admin.rb..."
puts

# add user #1
puts "Adding user 1..."
options = {
  headers: { 'Content-Type' => 'application/json' },
  body: {
      firstName: 'John101',
      lastName: 'Smith101',
      email: 'avioing+101@gmail.com',
      admin: false,
      licensedSheetCreator: true
  }.to_json
}
body = ss_connection.request('post', '/users', options)
user1 = body['result']
puts "User added, id: #{user1['id']}, name: #{user1['firstName']} #{user1['lastName']}."
puts
puts 'IMPORTANT: Invited user must accept invitation...'
puts

# add user #2
puts "Adding user 2..."
options = {
  headers: { 'Content-Type' => 'application/json' },
  body: {
      firstName: 'John102',
      lastName: 'Smith102',
      email: 'avioing+102@gmail.com',
      admin: false,
      licensedSheetCreator: true
  }.to_json
}
body = ss_connection.request('post', '/users', options)
user2 = body['result']
puts "User added, id: #{user2['id']}, name: #{user2['firstName']} #{user2['lastName']}."
puts
puts 'IMPORTANT: Invited user must accept invitation...'
puts

puts 'NEXT: Hit return/enter only if both invited users have accepted invitation.'
puts

enter = gets

# fetch list of users
puts "Fetching the list of all users in the organization..."
body = ss_connection.request('get', '/users') 
puts "Users total: #{body.length}"
body.each_with_index do |s, i|
  puts "User #{i+1}, id: #{s['id']}, name: #{s['firstName']} #{s['lastName']}"
end
puts

# create sheet as administrator
sheet_name = "SheetByAdmin"
puts "Creating sheet #{sheet_name}..."
options = {
  headers: { 'Content-Type' => 'application/json' },
  body: {
    name: sheet_name,
    columns: [
      { title: "Column1", type: "TEXT_NUMBER", primary: true }
    ] 
  }.to_json
}
body = ss_connection.request('post', '/sheets', options)
sheet_id = body['result']['id']
puts "Sheet #{sheet_name} created, id: #{sheet_id}."
puts

# create sheet as user 1
sheet_name = "SheetByUser1"
puts "Creating sheet #{sheet_name}..."
options = {
  headers: {
    'Content-Type' => 'application/json',
    'Assume-User' => CGI.escape(user1['email'])
  },
  body: {
    name: sheet_name,
    columns: [ { title: "Column1", type: "TEXT_NUMBER", primary: true } ] 
  }.to_json
}
body = ss_connection.request('post', '/sheets', options)
sheet_id = body['result']['id']
puts "Sheet #{sheet_name} created, id: #{sheet_id}."
puts

# create sheet as user 2
sheet_name = "SheetByUser2"
puts "Creating sheet #{sheet_name}..."
options = {
  headers: {
    'Content-Type' => 'application/json',
    'Assume-User' => CGI.escape(user2['email'])
  },
  body: {
    name: sheet_name,
    columns: [ { title: "Column1", type: "TEXT_NUMBER", primary: true } ] 
  }.to_json
}
body = ss_connection.request('post', '/sheets', options)
sheet_id = body['result']['id']
puts "Sheet #{sheet_name} created, id: #{sheet_id}."
puts

# fetch the list of sheets
puts "Fetching the list of all sheets in organization (as org admin)..."
body = ss_connection.request('get', '/users/sheets') 
puts "Sheets total: #{body.length}"
body.each_with_index do |s, i|
  puts "Sheet #{i+1}, id #{s['id']}, name: #{s['name']}, owner: #{s['owner']}"
end
puts

# delete user
puts "Deleting user1 and transferring his/her sheets to user2..."
options = {
  query: { 
    transferTo: user2['id'],
    removeFromSharing: true
  }
}
body = ss_connection.request('delete', "/user/#{user1['id']}", options) 
puts "User #{user1['id']} removed from org, sharing, sheets transferred."

# refresh list of users
puts "Fetching updated user list..."
body = ss_connection.request('get', '/users') 
puts "Users total: #{body.length}"
body.each_with_index do |s, i|
  puts "User #{i+1}, id: #{s['id']}, name: #{s['firstName']} #{s['lastName']}"
end
puts

# refresh list of sheets
puts "Fetching updated sheet list..."
body = ss_connection.request('get', '/users/sheets') 
puts "Sheets total: #{body.length}"
body.each_with_index do |s, i|
  puts "Sheet #{i+1}, id #{s['id']}, name: #{s['name']}, owner: #{s['owner']}"
end
puts

puts "Completed admin.rb."
puts
