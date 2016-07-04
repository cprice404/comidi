(ns puppetlabs.comidi.swagger.schema-test
  (:require [clojure.test :refer :all]
            [puppetlabs.comidi.swagger.schema :as comidi-schema]
            [puppetlabs.comidi.swagger.schema-testutils :as testutils]
            [ring.mock.request :as mock]
            [schema.core :as schema]
            [puppetlabs.trapperkeeper.services.swagger-ui.swagger-ui-core :as swagger-ui-core]))

(deftest get-test
  (testing "query param args work on GET route"
    (let [req (mock/request :get "/foo/plus?x=4&y=2")]
      (is (= "6"
             (:body (testutils/foo-handler req)))))))

(deftest swagger-metadata-test
  (testing "swagger data is attached to routes as metadata"
    ;; TODO: do this with the zipper?
    (let [plus-route (first (second testutils/foo-routes))
          plus-route-meta (meta plus-route)]
      (is (= #{:return :query-params :summary}
             (set (keys plus-route-meta))))
      (is (= "x+y with query-parameters" (:summary plus-route-meta)))
      (is (= [:foo-handler/x :foo-handler/y]
             (:query-params plus-route-meta)))
      (is (= schema/Int (:return plus-route-meta))))))

(deftest register-paths-test
  (testing "Can register paths based on route metadata"
    (is (= {"/foo/plus" {:get
                         {:responses
                          {200 {:description ""
                                :schema schema/Int}}}}}
           (swagger-ui-core/register-schema-paths
            (atom {})
            (comidi-schema/swagger-paths
             testutils/foo-routes))))))