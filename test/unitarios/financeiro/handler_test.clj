(ns unitarios.financeiro.handler-test
  (:require [financeiro.handler :refer :all]
            [midje.sweet :refer :all]
            [cheshire.core :as json]
            [ring.mock.request :as mock]))

(facts "Dá um 'Hello World' na rota raiz"
       (let [response (app (mock/request :get "/"))]
         (fact "o status da resposta é 200"
               (:status response) => 200)
         (fact "o texto do corpo é 'Hello World'"
               (:body response) => "Olá, mundo!")))

(facts "Rota inválida não existe"
       (let [response (app (mock/request :get "/invalid"))]
         (fact "o código de erro é 404"
               (:status response) => 404)
         (fact "o texto do corpo é 'Not Found'"
               (:body response) => "Recurso não encontrado")))

(facts "Saldo inicial é 0"
       (against-background (json/generate-string { :saldo 0 }) => "{\"saldo\":0)")
       (let [response (app (mock/request :get "/saldo"))]

         (fact "o formato é 'application/json'"
               (get-in response [ :headers "Content-Type"]) => "application/json; charset=utf-8")
         (fact "o código de erro é 404"
               (:status response) => 200)
         (fact "o texto do corpo é um JSON cura chave é saldo e o valor é "
               (:body response) => "{\"saldo\":0)")))
