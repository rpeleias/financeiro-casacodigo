(ns unitarios.financeiro.handler-test
  (:require [financeiro.handler :refer :all]
            [midje.sweet :refer :all]
            [cheshire.core :as json]
            [ring.mock.request :as mock]
            [financeiro.db :as db]))

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
       (against-background [(json/generate-string {:saldo 0}) => "{\"saldo\":0}"
                            (db/saldo) => 0])
       (let [response (app (mock/request :get "/saldo"))]

         (fact "o formato é 'application/json'"
               (get-in response [ :headers "Content-Type"]) => "application/json; charset=utf-8")

         (fact "o status da resposta é 200"
               (:status response) => 200)

         (fact "o texto do corpo é um JSON cuja chave é saldo e o valor é "
               (:body response) => "{\"saldo\":0}")))

(facts "Regsitra uma receita no valor de 10"
       (against-background (db/registrar {:valor 10 :tipo "receita"}) => {:id 1 :valor 10 :tipo "receita"})
       (let [response
             (app (-> (mock/request :post "/transacoes")
                      (mock/json-body {:valor 10 :tipo "receita"})))]
         (fact "o status da resposta é 201")
         (fact "o texto do corpo é um JSON com o conteúdo enviado e um id"
               (:body response) =>
               "{\"id\":1,\"valor\":10,\"tipo\":\"receita\"}")))

(facts "Existe rota para lidar com filtro de transação por tipo"
       (against-background
         [(db/transacoes-do-tipo "receita") => '({:id 1 :valor 2000 :tipo "receita"})
          (db/transacoes-do-tipo "despesa") => '({:id 2 :valor 89 :tipo "despesa"})
          (db/transacoes) => '({:id 1 :valor 2000 :tipo "receita"}
                                {:id 2 :valor 89 :tipo "despesa"})]

         (fact "Filtro por receita"
               (let [response (app (mock/request :get "/receitas"))]
                 (:status response) => 200
                 (:body response) => (json/generate-string
                                       {:transacoes '({:id 1 :valor 2000 :tipo "receita"})})))

         (fact "Filtro por despesa"
               (let [response (app (mock/request :get "/despesas"))]
                 (:status response) => 200
                 (:body response) => (json/generate-string
                                       {:transacoes '({:id 2 :valor 89 :tipo "despesa"})})))

         (fact "Sem filtro"
               (let [response (app (mock/request :get "/transacoes"))]
                 (:status response) => 200
                 (:body response) => (json/generate-string
                                       {:transacoes '({:id 1 :valor 2000 :tipo "receita"}
                                                      {:id 2 :valor 89 :tipo "despesa"})})))))