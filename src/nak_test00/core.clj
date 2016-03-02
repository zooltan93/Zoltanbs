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

(defn login []
  (let [{:keys [body] :as resp}
      @(http/request
         (merge common
                {:url "http://nak-test.dbx.hu/api/auth/v1/users/authenticate"
                 :body (json/write-str {:userName "korteheni" :password "Krumpli10"})}))]
  (:authToken (json/read-json body))))


;;CSV parse
(defn csv-in
  [path]
  (csv/read-csv (io/reader path)))

  ;;Ket vektor, ezekbol kell kiszedni 1-1 elemet, azokat osszmappelni, es belerakni egy nagy map-be
(let [[tag val] (csv-in cig-path)]
  (println tag)
  (println val))


;;Partner létrehozás TODO
(defn create-partner
  [authToken partner-path]
  (http/request
    (merge common
           {:url "http://nak-test.dbx.hu/api/partner/partners"
            :body (csv-in partner-path)
            :header {"X-Auth_Token" authToken}})))



