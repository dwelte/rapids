(ns rapids.state-machine-test
  (:require [clojure.test :refer :all]
            [rapids.state-machine :refer :all]))

(deftest create-state-machine-test
  (testing "Creating state machine"
    (let [sm (->MapStateMachine (atom {}))]
      (is (snapshot sm) {}))))

(deftest simple-flow-test
  (testing ""
    (let [sm (->MapStateMachine (atom {}))]
      (is (snapshot sm) {})
      (process sm {:message-type :snapshot :value {:a 1 :b 2}})
      (is (snapshot sm) {:a 1 :b 2})
      (process sm {:message-type :assoc :value [:a 3]})
      (is (snapshot sm) {:a 3 :b 2})
      (process sm {:message-type :assoc :value [:c 4]})
      (is (snapshot sm) {:a 3 :b 2 :c 4})
      (process sm {:message-type :dissoc :value :b})
      (is (snapshot sm) {:a 3 :c 4})
      (process sm {:message-type :assoc :value [:b 5]})
      (is (snapshot sm) {:a 3 :b 5 :c 4})
      (process sm {:message-type :dissoc :value :a})
      (is (snapshot sm) {:b 5 :c 4})
      (process sm {:message-type :snapshot :value {:e 1 :f 2}})
      (is (snapshot sm) {:e 1 :f 2}))))
