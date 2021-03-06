(defproject puppetlabs/comidi "0.2.2-SNAPSHOT"
  :description "Puppet Labs utility functions and compojure-like wrappers for use with the bidi web routing library"
  :url "https://github.com/puppetlabs/comidi"

  :pedantic? :abort

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.reader "0.8.9"]
                 [clj-time "0.7.0"]

                 [ring/ring-core "1.3.2"]
                 [commons-io "2.4"]
                 [bidi "1.21.0" :exclusions [org.clojure/clojurescript]]
                 [compojure "1.3.3"]
                 [prismatic/schema "0.4.3"]
                 [puppetlabs/kitchensink "1.1.0"]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]])
