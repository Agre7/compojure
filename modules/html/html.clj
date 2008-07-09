(in-ns* 'html)

;;;;; HTML/XML domain specific language ;;;;;

(defn escape-html
  "Change special characters into HTML character entities."
  [s]
  (.. (str s) (replaceAll "&"  "&amp;")
              (replaceAll "<"  "&lt;")
              (replaceAll ">"  "&gt;")
              (replaceAll "\"" "&quot;")))

(defn- make-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[k v]]
      (str* " " k "=\"" (escape-html v) "\""))
    attrs))

(defn- make-tag
  "Create an XML tag given a name, attribute map, and seq of contents."
  [name attrs contents]
  (if contents
    (str* "<" name (make-attrs attrs) ">\n"
          (indent (apply str contents))
          "</" name ">\n")
    (str* "<" name (make-attrs attrs) " />\n")))

(defn tag
  "Generate an XML tag.
  e.g.
    (tag :em \"text\")
    (tag 'a :href \"#top\" \"Back to top\")"
  [name & contents]
  (let [[attrs contents] (kwargs contents)]
    (make-tag name attrs (flatten contents))))

(defn- add-tag-symbol
  "Add the `tag symbol to each list beginning with a keyword."
  [tree]
  (if (seq? tree)
    (tree-map
      #(if (keyword? (first %))
         (cons `tag %)
         %)
      tree)
    tree))

(defmacro xml
  "Any forms starting with a keyword in the body of this macro get an implicit
  tag function.
  e.g.
    (xml (:body (:p \"Hello World\")))"
  [& body]
  `(lines
     (list ~@(map add-tag-symbol body))))

(defmacro html
  "An alias for the xml macro."
  [& exprs]
  `(xml ~@exprs))

;;;;; Useful functions for generating HTML tags ;;;;;

(defn javascript-tag "A javascript HTML tag."
  [script]
  (tag :script :type "text/javascript" script))

(defn javascript-sources
  "Include external javascript sources."
  [sources]
  (str-map
    #(tag :script :type "text/javascript" :src % "")
    sources))

(defmacro htmldoc
  [& body]
  (let [[options body] (kwargs body)]
   `(html
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
      (:html
        (:head
          (:title ~(options :title))
          (javascript-sources
            '~(map #(str "/javascript/" % ".js") 
                    (options :javascript))))
        (:body
          ~@body)))))