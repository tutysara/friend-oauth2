(ns friend-oauth2.util-facts
  (:use
   midje.sweet
   friend-oauth2.fixtures)
  (:require
   [friend-oauth2.util :as oauth2-util]
   [friend-oauth2.workflow :as oauth2]
   [cemerick.url :as url]
   [ring.mock.request :as ring-mock]
   [ring.util.response :refer [response]]))

(fact
 "Extracts the access token from a JSON access token response"
 (oauth2-util/extract-access-token access-token-response-fixture)
 => "my-access-token")

(fact
 "Extracts the access token from out-of-spec params"
 (oauth2-util/get-access-token-from-params (response "access_token=my-access-token"))
 => "my-access-token")

(fact
 "Returns nil if there is no code in the request"
 (get-in (ring-mock/request :get "/redirect") [:params :code]) => nil)

(fact
 "Formats URI from domain and path pairs in a map"
 (oauth2-util/format-config-uri client-config-fixture)
 => "http://127.0.0.1/redirect")

(fact
 "Formats the client authentication uri"
 (let [authn-uri (oauth2-util/format-authn-uri uri-config-fixture "anti-forgery-token")
       authn-uri (url/url authn-uri)]
   (:path authn-uri)  => "/authenticate"
   (:query authn-uri) => {"redirect_uri" "http://127.0.0.1/redirect"
                          "client_id"    "my-client-id"
                          "state"        "anti-forgery-token"}))

(fact
 "Replaces the authorization code"
 ((oauth2-util/replace-authz-code (uri-config-fixture :access-token-uri) "my-code") :code)
 => "my-code")

(fact
 "Returns the config key of the first provider in which callback uri matches the path-info of request"
 (oauth2-util/get-provider (ring-mock/request :get "/auth/a") {:providers providers}) => :a)

(fact
 "Returns nil if no provider's callback uri matches the path-info of request"
 (oauth2-util/get-provider (ring-mock/request :get "/auth/z") {:providers providers}) => nil)
