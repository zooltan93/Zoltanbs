(ns nak-test00.core
  (:require [org.httpkit.client :as http]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.core]
            [clojure.java.io :as io])
  (:gen-class))


;;defs
(def common {:method :post
             :headers {"Content-Type" "application/json"}})

(def cig-path "resources/csv/cig_partner.csv")
(def ober-path "resources/csv/ober_partner.csv")


;;Login
(defn login
  [username password]
  (let [{:keys [body] :as resp}
      @(http/request
         (merge common
                {:url "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                 :body (json/write-str {:userName username :password password})}))]
    (println resp)))


;;CSV parse
;;Két vectort csinál a JSON-ből
(defn csv-in
  [path]
  (csv/read-csv (io/reader path)))

;;Egy-egy map-et csinál minden kulcs-érték párhoz
(defn csv-to-maps
  [path]
  (let [[tag val] (csv-in path)]
  (map hash-map tag val)))

;;Előállítja az egybefüggő JSON stringet
(defn partner-csv
  [path]
  (json/write-str
    (reduce (fn [ret element]
          (merge ret element))
        {} (csv-to-maps path))))


;;Partner létrehozás
(defn create-partner
  [authToken partner-path]
  (let [{:keys [body] :as resp}
  @(http/request
    (merge common
           {:url "http://nak-test.dbx.hu/api/partner/partners"
            :body (partner-csv cig-path)
            :header {"X-Auth_Token" authToken}}))]
    (println body)))