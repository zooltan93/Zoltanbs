(ns nak-test00.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:gen-class))


;; Login és auth token kinyerése
(defn login
  []
  (get-in (client/post "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                        {:debug true :debug-body true
                         :form-params {:userName "korteheni" :password "Krumpli10"}
                         :content-type :json})
  [:headers :X-Auth-Token]))


;; Parner létrehozása

(defn send-to
  [partner-data]
  (client/post "http://nak-test.dbx.hu/api/partner/partners"
               {:debug true :debug-body true
                :body (slurp "resources/partner.txt")
               :headers {"X-Auth-Token" (login)}
               :content-type :json}))


(login)
(send-to partner-data)