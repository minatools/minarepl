(ns mina.repl
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [hato.client :as http-client]
            [graphql-builder.parser :refer [defgraphql]]
            [graphql-builder.core :as gqlb]
            [re-graph.core :as re-graph]
            ;; [re-frame.core :as re-frame]
            [cheshire.core :as json]))

(def pub-key "B62qm1CqZ3A7gY43QoAKLTnkorVEMyuRwv1minimcUv42xjN6QJm4UB")

(defgraphql graphql-queries
  "gql/account_for_key_and_token.graphql"
  "gql/account_create.graphql"
  "gql/accounts_for_key.graphql"
  "gql/accounts_list.graphql"
  "gql/daemon_status.graphql"
  "gql/peers_get.graphql"
  "gql/snark_pool.graphql"
  "gql/snark_set_work_fee.graphql"
  "gql/snark_set_worker.graphql"
  "gql/snark_work_pending.graphql"
  "gql/token_next_available.graphql"
  )

(def query-map (gqlb/query-map graphql-queries))

;; now query-map takes key :query to a map  all the queries.
(-> query-map :query keys)
;; similar for mutations
(-> query-map :mutation keys)

;; each query contains a function; to set query variables,
;; apply the function to a map:

(def gql-request
  ;; ((-> query-map :query :accounts-list))
  ((-> query-map :query :account-for-key-and-token)
   {:pub-key pub-key :tok "1"})
  ;; ((-> query-map :query :accounts-for-key) {:public-key pub-key})
  ;; ((-> query-map :mutation :account-create) {:password pub-key})

  ;; ((-> query-map :query :daemon-status))
  ;; ((-> query-map :query :get-peers))

  ;; ((-> query-map :query :snark-pool))
  ;; ((-> query-map :mutation :snark-set-work-fee) {:fee 150000000}) ;; in nanomina
  ;; ((-> query-map :mutation :snark-set-worker) {:public-key pub-key})
  ;; ((-> query-map :query :snark-work-pending))

  ;; ((-> query-map :query :token-next-available))
  )

(pp/pprint gql-request)

(let [qry (-> gql-request :graphql :query)
      ;; _ (println qry)
      vars (-> gql-request :graphql :variables)
      ;; _ (println vars)
      json-body (json/encode {:query qry :variables vars})
      ;; _ (println json-body)

      result (http-client/post "http://localhost:3085/graphql"
                               {:body json-body
                                :content-type :json})]

  ;; result is a map. print the keys:
  ;; (keys result)

  ;; print some key values:
  ;; (result :uri)
  ;; (result :headers)
  ;; (result :version)
  ;; what did we ask for?
  ;; (println (-> result :request :body))

  (println "result status: " (result :status))

  ;; what did we get?
  (let [output (json/decode (-> result :body)
                            true ;; keywordize keys
                            )]
    (pp/pprint output)))

;; for snark data:

;; how many?
(count (-> output
           :data
           ;; :pendingSnarkWork
           :snarkPool
           ))

(keys (first (-> output :data :snarkPool)))

(def snark-pool (-> output :data :snarkPool))
(def fees (map #(-> % :fee Long/parseLong) snark-pool))

;; stats helper fns https://gist.github.com/scottdw/2960070
(defn mode [vs]
  (let [fs (frequencies vs)]
        (first (last (sort-by second fs)))))
(defn quantile
  ([p vs]
   (let [svs (sort vs)]
     (quantile p (count vs) svs (first svs) (last svs))))
  ([p c svs mn mx]
   (let [pic (* p (inc c))
         k (int pic)
         d (- pic k)
         ndk (if (zero? k) mn (nth svs (dec k)))]
     (cond
       (zero? k) mn
       (= c (dec k)) mx
       (= c k) mx
               :else (+ ndk (* d (- (nth svs k) ndk)))))))
(defn median
  ([vs] (quantile 0.5 vs))
  ([sz svs mn mx] (quantile 0.5 sz svs mn mx)))

(defn mean
  ([vs] (mean (reduce + vs) (count vs)))
    ([sm sz] (/ sm sz)))

;; insert commas
;; human-readable print
(defn hprint [n] (pp/cl-format nil "~:d" n))

(reverse (sort-by val (frequencies fees)))

(reverse (sort-by first (seq (frequencies fees))))

(hprint 1000000000000000)


(apply min fees)
(hprint (apply max fees))
(hprint (mode fees))
(hprint (median fees))
(hprint (float (mean fees)))

;; we don't need no stinkin' main!
;; (defn -main
;;   [& args]

;;   (println "entering main")

;;   (println "exiting main")
;;   #_(shutdown-agents))
