(ns puppetlabs.comidi.swagger.schema-testutils
  (:require [puppetlabs.comidi.swagger.schema :as comidi-schema]
            [schema.core :as schema]
            [ring.middleware.params :as params]))

(def foo-routes
  (comidi-schema/context "/foo"
    (comidi-schema/GET "/plus"
      {:return schema/Int
       :query-params [x y]
       :summary "x+y with query parameters"}
      {:body (str (+ (Integer/parseInt x)
                     (Integer/parseInt y)))})
    (comidi-schema/POST "/minus"
      {:return schema/Int
       :form-params [x y]
       :summary "x-y with form parameters"}
      {:body (str (- (Integer/parseInt x)
                     (Integer/parseInt y)))})
    (comidi-schema/POST "/multiply"
      {:return schema/Int
       :params [x y]
       :summary "x*y with any parameters"}
      {:body (str (* (Integer/parseInt x)
                     (Integer/parseInt y)))})
    (comidi-schema/ANY "/divide"
      {:return schema/Int
       :params [x y]
       :summary "x/y with any parameters"}
      {:body (str (/ (Integer/parseInt x)
                     (Integer/parseInt y)))})))

(def foo-handler
  (-> foo-routes
      comidi-schema/routes->handler
      params/wrap-params))

