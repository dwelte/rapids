(ns rapids.state-machine)

(defprotocol StateMachine
  "Protocol that represents the interface for a state machine that rapids can
   distribute

   process:  Takes a message and applies it to the state machine
   snapshot: Turns a message that encapsulates the current state of the
             state machine"
  (process
    [_ message])
  (snapshot
    [_]))

(defrecord MapStateMachine [state]
  StateMachine
  (process [_ {:keys [message-type value]}]
    (case message-type
      :snapshot (reset! state value)
      :assoc    (swap! state #(assoc % (first value) (second value)))
      :dissoc   (swap! state #(dissoc % value))))
  (snapshot [_] state))

