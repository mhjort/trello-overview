(ns trello-overview.handler
  (:require [compojure.core :refer :all]
            [cheshire.core :refer [generate-string]]
            [trello-overview.trello :refer [all-names-for-list]]))

(defroutes endpoints
  (GET "/cards/:list-name" [list-name] (generate-string {:cards (all-names-for-list list-name)})))
