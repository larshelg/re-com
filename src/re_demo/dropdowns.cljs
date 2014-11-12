(ns re-demo.dropdowns
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-com.util     :as    util]
            [re-com.core     :refer [button label input-text checkbox title]]
            [re-com.box      :refer [h-box v-box box gap]]
            [re-com.dropdown :refer [single-dropdown find-choice filter-choices-by-keyword single-dropdown-desc]]
            [cljs.core.async :refer [<! >! chan close! put! take! alts! timeout]]
            [re-demo.utils   :refer [panel-title component-title args-table]]
            [reagent.core    :as    reagent]))


(def demos [{:id 1 :label "Simple dropdown"}
            {:id 2 :label "Dropdown with grouping"}
            {:id 3 :label "Dropdown with filtering"}
            {:id 4 :label "Keyboard support"}
            {:id 5 :label "Other parameters"}
            {:id 6 :label "Two dependent dropdowns"}])


(def countries [{:id "au" :label "Australia"}
                {:id "us" :label "United States"}
                {:id "uk" :label "United Kingdom"}
                {:id "ca" :label "Canada"}
                {:id "nz" :label "New Zealand"}])


(def cities [{:id "01" :label "Sydney"       :country-id "au"}
             {:id "02" :label "Melbourne"    :country-id "au"}
             {:id "03" :label "Brisbane"     :country-id "au"}
             {:id "04" :label "Adelaide"     :country-id "au"}
             {:id "05" :label "Perth"        :country-id "au"}
             {:id "06" :label "Canberra"     :country-id "au"}
             {:id "07" :label "Hobart"       :country-id "au"}
             {:id "08" :label "Darwin"       :country-id "au"}
             {:id "09" :label "New York"     :country-id "us"}
             {:id "10" :label "Los Angeles"  :country-id "us"}
             {:id "11" :label "Dallas"       :country-id "us"}
             {:id "12" :label "Washington"   :country-id "us"}
             {:id "13" :label "Orlando"      :country-id "us"}
             {:id "14" :label "London"       :country-id "uk"}
             {:id "15" :label "Manchester"   :country-id "uk"}
             {:id "16" :label "Glasgow"      :country-id "uk"}
             {:id "17" :label "Brighton"     :country-id "uk"}
             {:id "18" :label "Birmingham"   :country-id "uk"}
             {:id "19" :label "Toronto"      :country-id "ca"}
             {:id "20" :label "Montreal"     :country-id "ca"}
             {:id "21" :label "Calgary"      :country-id "ca"}
             {:id "22" :label "Ottawa"       :country-id "ca"}
             {:id "23" :label "Edmonton"     :country-id "ca"}
             {:id "24" :label "Auckland"     :country-id "nz"}
             {:id "25" :label "Wellington"   :country-id "nz"}
             {:id "26" :label "Christchurch" :country-id "nz"}
             {:id "27" :label "Hamilton"     :country-id "nz"}
             {:id "28" :label "Dunedin"      :country-id "nz"}])



(def grouped-countries [{:id "AU" :label "Australia"                :group "EN Speakers"}
                        {:id "US" :label "United States"            :group "EN Speakers"}
                        {:id "GB" :label "United Kingdom"           :group "EN Speakers"}
                        {:id "E1" :label "Iraq"                     :group "Updated Axis Of Evil"}
                        {:id "E4" :label [:strong "New Zealand"]    :group "Updated Axis Of Evil"}
                        {:id "E2" :label "Iran"                     :group "Updated Axis Of Evil"}
                        {:id "E3" :label "North Korea"              :group "Updated Axis Of Evil"}
                        {:id "AF" :label "Afghanistan"              :group "'A' COUNTRIES"}
                        {:id "AB" :label "Albania"                  :group "'A' COUNTRIES"}
                        {:id "AG" :label "Algeria"                  :group "'A' COUNTRIES"}
                        {:id 06   :label "American Samoa"           :group "'A' COUNTRIES"}
                        {:id 07   :label "Andorra"                  :group "'A' COUNTRIES"}
                        {:id true :label "Angola"                   :group "'A' COUNTRIES"}
                        {:id [4]  :label "Anguilla"                 :group "'A' COUNTRIES"}
                        {:id "00" :label "Antarctica"               :group "'A' COUNTRIES"}
                        {:id "11" :label "Antigua and Barbuda"      :group "'A' COUNTRIES"}
                        {:id "12" :label "Argentina"                :group "'A' COUNTRIES"}
                        {:id "13" :label "Armenia"                  :group "'A' COUNTRIES"}
                        {:id "14" :label "Aruba"                    :group "'A' COUNTRIES"}
                        {:id "16" :label "Austria"                  :group "'A' COUNTRIES"}
                        {:id "17" :label "Azerbaijan"               :group "'A' COUNTRIES"}
                        {:id "18" :label "Bahamas"                  :group "'B' COUNTRIES"}
                        {:id "19" :label "Bahrain"                  :group "'B' COUNTRIES"}
                        {:id "20" :label "Bangladesh"               :group "'B' COUNTRIES"}
                        {:id "21" :label "Barbados"                 :group "'B' COUNTRIES"}
                        {:id "22" :label "Belarus"                  :group "'B' COUNTRIES"}
                        {:id "23" :label "Belgium"                  :group "'B' COUNTRIES"}
                        {:id "24" :label "Belize"                   :group "'B' COUNTRIES"}
                        {:id "25" :label "Benin"                    :group "'B' COUNTRIES"}
                        {:id "26" :label "Bermuda"                  :group "'B' COUNTRIES"}
                        {:id "27" :label "Bhutan"                   :group "'B' COUNTRIES"}
                        {:id "28" :label "Bolivia"                  :group "'B' COUNTRIES"}
                        {:id "29" :label "Bosnia and Herzegovina"   :group "'B' COUNTRIES"}
                        {:id "30" :label "Botswana"                 :group "'B' COUNTRIES"}
                        {:id "31" :label "Bouvet Island"            :group "'B' COUNTRIES"}
                        {:id "32" :label "Brazil"                   :group "'B' COUNTRIES"}
                        {:id "34" :label "Brunei Darussalam"        :group "'B' COUNTRIES"}
                        {:id "35" :label "Bulgaria"                 :group "'B' COUNTRIES"}
                        {:id "36" :label "Burkina Faso"             :group "'B' COUNTRIES"}
                        {:id "37" :label "Burundi"                  :group "'B' COUNTRIES"}])


(defn demo1
  []
  [:div
   [:p "The dropdown above is the simple case."]
   [:p "It presents a list of choices and allows one to be selected, via mouse or keyboard."]])


(defn demo2
  []
  (let [selected-country-id (reagent/atom nil)]
    (fn []
      [v-box
       :gap      "10px"
       :children [[:p "The dropdown below shows how related choices can be displayed in groups. In this case, several country related groups. e.g. 'EN COUNTRIES'."]
                  [:p "This feature is triggered if any choice has a :group attribute. Typically all choices will have a :group or none will. It's up to you to ensure that choices with the same :group are adjacent in the vector."]
                  [:p "Because :model is initially nil, the :placeholder text is initially displayed."]
                  [:p ":max-width is set here to make the dropdown taller."]
                  [:p ":label can be a string or arbitrary markup. Notice the boldness of the 'New Zealand'."]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices     grouped-countries
                               :model       selected-country-id
                               :placeholder "Choose a country"
                               :width       "300px"
                               :max-height  "400px"
                               :filter-box? false
                               :on-change   #(reset! selected-country-id %)]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (find-choice grouped-countries @selected-country-id)) " [" @selected-country-id "]"))]]]]])))


(defn demo3
  []
  (let [selected-country-id (reagent/atom "US")]
    (fn []
      [v-box
       :gap      "10px"
       :children [[:p "The dropdown below adds a filter text box to the dropdown section which is convenient for when there are many choices."]
                  [:p "The filter text is searched for in both the :group and the :label values. If the text matches the :group, then all
                       choices under that group are considered to be 'matched'."]
                  [:p "The initial model value has been set to 'US'."]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices     grouped-countries
                               :model       selected-country-id
                               :width       "300px"
                               :max-height  "400px"
                               :filter-box? true
                               :on-change   #(reset! selected-country-id %)]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (find-choice grouped-countries @selected-country-id)) " [" @selected-country-id "]"))]]]]])))


(defn demo4
  []
  (let [selected-country-id (reagent/atom "US")]
    (fn []
      [v-box
       :gap      "10px"
       :children [[:p "[single-dropdown ...] supports tab key navigation."]
                  [:p "The :tab-index parameter specifies position in the tab order,
                       or it can be removed from the tab order using a value of -1."]
                  [:p "Up-arrow and Down-arrow do sensible things."]
                  [:p "Home and End keys move to the beginning and end of the list."]
                  [:p "Enter, Tab and Shift+Tab trigger selection of the currently highlighted choice."]
                  [:p "Esc closes the dropdown without making a selection."]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[label :label "Test tabbing"]
                              [input-text
                               :model      ""
                               :on-change #()
                               :width     "80px"
                               ]
                              [single-dropdown
                               :choices     grouped-countries
                               :model       selected-country-id
                               :width       "300px"
                               :filter-box? true
                               :on-change   #(reset! selected-country-id %)]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (find-choice grouped-countries @selected-country-id)) " [" @selected-country-id "]"))]]]]])))


(defn demo5
  []
  (let [selected-country-id (reagent/atom "US")
        disabled?           (reagent/atom false)
        regex?              (reagent/atom false)
        width?              (reagent/atom false)
        dropdown-width      "300px"]
    (fn []
      [v-box
       :gap      "10px"
       :children [[:p "Experiment with the checkboxes below to understand the effect of other parameters."]
                  [h-box
                   :align    :center
                   :children [[checkbox
                               :label ":disabled?"
                               :model disabled?
                               :label-style {:width "100px"}
                               :on-change #(reset! disabled? %)]
                              [:span (str @disabled? " - " (if @disabled?
                                                             "the dropwdown is locked and cannot be changed."
                                                             "the dropdown is enabled and a choice can be selected."))]]]
                  [h-box
                   :align    :center
                   :children [[checkbox
                               :label ":regex-filter?"
                               :model regex?
                               :label-style {:width "100px"}
                               :on-change #(reset! regex? %)]
                              [:span (str @regex? " - " (if @regex?
                                                          "the filter text box supports JavaScript regular expressions."
                                                          "the filter text box supports plain text filtering only."))]]]
                  [h-box
                   :align    :center
                   :children [[checkbox
                               :label ":width"
                               :model width?
                               :label-style {:width "100px"}
                               :on-change #(reset! width? %)]
                              [:span (str (if @width?
                                            (str "\"" dropdown-width "\" - the dropdown is fixed to this width.")
                                            "not specified - the dropdown takes up all available width."))]]]
                  [gap :size "10px"]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices       grouped-countries
                               :model         selected-country-id
                               :disabled?     @disabled?
                               :filter-box?   true
                               :regex-filter? @regex?
                               :width         (when @width? dropdown-width)
                               :on-change     #(reset! selected-country-id %)]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (find-choice grouped-countries @selected-country-id)) " [" @selected-country-id "]"))]]]]])))


(defn demo6
  []
  (let [selected-country-id (reagent/atom nil)
        filtered-cities     (reagent/atom nil)
        selected-city-id    (reagent/atom nil)]
    (fn []
      [v-box
       :gap      "10px"
       :children [[:p "Two dropdowns can be tied together in a parent-child relationship. In this case, countries and their cities."]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices   countries
                               :model     selected-country-id
                               :width     "300px"
                               :on-change #(do
                                            (reset! selected-country-id %)
                                            (reset! filtered-cities (filter-choices-by-keyword cities :country-id @selected-country-id))
                                            (reset! selected-city-id nil))]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (find-choice countries @selected-country-id)) " [" @selected-country-id "]"))]]]
                  [gap :size "10px"]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices   filtered-cities
                               :model     selected-city-id
                               :width     "300px"
                               :on-change #(reset! selected-city-id %)]
                              [:div
                               [:strong "Selected city: "]
                               (if (nil? @selected-city-id)
                                 "None"
                                 (str (:label (find-choice cities @selected-city-id)) " [" @selected-city-id "]"))]]]]])))


(defn notes
  []
  [v-box
   :width    "400px"
   :style    {:font-size "small"}
   :children [[component-title  "[single-dropdown ... ]"]
              [ args-table   single-dropdown-desc]
              #_[:p "Required parameters:"]
              #_[:ul
               [:li.spacer [:code ":choices"] " - a vector of maps, one map for each choice. Each map contains a unique :id and a :label and can optionally include a :group."]
               [:li.spacer [:code ":model"] " - the :id of the initially selected choice, or nil to have no initial selection (in which case, :placeholder will be shown)."]
               [:li.spacer [:code ":on-change"] " - a function taking one parameter which will be the :id for the new choice."]]
              #_[:p "Optional:"]
              #_[:ul
               [:li.spacer [:code ":disabled?"] " - a boolean indicating whether the control should be disabled. Defaults to false."]
               [:li.spacer [:code ":filter-box?"] " - a boolean. If true a filter text box is available at the top of the dropped down section. Defaults to false."]
               [:li.spacer [:code ":regex-filter?"] " - a boolean. If true the filter text box will support JavaScript regular expressions. If false plain text searches.  Defaults to false."]
               [:li.spacer [:code ":placeholder"] " - the text to display in the dropdown if :model is nil."]
               [:li.spacer [:code ":width"] " - the width of the component (e.g. \"500px\"). If not specified, all available width is taken."]
               [:li.spacer [:code ":max-height"] " - maximum height the dropdown will grow to. Defaults to \"240px\"."]
               [:li.spacer [:code ":tab-index"] " - the tabindex number of this component. -1 to remove from tab order. Defaults to use natural tab order."]]]])


(defn panel
  []
  (let [selected-demo-id (reagent/atom 1)]
    (fn []
      [v-box
       :gap "10px"
       :children [[panel-title "Dropdown Components"]
                  [h-box
                   :gap      "50px"
                   :children [[notes]
                              [v-box
                               :gap       "15px"
                               :size      "auto"
                               :min-width "500px"
                               :margin    "20px 0px 0px 0px"
                               :children  [[h-box
                                            :gap      "10px"
                                            :align    :center
                                            :children [[label :label "Select a demo"]
                                                       [single-dropdown
                                                        :choices   demos
                                                        :model     selected-demo-id
                                                        :width     "300px"
                                                        :on-change #(reset! selected-demo-id %)]]]
                                           [gap :size "0px"] ;; Force a bit more space here
                                           (case @selected-demo-id
                                             1 [demo1]
                                             2 [demo2]
                                             3 [demo3]
                                             4 [demo4]
                                             5 [demo5]
                                             6 [demo6])]]]]]])))
