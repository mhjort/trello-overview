(ns trello-overview.trello
  (:require [trello.core :refer [with-user make-client]]
            [clojure.edn :as edn]
            [trello.client :refer [api-call]]))

(def settings (edn/read-string (slurp "config.edn")))
(def trello-key (-> settings :trello :key))
(def trello-secret (-> settings :trello :secret))
(def trello-token (-> settings :trello :token))

(def client (make-client trello-key trello-secret))

(defn- names-for-list [list-id]
  (with-user trello-token trello-secret
    (map :name (client api-call :GET (str "lists/" list-id "/cards")))))

(defn- list-id-for-board-and-list [board-id list-name]
  (with-user trello-token trello-secret
    (let [lists (client api-call :GET (str "boards/" board-id "/lists"))]
      (:id (first (filter #(= list-name (:name %)) lists))))))

(defn all-names-for-list [list-name]
  (apply concat (pmap #(names-for-list (list-id-for-board-and-list % list-name))
                      (-> settings :trello :boards))))
