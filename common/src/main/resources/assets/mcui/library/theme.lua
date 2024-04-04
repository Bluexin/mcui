--- @module theme
theme = {}

---
--- Reads the Fragment pointed towards by the resource location made from given
--- namespace and path.
--- If only one parameter is given, it will be considered to be the full resource
--- location in `namespace:path` format
---@overload fun(resourceLocation:string):table
---@param namespace string
---@param path string
---@return table read fragment
function theme.readFragment(namespace, path) return {} end
---
--- Loads the Fragment into the target's children, optionally using variables to
--- evaluate the Fragment.
---@overload fun(target:string,fragment:table):boolean
---@param target string
---@param fragment table as returned by readFragment
---@param variables table containing variables to use when evaluating the fragment
---@return Fragment|boolean the fragment if successful, false otherwise
function theme.loadFragment(target, fragment, variables) return false end

---
--- Reads the Widget pointed towards by the resource location made from given
--- namespace and path.
--- If only one parameter is given, it will be considered to be the full resource
--- location in `namespace:path` format
---@overload fun(resourceLocation:string):table
---@param namespace string
---@param path string
---@return table read widget
function theme.readWidget(namespace, path) return {} end
---
--- Loads the Widget into the target's children, optionally using variables to
--- evaluate the Widget.
---@overload fun(target:string,fragment:table):boolean
---@param target string
---@param widget table as returned by readWidget
---@param variables table containing variables to use when evaluating the fragment
---@return Widget|boolean the widget if successful, false otherwise
function theme.loadWidget(target, widget, variables) return false end

---
--- Registers given callback for initializing Screen with given id
---@param id string
---@param callback fun(rootId: string) receiving the root for manipulating the Screen
---@return boolean whether the operation was successful
function theme.registerScreen(id, callback) return false end

return theme
