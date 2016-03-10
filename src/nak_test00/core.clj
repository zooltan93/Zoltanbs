(ns nak-test00.core
  (:require [org.httpkit.client :as http]
            [clojure.data.csv :as csv]
            [cheshire.core :as json]
            [clojure.core]
            [clojure.java.io :as io]
            [clojure.walk :only keywordize-keys]
            [clojure.string :as str :only replace])
  (:gen-class))



(def partner-csv "resources/csv/partners.csv")

(defn list-error
  [body]
  (let [messages (get body "messages")]
    (println messages)))

(defn has-error?
  [body]
  (let [messages (get body "messages")]
    (if messages
      true
      false)))

;;;;;
(defn login
  [username password]
  (let [{:keys [body] :as resp} @(http/request {
                                                :url     "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                                                :method  :post
                                                :headers {"Content-Type" "application/json"}
                                                :body    (json/generate-string {:userName username :password password})})
        auth-token (get-in resp [:headers :x-auth-token])]
    (has-error? body)
    auth-token))



(def korteheni (login "korteheni" "Krumpli10"))

;;;;;;
(defn csv-in
  [path]
  ;;egyszintű map a csv-ből
  (let [[keys & vals] (csv/read-csv (io/reader path))
        keys (map keyword keys)]
    (map (partial zipmap keys) vals)))


(defn create-partner-map
  [csv-element]
  {:partner {
             :partnerRef          (:partnerRef csv-element)
             :partnerType         (:partnerType csv-element)
             :specialType         (:specialType csv-element)
             :messageLanguage     (:messageLanguage csv-element)
             :isUser              (boolean (:isUser csv-element))
             :isAgent             (boolean (:isAgent csv-element))
             :isOrgUnit           (boolean (:isOrgUnit csv-element))
             :country             (:country csv-element)
             :county              (:county csv-element)
             :fullName            (:fullName csv-element)
             :shortName           (:shortName csv-element)
             :companyForm         (:companyForm csv-element)
             :externalIdentifiers [{:externalIdentifierType  "VAT_NUMBER"
                                    :externalIdentifierValue (:vatNumber csv-element)}
                                   {:externalIdentifierType  "REGISTER_NUMBER"
                                    :externalIdentifierValue (:registerNumber csv-element)}]
             :establishedAt       (:establishedAt csv-element)
             :managerName         (:managerName csv-element)
             :addresses           [
                                   {:addressType   (:addressType csv-element)
                                    :addressRef    (:addressRef csv-element)
                                    :zip           (:zip csv-element)
                                    :city          (:city csv-element)
                                    :streetAddress (:streetAddress csv-element)
                                    :country       (:country csv-element)}]
             :contacts            [
                                   {:contactType  (:contactType csv-element)
                                    :phoneNumber  (:phoneNumber csv-element)
                                    :fax          (boolean (:fax csv-element))
                                    :emailAddress (:emailAddress csv-element)}]
             :bankAccountNumbers  [
                                   {:bankAccountRef  (:bankAccountRef csv-element)
                                    :number          (str/replace (:bankAccountNumber csv-element) #"-" "")
                                    :bankAccountType (:bankAccountType csv-element)
                                    :bankFullName    (:bankFullName csv-element)
                                    :bankShortName   (:bankShortName csv-element)
                                    :bankAddress     (:bankAddress csv-element)
                                    :bankSwiftCode   (:bankSwiftCode csv-element)}]
             }})


(defn create-agent-map
  [csv-element partnerRef]
  {:agent {
           :partnerRef         partnerRef
           :ruleTableCode      (:ruleTableCode csv-element)
           :supervisorAgentRef (:supervisorAgentRef csv-element)
           :certificates       []
           }})

;;;;;;
(defn send-json
  [url auth-token map-to-send]
  (let [{:keys [body] :as resp}
        @(http/request
           {:url     url
            :method  :post
            :body    (json/generate-string map-to-send)
            :headers {"X-Auth-Token" korteheni "Content-Type" "application/json"}})]

    (list-error (json/parse-string body))
    body))

;;;;; 1. create-partner meghívja az login-t, ha az valid választ ad, akkor beküldi a partner json-t 2. ha a create-partner valid választ ad, akkor az meghívja (jelen esetben még csak) a create-agent fv-t
(defn create-partner
  [path-to-csv]
  (let [auth-token korteheni]
    (if auth-token
      (let [flat-csv (csv-in path-to-csv)]
        (map #(let
               [resp (json/parse-string (send-json "http://nak-test.dbx.hu/api/partner/partners" auth-token (create-partner-map %)))]
               (if (true?
                     (and (= "true" (:isAgent %)) (not (has-error? resp)) ))
                 (send-json "http://nak-test.dbx.hu/api/agent/v1/agents" auth-token (create-agent-map % (get-in resp ["partner" "partnerRef"])))))
             flat-csv)))))