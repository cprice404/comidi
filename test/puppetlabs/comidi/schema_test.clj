(ns puppetlabs.comidi.schema-test
  (:require [clojure.test :refer :all]
            [puppetlabs.comidi.schema :as comidi-schema]
            [ring.middleware.params :as params]
            [ring.mock.request :as mock]
            [schema.core :as schema]
            [puppetlabs.trapperkeeper.services.swagger-ui.swagger-ui-service :as svc]
            ))


(def scratch-routes
  (comidi-schema/context "/foo"
    (comidi-schema/GET "/plus"
      ;; TODO: maybe have the macro inject the `spec/spec` part here?
      {:return schema/Int
       :query-params [:foo-handler/x :foo-handler/y]
       :summary "x+y with query-parameters"}
      {:body (str (+ (Integer/parseInt x)
                     (Integer/parseInt y)))})))

(def scratch-handler
  (params/wrap-params
   (comidi-schema/routes->handler
    scratch-routes)))

(deftest scratch-handler-test
  (testing "plus works"
    (let [req (mock/request :get "/foo/plus?x=4&y=2")]
      (is (= "6"
             (:body (scratch-handler req)))))))

(deftest scratch-handler-specs-test
  (testing "specs are attached to routes as metadata"
    ;; TODO: do this with the zipper?
    (let [plus-route (first (second scratch-routes))
          plus-route-meta (meta plus-route)]
      (is (= #{:return :query-params :summary}
             (set (keys plus-route-meta))))
      (is (= "x+y with query-parameters" (:summary plus-route-meta)))
      (is (= [:foo-handler/x :foo-handler/y]
             (:query-params plus-route-meta)))
      (is (= schema/Int (:return plus-route-meta))))))

#_(deftest scratch-handler-register-paths-test
  (testing "Can register paths based on route metadata"
    (is (= {"/foo/plus" {:get
                         {:responses
                          {200 {:description ""
                                :schema schema/Int}}}}}
           (-> (svc/register-schema-paths (atom {})
                                    (comidi-schema/swagger-paths
                                     scratch-routes))
               (update-in ["/foo/plus" :get :responses 200 :schema]
                          identity))))))
