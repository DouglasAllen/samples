Rails.application.config.middleware.use OmniAuth::Builder do
  provider :smartsheet, 'APP_CLIENT_ID', nil, :smartsheet_secret => 'APP_SECRET', :scope => 'READ_SHEETS'
end
