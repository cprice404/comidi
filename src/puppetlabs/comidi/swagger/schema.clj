(ns puppetlabs.comidi.swagger.schema
  (:require [puppetlabs.comidi :as comidi]
            [compojure.response :as compojure-response]
            [clojure.string :as str]
            [schema.core :as schema]
            [ring.swagger.swagger2-full-schema :as swagger2-schema]
            [bidi.schema :as bidi-schema])
  (:import (schema.core Predicate)))

(def #^{:doc "TODO"}
  routes->handler #'comidi/routes->handler)

(def #^{:doc "TODO"}
  context #'comidi/context)

(def RouteMetadata
  {(schema/optional-key :query-params) [schema/Symbol]
   (schema/optional-key :form-params) [schema/Symbol]
   (schema/optional-key :params) [schema/Symbol]
   (schema/optional-key :summary) schema/Str
   (schema/optional-key :description) schema/Str
   (schema/optional-key :tags) [schema/Str]
   :return Predicate})

(defn- qp-binding [req sym]
  "TODO"
  `(get-in ~req [:query-params ~(keyword sym)]
           (get-in ~req [:query-params ~(name sym)])))

(defn- get-qp-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [sym]
            [sym (qp-binding req sym)])
          (:query-params route-meta)))

;; TODO: DRY up

(defn- fp-binding [req sym]
  "TODO"
  `(get-in ~req [:form-params ~(keyword sym)]
           (get-in ~req [:form-params ~(name sym)])))

(defn- get-fp-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [sym]
            [sym (fp-binding req sym)])
          (:form-params route-meta)))

;; TODO: DRY up

(defn- param-binding [req sym]
  "TODO"
  `(get-in ~req [:params ~(keyword sym)]
           (get-in ~req [:params ~(name sym)])))

(defn- get-param-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [sym]
            [sym (param-binding req sym)])
          (:params route-meta)))

(defmacro let-request
  [[route-meta req] & body]
  ;; TODO: add support for other bindings besides query params
  ;; TODO: type coercion?
  (let [req-bindings (concat
                      (get-qp-req-bindings route-meta req)
                      (get-fp-req-bindings route-meta req)
                      (get-param-req-bindings route-meta req))]
    `(let [~@req-bindings]
       ~@body)))

(defmacro handler-fn*
  "Helper macro, used by the compojure-like macros (GET/POST/etc.) to generate
  a function that provides compojure's destructuring and rendering support."
  [route-spec body]
  `(fn [request#]
     (compojure-response/render
      (let-request [~route-spec request#] ~@body)
      request#)))

(defn- params->meta*
  [params]
  (mapv (fn [sym] `(quote ~sym)) params))

(defn- route-spec->meta*
  [route-spec]
  (println "ROUTE SPEC:" route-spec)
  ;; TODO: DRY up
  (-> route-spec
      (#(if (:params %) (assoc % :params (params->meta* (:params %))) %))
      (#(if (:form-params %) (assoc % :form-params (params->meta* (:form-params %))) %))
      (#(if (:query-params %) (assoc % :query-params (params->meta* (:query-params %))) %))))

(defn- route-with-method*
  "Helper function, used by the compojure-like macros (GET/POST/etc.) to generate
  a bidi route that includes a wrapped handler function."
  [method pattern route-spec body]
  (let [route-spec-meta (route-spec->meta* route-spec)]
    `[~pattern {~method (with-meta
                         (handler-fn* ~route-spec ~body)
                         ~route-spec-meta)}]))

(defmacro GET
  [pattern route-spec & body]
  (route-with-method* :get pattern route-spec body))

(defmacro POST
  [pattern route-spec & body]
  (route-with-method* :post pattern route-spec body))

(defmacro ANY
  [pattern route-spec & body]
  (let [route-spec-meta (route-spec->meta* route-spec)]
    `[~pattern (with-meta
                (handler-fn* ~route-spec ~body)
                ~route-spec-meta)]))

(schema/defn ^:always-validate
  route-meta->swagger-path :- swagger2-schema/PathItem
  [method :- comidi/RequestMethod
   route-meta :- RouteMetadata]
  ;; We need to convert `:any` to something else, because swagger does not
  ;;  support it.  Might be more correct to iterate over all of the http methods
  ;;  and create an entry for each
  (println "ROUTE META:" route-meta)
  (let [method (if (= :any method) :post method)
        rv
        {method (-> {:description (:description route-meta)
                     :responses {200 {:schema (:return route-meta)}}}
                    ;; TODO clean up
                    (#(if (:summary route-meta)
                       (assoc % :summary (:summary route-meta))
                       %))
                    (#(if (:tags route-meta)
                       (assoc % :tags (:tags route-meta))
                       %)))}]
    (println "RV:" rv)
    rv))

(schema/defn ^:always-validate
  swagger-paths :- swagger2-schema/Paths
  [routes :- bidi-schema/RoutePair]
  (comidi/walk-route-tree
   routes {}
   (fn [acc route-node route-info pattern route-handler]
     (let [route-meta (meta route-handler)]
       (assoc acc (str/join (:path route-info))
                  (route-meta->swagger-path
                   pattern route-meta))))))