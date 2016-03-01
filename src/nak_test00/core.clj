(ns nak-test00.core
  (:gen-class))

(require 'clj-http.client)
(alias 'client 'clj-http.client)

(require 'cheshire.core)
(alias 'json 'cheshire.core)

(require 'clojure.walk)
(refer 'clojure.walk :only ['keywordize-keys])

(require 'clojure.string)
(alias 'string 'clojure.string)


;; Login és auth token kinyerése
(defn login
  []
  (get-in (client/post "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                        {:debug true :debug-body true
                         :form-params {:userName "korteheni" :password "Krumpli10"}
                         :content-type :json})
  [:headers :X-Auth-Token] "Cant login."))

;; Parner létrehozása
(def partner-data
  (slurp "resources/partner.txt"))

(defn send-to
  [partner-data]
  (client/post "http://nak-test.dbx.hu/api/partner/partners"
               {:debug true :debug-body true
                :body partner-data
               :headers {"X-Auth-Token" (login)}
               :content-type :json}))


(login)
(send-to partner-data)