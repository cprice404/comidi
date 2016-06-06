(ns puppetlabs.comidi.spec
  (:require [puppetlabs.comidi :as comidi]
            [compojure.response :as compojure-response]
            [compojure.core :as compojure]))


(def #^{:doc "TODO"}
  routes->handler #'comidi/routes->handler)

(def #^{:doc "TODO"}
  context #'comidi/context)

(defmacro handler-fn*
  "Helper macro, used by the compojure-like macros (GET/POST/etc.) to generate
  a function that provides compojure's destructuring and rendering support."
  [bindings body]
  `(fn [request#]
     (compojure-response/render
      (compojure/let-request [~bindings request#] ~@body)
      request#)))

(defn route-with-method*
  "Helper function, used by the compojure-like macros (GET/POST/etc.) to generate
  a bidi route that includes a wrapped handler function."
  [method pattern specs body]
  `[~pattern {~method (handler-fn* ~(:bindings specs) ~body)}])



(defmacro GET
  [pattern specs & body]
  (route-with-method* :get pattern specs body))