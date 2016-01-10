(ns trello-overview.handler
  (:require [compojure.core :refer :all]
            [trello.core :as t :refer [make-client]]))


(def trello-key (System/getenv "TRELLO_KEY"))
(def trello-secret (System/getenv "TRELLO_SECRET"))

(def client (make-client trello-key trello-secret))

(client t/api-call :GET "boards/my-board-id")


(defroutes endpoints
  (GET "/boards" [] "1"))
