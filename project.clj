(defproject puppetlabs/comidi "0.3.2-SNAPSHOT"
  :description "Puppet Labs utility functions and compojure-like wrappers for use with the bidi web routing library"
  :url "https://github.com/puppetlabs/comidi"

  :pedantic? :abort

  :dependencies [[org.clojure/clojure "1.8.0"]
                 
                 ;; begin version conflict resolution dependencies
                 [clj-time "0.10.0"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.6.1"]
                 [commons-io "2.5"]
                 [slingshot "0.12.2"]
                 [ring/ring-codec "1.0.1"]
                 [ring/ring-core "1.5.0"]
                 [org.clojure/tools.reader "1.0.0-beta3"]
                 [clj-time "0.12.0"]
                 [commons-codec "1.9"]
                 ;; end version conflict resolution dependencies

                 [bidi "1.23.1" :exclusions [org.clojure/clojurescript]]
                 [compojure "1.4.0"]
                 [prismatic/schema "1.1.2"]

                 [puppetlabs/kitchensink "1.3.0"]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.0"]
                                  [puppetlabs/trapperkeeper-swagger-ui "0.1.0-SNAPSHOT"]]}}
  )
