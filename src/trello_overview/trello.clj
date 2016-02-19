(ns trello-overview.trello
  (:require [trello.core :refer [with-user make-client]]
            [clojure.edn :as edn]
            [trello.client :refer [api-call]]))

(def settings (edn/read-string (slurp "config.edn")))
(def trello-key (-> settings :trello :key))
(def trello-secret (-> settings :trello :secret))
(def trello-token (-> settings :trello :token))

(def client (make-client trello-key trello-secret))

(defn- trello-call [uri]
  (with-user trello-token trello-secret
    (client api-call :GET uri)))

(defn boards-for-organization [organization]
  (map #(select-keys % [:id :name])
       (trello-call (str "organizations/" organization "/boards"))))

(def boards
  (delay
    (apply hash-map
           (mapcat vals (boards-for-organization (-> settings :trello :organization))))))

(defn- cards-for-list [list-id]
  (with-user trello-token trello-secret
    (map (fn [{:keys [name idBoard]}]
           {:name name :board (get @boards idBoard)})
         (client api-call :GET (str "lists/" list-id "/cards")))))

(defn- list-id-for-board-and-list [board-id list-name]
  (with-user trello-token trello-secret
    (let [lists (client api-call :GET (str "boards/" board-id "/lists"))]
      (:id (first (filter #(= list-name (:name %)) lists))))))

(defn cards-for-board-ids-and-list [board-ids list-name]
  (apply concat (pmap #(cards-for-list (list-id-for-board-and-list % list-name))
                      board-ids)))

(defn all-boards []
  (boards-for-organization (-> settings :trello :organization)))

