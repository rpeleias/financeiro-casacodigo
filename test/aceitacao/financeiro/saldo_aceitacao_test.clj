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
          (http/post (endereco-para "/transacoes") (receita 10))

          (json/parse-string (conteudo "/saldo") true) => {:saldo 10})

    (fact "Rejeita uma transação sem valor" :aceitacao
          (let [resposta (http/post (endereco-para "/transacoes")
                                    (conteudo-como-json {:tipo "receita"}))]
            (:status resposta) => 422))

          (fact "Rejeita uma transação com valor negativo" :aceitacao
                (let [resposta (http/post (endereco-para "/transacoes")
                                          (receita -100))]
                  (:status resposta) => 422))

          (fact "Rejeita uma transação com valor que não é um numero" :aceitacao
                (let [resposta (http/post (endereco-para "/transacoes")
                                          (receita "mil"))]
                  (:status resposta) => 422))

          (fact "Rejeita uma transação sem tipo" :aceitacao
                (let [resposta (http/post (endereco-para "/transacoes")
                                          (conteudo-como-json {:valor 70}))]
                  (:status resposta) => 422))

          (fact "Rejeita uma transação com tipo desconhecido" :aceitacao
                (let [resposta (http/post (endereco-para "/transacoes")
                                          (conteudo-como-json {:valor 70 :tipo "investimento"}))]
                  (:status resposta) => 422))
  )