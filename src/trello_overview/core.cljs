(ns trello-overview.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [tailrecursion.cljson :refer [clj->cljson cljson->clj]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]))

(enable-console-print!)

(defonce app-state (atom {:cards []
                          :selected-boards #{}
                          :state "ready"}))

(defn- json-parse [s]
  (:data (js->clj (cljson->clj s) :keywordize-keys true)))

(defn cards []
  [:ul {:class (:state @app-state)}
   (for [item (:cards @app-state)]
     ^{:key item} [:li (str "[" (:board item) "]: " (:name item))])])

(defn- get-cards [list-name]
  (go
    (swap! app-state assoc :state "loading")
    (let [body (:body (<! (http/get (str "/cards/list/" list-name "?board_ids=" (clojure.string/join "," (:selected-boards @app-state))))))]
      (swap! app-state assoc :cards (json-parse body)
                             :state "ready"))))

(def lists ["Backlog" "In Progress" "Pending Deployment"])

(defn navi []
  [:ul {:class "boards"}
   (for [l lists]
     ^{:key l} [:li {:class (str "navi" (if (= (:list @app-state) l)
                                       " selected"
                                       ""))
                       :on-click #(do
                                    (swap! app-state assoc :list l)
                                    (get-cards l))} l])])
(defn- toggle-board [x]
  (if (some #(= (:id x) %) (:selected-boards @app-state))
    (swap! app-state update :selected-boards disj (:id x))
    (swap! app-state update :selected-boards conj (:id x))))

(defn boards []
  [:div
   {:class "boards"}
   (for [b (:boards @app-state)]
     ^{:key (:id b)} [:span {:on-click #(toggle-board b)
                             :class (if (some #(= (:id b) %) (:selected-boards @app-state))
                                      "selected"
                                      "")}

                      (:name b)])])

(defn trello-app []
  [:div
   (boards)
   (navi)
   (cards)])

(go
  (let [body (:body (<! (http/get "/boards")))]
    (swap! app-state assoc :boards (json-parse body))
    (reagent/render-component [trello-app]
                              (.-body js/document))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
