

one widget that handles its own layout.  (? screen type instead)

components
1. definitions for where things appear on screen, their size, and visibility
2. handlers for interactive pieces
3. renderers for displaying

example definitions 
```json
{
  "class": "grid",
  "columns": 2,
  "rows": 2,
  "children": [
    {
      "row": [0],
      "column": [0],
      "size_function": {"class":"fill"},
      "child": {
        "class": "string_widget",
        "test": "Hello World"
      }
    },
    {
      "row": [0],
      "column": [1],
      "size_function": {"class": "static", "width": 30, "height": 12},
      "child": {
        "class": "button",
        "text": "Click Me!",
        "function": "mod:do_stuff"
      }
    },
    {
      "row": [1],
      "column": [0,1],
      "size_function": {"class": "fill"},
      "child": {
        "class": "grid",
        "columns": 1,
        "rows": 1,
        "children": [
          {
            "class": "scroll",
            "content_function": "pmmo:glossary"
          }
        ]
      }
    }
  ]
}
```

Still have no idea how to do callbacks 