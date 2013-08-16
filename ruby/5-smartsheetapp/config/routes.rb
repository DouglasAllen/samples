Smartsheetapp::Application.routes.draw do

  get "sheets/index"
  resource :session
  resources :sheets

  root to: "home#index"

  match '/auth/:provider/callback' => 'sessions#create', via: [:get, :post]
  match '/auth/failure' => 'sessions#failure', via: [:get, :post]
end
