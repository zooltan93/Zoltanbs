(ns nak-test00.core
  (:require [org.httpkit.client :as http]
            [clojure.data.csv :as csv]
            [cheshire.core :as json]
            [clojure.core]
            [clojure.java.io :as io]
            [clojure.walk :only keywordize-keys]
            [clojure.string :as str :only replace])
  (:gen-class))


;;defs
(def common {:method  :post
             :headers {"Content-Type" "application/json"}})

(def cig-path "resources/csv/cig_partner.csv")
(def ober-path "resources/csv/ober_partner.csv")



;;Login
(defn login
  [username password]
  (let [request (merge common
                       {:url  "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                        :body (json/generate-string {:userName username :password password})})
        respnse @(http/request request)]
    (get-in respnse [:headers :x-auth-token])))

;;teszthez
(def korteheni (login "korteheni" "Krumpli10"))


(defn csv-in
  [path]
  ;;egyszintű map a csv-ből
  (let [[keys & vals] (csv/read-csv (io/reader path))
        keys (map keyword keys)]
    (map (partial zipmap keys) vals)))


(defn create-partner-maps
  [path]
  (let [csv (csv-in path)
        maps (reduce
               (fn [ret csv-element]
                 (cons {:partner {
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
                                  }} ret)) [] csv)]
    maps))


(defn create-partner
  [authToken partner-path]
  (let [partners (create-partner-maps partner-path)]
    (map #(let [resp
                @(http/request
                   {:url     "http://nak-test.dbx.hu/api/partner/partners"
                    :method  :post
                    :body    (json/generate-string %)
                    :headers {"X-Auth-Token" authToken "Content-Type" "application/json"}})]
           (get (json/parse-string (get resp :body)) "messages")) partners)))