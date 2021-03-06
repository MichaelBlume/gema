;::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
; GEMA - Extensible and modular software for Monte Carlo simulations          :
;                                                                             :
; Copyright (c) 2014 Centro Nacional de Investigaciones Cardiovasculares      :
;                       (http://www.cnic.es)                                  :
; Copyright (c) 2014 by CIBERES (http://www.ciberes.org)                      :
; All rights reserved. This program and the accompanying materials            :
; are made available under the terms of the Eclipse Public License v1.        :
; which accompanies this distribution, and is available at                    :
; http://www.eclipse.org/legal/epl-v10.html                                   :
;                                                                             :
; Contributors:                                                               :
;    Jose M. Perez-Sanchez (CIBERES) - Initial implementation                 :
;::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


;:::::::::::::::::::::::::::::::::::::::::::
; Namespace definition and module import   :
;:::::::::::::::::::::::::::::::::::::::::::

(ns gema.models.segment
  (:require [gema.utils :as utils])
  (:import [cern.jet.random.engine MersenneTwister]
           [cern.jet.random Uniform Normal]))

;:::::::::::::::::::::::::
; Function definitions   :
;:::::::::::::::::::::::::

(defn check-model-pars
  "Returns true if pars are compatible with the model, false otherwise"
  [simul-info pars]
  (if (contains? pars :l) (> (pars :l) 0.0) true))

(defn build-model-info
  "Returns map with model info from input parameters"
  [simul-info pars]
  (let [l (if (contains? pars :l) (pars :l) 1.0)
        x-min (* l -0.5)
        x-max (* l 0.5)]
    {:dim 1 :l l :x-min x-min :x-max x-max}))

(defn create-pdf
  "Returns map containing the Probability Density Functions for the model"
  [simul-info model-info seed]
  (let [x-min (model-info :x-min)
        x-max (model-info :x-max)
        step-len (simul-info :step-len)
        mt19937 (MersenneTwister. ^int seed)
        pdf-uniform (Uniform. ^double x-min ^double x-max ^cern.jet.random.engine.RandomEngine mt19937)
        pdf-normal (Normal. 0.0 step-len mt19937)] 
    {:gen mt19937 :init-pdf pdf-uniform :step-pdf pdf-normal}))

(defn get-displacement
  "Returns a vector with a candidate displacement"
  [simul-info model-info model-pdfs from-point]
  (utils/get-rand-1 (model-pdfs :step-pdf)))

(defn check-displacement
  "Returns the state the particle would have if displacement specified by the 
  argument `disp` would be performed.  State can be any non-nil value. It 
  should return nil if the displacement is incompatible with the model. This 
  functions acts thus as a condition for selecting valid displacements, since 
  displacements for which it returns `nil` never happen. For each particle, 
  the simulation engine keeps track of the state returned by this function and 
  passes the value returned for the displacement corresponding to the last 
  step, as argument `state`, each time this function is called."
  [simul-info model-info from-point ^doubles with-disp ^doubles new-pos]
  (let [x-min (model-info :x-min)
        x-max (model-info :x-max)
        x-next (first new-pos)]
    (if (and (> x-next x-min)
         (< x-next x-max))
      (:state from-point)
      nil)))

(defn get-initpos
  "Returns a vector with a candidate for initial position"
  [simul-info model-info model-pdfs]
  (utils/get-rand-1 (model-pdfs :init-pdf)))

(defn check-initpos
  "Returns the initial state the particle would have if it gets assigned the 
  initial position specified by the argument `pos`. The return value should 
  fulfill the same rules as in the `check-displacement` function."
  [simul-info model-info ^doubles pos]
  (check-displacement simul-info model-info {:step 0 :state 0 :pos (double-array 1 0.0)} pos pos))

;::::::::::::::::
; End of file   :
;::::::::::::::::
