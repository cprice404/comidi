(ns puppetlabs.comidi.swagger.schema
  (:require [puppetlabs.comidi :as comidi]
            [compojure.response :as compojure-response]
            [compojure.core :as compojure]
            [clojure.string :as str]))

(def #^{:doc "TODO"}
  routes->handler #'comidi/routes->handler)

(def #^{:doc "TODO"}
  context #'comidi/context)

(defn- qp-binding [req sym]
  "TODO"
  `(get-in ~req [:query-params ~(keyword sym)]
           (get-in ~req [:query-params ~(name sym)])))

(defn- get-qp-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [kw]
            (let [sym (symbol (name kw))]
              [sym (qp-binding req sym)]))
          (:query-params route-meta)))

;; TODO: DRY up

(defn- fp-binding [req sym]
  "TODO"
  `(get-in ~req [:form-params ~(keyword sym)]
           (get-in ~req [:form-params ~(name sym)])))

(defn- get-fp-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [kw]
            (let [sym (symbol (name kw))]
              [sym (fp-binding req sym)]))
          (:form-params route-meta)))

;; TODO: DRY up

(defn- param-binding [req sym]
  "TODO"
  `(get-in ~req [:params ~(keyword sym)]
           (get-in ~req [:params ~(name sym)])))

(defn- get-param-req-bindings
  "TODO"
  [route-meta req]
  (mapcat (fn [kw]
            (let [sym (symbol (name kw))]
              [sym (param-binding req sym)]))
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

(defn- route-with-method*
  "Helper function, used by the compojure-like macros (GET/POST/etc.) to generate
  a bidi route that includes a wrapped handler function."
  [method pattern route-spec body]
  `[~pattern {~method (with-meta
                       (handler-fn* ~route-spec ~body)
                       ~route-spec)}])

(defmacro GET
  [pattern route-spec & body]
  (route-with-method* :get pattern route-spec body))

(defmacro POST
  [pattern route-spec & body]
  (route-with-method* :post pattern route-spec body))

(defmacro ANY
  [pattern route-spec & body]
  (route-with-method* :any pattern route-spec body))

(defn route-meta->swagger-path
  [method route-meta]
  {method {:responses {200 {:schema (:return route-meta)
                            :description ""}}}})

(defn swagger-paths
  [routes]
  (comidi/walk-route-tree
   routes {}
   (fn [acc route-node route-info method route-handler]
     (let [route-meta (meta route-handler)]
       (assoc acc (str/join (:path route-info))
                  (route-meta->swagger-path
                   method route-meta))))))
