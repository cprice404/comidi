(ns puppetlabs.comidi.swagger.schema-testutils
  (:require [puppetlabs.comidi.swagger.schema :as comidi-schema]
            [schema.core :as schema]
            [ring.middleware.params :as params]))

(def foo-routes
  (comidi-schema/context "/foo"
    (comidi-schema/GET "/plus"
      {:return schema/Int
       :query-params [:foo-handler/x :foo-handler/y]
       :summary "x+y with query-parameters"}
      {:body (str (+ (Integer/parseInt x)
                     (Integer/parseInt y)))})))

(def foo-handler
  (params/wrap-params
   (comidi-schema/routes->handler
    foo-routes)))

