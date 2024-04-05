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
--- @field expression string|number
--- @field cache CacheType

--- Represents a MiniScript value computing to generic type
--- @shape CValue: TypeSpecificCValue
--- @field type JelType

--- Represents a MiniScript value computing to int type
--- @alias CInt TypeSpecificCValue|string|number

--- Represents a MiniScript value computing to boolean type
--- @alias CBoolean TypeSpecificCValue|string|number

--- Represents a MiniScript value computing to double type
--- @alias CDouble TypeSpecificCValue|string|number

--- Represents a MiniScript value computing to string type
--- @alias CString TypeSpecificCValue|string|number

--- Represents a MiniScript value computing to void type
--- @alias CUnit TypeSpecificCValue|string|number
