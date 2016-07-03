(ns puppetlabs.comidi.schema
  (:require [puppetlabs.comidi :as comidi]
            [compojure.response :as compojure-response]
            [compojure.core :as compojure]
            [clojure.string :as str]))


(def #^{:doc "TODO"}
  routes->handler #'comidi/routes->handler)

(def #^{:doc "TODO"}
  context #'comidi/context)

(defn qp-binding [req sym]
  `(get-in ~req [:query-params ~(keyword sym)]
           (get-in ~req [:query-params ~(name sym)])))

(defn get-qp-req-bindings
  [route-meta req]
  (mapcat (fn [kw]
            (let [sym (symbol (name kw))]
              [sym (qp-binding req sym)]))
          (:query-params route-meta)))

(defmacro let-request
  [[route-meta req] & body]
  ;; TODO: add support for other bindings besides query params
  ;; TODO: type coercion?
  (let [req-bindings (get-qp-req-bindings route-meta req)]
    #_(println "req bindings:" req-bindings)
    `(let [~@req-bindings]
       ~@body)))

(defmacro handler-fn*
  "Helper macro, used by the compojure-like macros (GET/POST/etc.) to generate
  a function that provides compojure's destructuring and rendering support."
  [specs body]
  `(fn [request#]
     (compojure-response/render
      (let-request [~specs request#] ~@body)
      request#)))

(defn route-with-method*
  "Helper function, used by the compojure-like macros (GET/POST/etc.) to generate
  a bidi route that includes a wrapped handler function."
  [method pattern specs body]
  `(with-meta [~pattern {~method (handler-fn* ~specs ~body)}] ~specs))

(defmacro GET
  [pattern specs & body]
  (route-with-method* :get pattern specs body))

(defn route-meta->swagger-path
  [method route-meta]
  {method {:responses {200 {:schema (:return route-meta)
                            :description ""}}}})

(defn swagger-paths
  [routes]
  (let [paths (comidi/walk-route-tree
               routes {}
               (fn [acc route-node route-info method route-handler]
                 (println "VISITOR CALLED")
                 (println "\tROUTE NODE:" route-node)
                 (println "\tROUTE META:" (meta route-node))
                 (println "\tROUTE INFO:" route-info)
                 (println "\tMETHOD:" method)
                 (println "\tROUTE HANDLER:" route-handler)
                 (let [route-meta (meta route-node)]
                   (assoc acc (str/join (:path route-info))
                              (route-meta->swagger-path
                               method route-meta)

                              #_{:tags nil
                                 :summary nil
                                 :description nil
                                 :parameters nil
                                 :responses nil
                                 :produces nil
                                 :consumes nil
                                 :deprecated nil}))))]
    (println "PATHS:")
    (prn paths)
    paths))
