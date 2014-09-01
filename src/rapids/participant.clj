(ns rapids.participant)

(defrecord ServerState
  [; persistant
   current-term
   voted-for
   logs

   ; volatile
   commit-index
   last-applied
   next-indexs
   match-indexs

   expiration-time
   role
   state-machine])

(defn persist-write-entries
  [server-state {:keys [term
                        leader-id
                        prev-log-index
                        prev-log-term
                        entries
                        leader-commit]
                 :as    arguments}]
  (-> server-state
      (trim-log-to prev-log-index)
      (persist-append-entries prev-log-index entries)
      (update-commit-index leader-commit)))

(defn trim-log-to
  [server-state prev-log-index]
  (let [first-index      (inc prev-log-index)
        old-logs         (server-state :logs)
        new-logs         entries
        conflicting-ind  (find-first-conflicting-index first-index old-logs new-logs)
        trimmed-logs     (if conflicting-ind (subvec old-logs 0 conflicting-ind) old-logs)]
    (assoc server-state :logs trimmed-logs)))

(defn append-entries-process
  [server-state {:keys [term
                        leader-id
                        prev-log-index
                        prev-log-term
                        entries
                        leader-commit]
                 :as    arguments}]
  (cond
    (< term (server-state :current-term))               [false server-state]
    (not= (-> logs prev-log-index :term) prev-log-term) [false server-state]
    :default                                            [true  (persist-write-entries server-state argmuents)]))

(defn append-entries
  [server-state-atom arguments]
  (let [server-state              @server-state-atom
        [result new-server-state] (append-entries-process server-state arguments)]
    (reset! server-state new-server-state)
    {:term    (new-server-state :current-term)
     :success result}))

