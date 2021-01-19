(ns mina.regraph
  (:require [re-graph.core :as re-graph]
            [re-frame.core :as re-frame]))
  ;; (:gen-class))


(def init-opts
  {:http {:url    "http://localhost:3085/graphql"
          :impl   {:connect-timeout 1000} ;; implementation-specific options (see clj-http or hato for options, defaults to {}, may be a literal or a function that returns the options)
          :supported-operations #{:query    ;; declare the operations supported via http, defaults to :query and :mutate
                                  :mutate}
          }})
  ;; {:ws
  ;;  {:url "ws://graphql" ;; override the websocket url (defaults to /graphql-ws, nil to disable)
  ;;   :sub-protocol            "graphql"              ;; override the websocket sub-protocol (defaults to "graphql-ws")
  ;;   :reconnect-timeout       5000                      ;; attempt reconnect n milliseconds after disconnect (defaults to 5000, nil to disable)
  ;;   :resume-subscriptions?   true                      ;; start existing subscriptions again when websocket is reconnected after a disconnect (defaults to true)
  ;;   :connection-init-payload {}                        ;; the payload to send in the connection_init message, sent when a websocket connection is made (defaults to {})
  ;;   :impl                    {} ;; implementation-specific options (see hato for options, defaults to {}, may be a literal or a function that returns the options)
  ;;   :supported-operations    #{:subscribe              ;; declare the operations supported via websocket, defaults to all three
  ;;                              :query                  ;;   if queries/mutations must be done via http set this to #{:subscribe} only
  ;;                              :mutate}
  ;;                    }})

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn on-thing [{:keys [data errors] :as payload}]
  (print "on-thing called with payload: " payload)
    )


(defn reframe []

  ;; initialise re-graph, possibly including configuration options (see below)
  (re-frame/dispatch [::re-graph/init {}])

  (re-frame/reg-event-db
   ::on-thing
   (fn [db [_ {:keys [data errors] :as payload}]]
     ;; do things with data e.g. write it into the re-frame database
     ))

  ;; start a subscription, with responses sent to the callback event provided
  (re-frame/dispatch [::re-graph/subscribe
                      :my-subscription-id  ;; this id should uniquely identify this subscription
                      "{ things { id } }"  ;; your graphql query
                      {:some "variable"}   ;; arguments map
                      [::on-thing]])       ;; callback event when messages are recieved

  ;; stop the subscription
  (re-frame/dispatch [::re-graph/unsubscribe :my-subscription-id])

  ;; perform a query, with the response sent to the callback event provided
  (re-frame/dispatch [::re-graph/query
                      "{ things { id } }"  ;; your graphql query
                      {:some "variable"}   ;; arguments map
                      [::on-thing]])       ;; callback event when response is recieved
  )

(defn regraph []
  (println "initializing re-graph")

  ;; (re-graph/init init-opts)
  (re-graph/init {:ws-url   "ws://localhost:3086/graphql-ws"
                  :http-url "http://localhost:3085/graphql"})

  ;; start a subscription, with responses sent to the callback-fn provided
  ;; (re-graph/subscribe :my-subscription-id  ;; this id should uniquely identify this subscription
  ;;                     "{ things { id } }"  ;; your graphql query
  ;;                     {:some "variable"}   ;; arguments map
  ;;                     on-thing)            ;; callback-fn when messages are recieved

  ;; ;; stop the subscription
  ;; (re-graph/unsubscribe :my-subscription-id)

  ;; ;; perform a query, with the response sent to the callback event provided
  ;; (re-graph/query "{ things { id } }"  ;; your graphql query
  ;;                 {:some "variable"}  ;; arguments map
  ;;                 on-thing)

  )

(defn -main
  [& args]

  (println "entering main")
  (re-graph/init {:ws-url   "ws://localhost:3086/graphql-ws"
                  :http-url "http://localhost:3085/graphql"})
  (println "exiting main")
  (shutdown-agents))
