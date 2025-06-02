(ns aceitacao.financeiro.saldo-aceitacao-test
  (:require
    [midje.sweet :refer :all]
    [unitarios.financeiro.auxiliares :refer :all]))

(against-background [(before :facts (iniciar-servidor 3001))
                     (after :facts parar-servidor)]
  (fact "O saldo inicial é 0" :aceitacao
        ( conteudo "/saldo") => "0"))
