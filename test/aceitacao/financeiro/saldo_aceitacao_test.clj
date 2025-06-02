(ns aceitacao.financeiro.saldo-aceitacao-test
  (:require
    [cheshire.core :as json]
    [clj-http.client :as http]
    [midje.sweet :refer :all]
    [unitarios.financeiro.auxiliares :refer :all]
    [financeiro.db :as db]))

(against-background [(before :facts [(iniciar-servidor porta-padrao)
                                     (db/limpar)])
                     (after :facts (parar-servidor))]
                    (fact "O saldo inicial é 0" :aceitacao
                          (json/parse-string (conteudo "/saldo") true) => {:saldo 0})

                    (fact "O saldo é 10 quando a única transação é uma receita de 10" :aceitacao
                          (http/post (endereco-para "/transacoes")
                                     {:content-type :json
                                      :body         (json/generate-string {:valor 10 :tipo "receita"})})
                          (json/parse-string (conteudo "/saldo") true) => {:saldo 10})
                    (fact "O saldo é 1000 quando criamos duas receitas de 2000 e uma despesa de 3000" :aceitacao
                          (http/post (endereco-para "/transacoes")
                                     {:content-type :json
                                      :body         (json/generate-string {:valor 2000 :tipo "receita"})})

                          (http/post (endereco-para "/transacoes")
                                     {:content-type :json
                                      :body         (json/generate-string {:valor 2000 :tipo "receita"})})

                          (http/post (endereco-para "/transacoes")
                                     {:content-type :json
                                      :body         (json/generate-string {:valor 3000 :tipo "despesa"})})
                          (json/parse-string (conteudo "/saldo") true) => {:saldo 1000}
                          ))
