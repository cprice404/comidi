(ns puppetlabs.comidi.swagger.schema-test
  (:require [clojure.test :refer :all]
            [puppetlabs.comidi :as comidi]
            [puppetlabs.comidi.swagger.schema :as comidi-schema]
            [puppetlabs.comidi.swagger.schema-testutils :as testutils]
            [ring.mock.request :as mock]
            [schema.core :as schema]
            [puppetlabs.trapperkeeper.services.swagger-ui.swagger-ui-core :as swagger-ui-core]
            [ring.middleware.params :as params]))

(deftest get-test
  (testing "query param args work on GET route"
    (let [req (mock/request :get "/foo/plus?x=4&y=2")]
      (is (= "6"
             (:body (testutils/foo-handler req)))))))

(deftest post-test
  (testing "form param args work on form params POST route"
    (let [req (mock/request :post "/foo/minus"
                            {:x 4 :y 2})]
      (is (= "2"
             (:body (testutils/foo-handler req))))))
  (testing "query param args don't work on form params POST route"
    (let [req (mock/request :post "/foo/minus?x=4&y=2")]
      (is (thrown? NumberFormatException
                   (testutils/foo-handler req)))))
  (testing "form param args work on generic params POST route"
    (let [req (mock/request :post "/foo/multiply"
                            {:x 4 :y 2})]
      (is (= "8"
             (:body (testutils/foo-handler req))))))
  (testing "query param args work on generic params POST route"
    (let [req (mock/request :post "/foo/multiply?x=4&y=2")]
      (is (= "8"
             (:body (testutils/foo-handler req)))))))

(deftest tests-to-write
  (testing "GET RID OF KEYWORD NAMESPACES IN ROUTE DESCS"
    (is (true? false)))
  (testing "works with ANY route"
    (is (true? false)))
  (testing "summary/description are passed through metadata"
    (is (true? false))))

(deftest swagger-metadata-test
  (testing "swagger data is attached to routes as metadata"
    (let [plus-route (comidi/find-handler
                      testutils/foo-routes
                      "/foo/plus"
                      :get)
          plus-route-meta (meta plus-route)]
      (is (= #{:return :query-params :summary}
             (set (keys plus-route-meta))))
      (is (= "x+y with query parameters" (:summary plus-route-meta)))
      (is (= [:foo-handler/x :foo-handler/y]
             (:query-params plus-route-meta)))
      (is (= schema/Int (:return plus-route-meta)))))

  (testing "with wrapped routes"
    (let [wrapped-routes (comidi/wrap-routes
                          testutils/foo-routes
                          (fn [handler]
                            (fn [req]
                              (handler req))))
          handler (params/wrap-params
                   (comidi-schema/routes->handler
                    wrapped-routes))]
      (testing "get request still works with wrapped routes"
        (let [req (mock/request :get "/foo/plus?x=4&y=2")]
          (is (= "6"
                 (:body (handler req))))))
      (testing "swagger metadata still available after wrap-routes"
        (let [plus-route (comidi/find-handler
                          wrapped-routes
                          "/foo/plus"
                          :get)
              plus-route-meta (meta plus-route)]
          (is (= #{:return :query-params :summary}
                 (set (keys plus-route-meta))))
          (is (= "x+y with query parameters" (:summary plus-route-meta)))
          (is (= [:foo-handler/x :foo-handler/y]
                 (:query-params plus-route-meta)))
          (is (= schema/Int (:return plus-route-meta))))))))

(deftest register-schema-paths-test
  (testing "Can register paths based on route metadata"
    (is (= {"/foo/plus" {:get {:responses {200 {:description ""
                                                :schema schema/Int}}}}
            "/foo/minus" {:post {:responses {200 {:description ""
                                                  :schema schema/Int}}}}
            "/foo/multiply" {:post {:responses {200 {:description ""
                                                     :schema schema/Int}}}}
            }
           (swagger-ui-core/register-schema-paths
            (atom {})
            (comidi-schema/swagger-paths
             testutils/foo-routes))))))
