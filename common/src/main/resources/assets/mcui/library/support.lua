--- @module mouse_buttons
mouse_buttons = {
    LEFT = 0,
    RIGHT = 1,
    MIDDLE = 2,
    BACK = 3,
    FORWARD = 4
}

--- @alias CacheType 'PER_FRAME' | 'STATIC' | 'SIZE_CHANGE' | 'NONE'
--- @alias JelType 'STRING' | 'DOUBLE' | 'INT' | 'BOOLEAN' | 'UNIT' | 'ERROR'

--- @shape TypeSpecificCValue
--- @field expression string
--- @field cache CacheType

--- Represents a MiniScript value computing to generic type
--- @shape CValue: TypeSpecificCValue
--- @field type JelType

--- Represents a MiniScript value computing to int type
--- @alias CInt CValue|string|number

--- Represents a MiniScript value computing to boolean type
--- @alias CBoolean CValue|string|number

--- Represents a MiniScript value computing to double type
--- @alias CDouble CValue|string|number

--- Represents a MiniScript value computing to string type
--- @alias CString CValue|string|number

--- Represents a MiniScript value computing to void type
--- @alias CUnit CValue|string|number

--- Minecraft ResourceLocation
--- @shape LKResourceLocation
--- @field namespace string
--- @field path string
--- @field string string text representation in namespace:path format

---@param modname string
---@param themeId? LKResourceLocation
---@return any
function require(modname, themeId)
    return {}
end
