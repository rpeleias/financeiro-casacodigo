(ns financeiro.handler
  (:require [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [financeiro.db :as db]))

(defn como-json [conteudo & [status]]
  {:status (or status 200)}
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body    (json/generate-string conteudo)})

(defroutes app-routes
           (GET "/" [] "Olá, mundo!")
           (GET "/saldo" [] (como-json { :saldo 0}))
           (POST "/transacoes" requisicao (-> (db/registrar (:body requisicao))
                                              (como-json 201)))
           (route/not-found "Recurso não encontrado"))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})))
