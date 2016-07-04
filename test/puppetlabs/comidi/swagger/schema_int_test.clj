(ns puppetlabs.comidi.swagger.schema-int-test
  (:require [clojure.test :refer :all]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]
            [puppetlabs.trapperkeeper.services.swagger-ui.swagger-ui-service :refer [swagger-ui-service]]
            [puppetlabs.trapperkeeper.services.webrouting.webrouting-service :refer [webrouting-service]]
            [puppetlabs.trapperkeeper.services.webserver.jetty9-service :refer [jetty9-service]]
            [puppetlabs.http.client.sync :as http-client]
            [puppetlabs.trapperkeeper.core :as tk]
            [puppetlabs.trapperkeeper.services :as tk-services]
            [puppetlabs.comidi.swagger.schema :as comidi-schema]
            [puppetlabs.comidi.swagger.schema-testutils :as testutils]))

(defprotocol FooService)

(tk/defservice foo-web-service
  FooService
  {:required [[:WebroutingService add-ring-handler]]
   :optional [SwaggerUIService]}
  (init [this context]
        (add-ring-handler this testutils/foo-handler)
        (if (tk-services/service-included? this :SwaggerUIService)
          (let [{:keys [register-tags register-schema-paths]} SwaggerUIService]
            (register-tags [{:name "foo"
                             :description "Foo Desc"}])
            (register-schema-paths
             (comidi-schema/swagger-paths testutils/foo-routes))))
        context))


(deftest swagger-ui-test
  (testing "swagger ui is served with registered paths"
    (with-app-with-config app
      [swagger-ui-service
       webrouting-service
       jetty9-service
       foo-web-service]
      {:webserver {:port 8000}
       :web-router-service
       {:puppetlabs.trapperkeeper.services.swagger-ui.swagger-ui-service/swagger-ui-service
        {:swagger "/docs"
         :swagger-json "/swagger.json"}
        :puppetlabs.comidi.swagger.schema-int-test/foo-web-service "/foo"}
       :swagger-ui {:info {:title "Test App"
                           :version "0.1.0"}}}
      (let [response (http-client/get "http://localhost:8000/swagger.json")]
        (is (= 200 (:status response))))
      (let [response (http-client/get "http://localhost:8000/docs")]
        (is (= 200 (:status response))))
      ;; TODO: would like to be able to validate swagger-ui output a bit more, but
      ;; it's very javascripty
      (let [response (http-client/get "http://localhost:8000/foo/plus?x=4&y=6"
                                      {:as :text})]
        (is (= 200 (:status response)))
        (is (= "10" (:body response))))))

  (testing "routes still work without swagger ui"
    (with-app-with-config app
      [webrouting-service
       jetty9-service
       foo-web-service]
      {:webserver {:port 8000}
       :web-router-service
       {:puppetlabs.comidi.swagger.schema-int-test/foo-web-service "/foo"}}
      (let [response (http-client/get "http://localhost:8000/foo/plus?x=4&y=6"
                                      {:as :text})]
        (is (= 200 (:status response)))
        (is (= "10" (:body response)))))))
