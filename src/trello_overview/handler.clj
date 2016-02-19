(ns trello-overview.handler
  (:require [compojure.core :refer :all]
            [cheshire.core :refer [generate-string]]
            [ring.middleware.params :refer [wrap-params]]
            [trello-overview.trello :refer [all-boards cards-for-board-ids-and-list]]))

(defroutes all-routes
  (GET "/boards" [] (generate-string {:data (all-boards)}))
  (GET "/cards/list/:list-name"
       [list-name :as request]
       (let [board-ids (clojure.string/split (get (:params request) "board_ids") #",")]
         (generate-string {:data (cards-for-board-ids-and-list
                                   board-ids
                                   list-name)}))))

(def endpoints
  (wrap-params all-routes))

