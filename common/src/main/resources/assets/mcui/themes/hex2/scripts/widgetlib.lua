--- @module widgetlib
local wl = {}

local theme = require 'theme'

function wl.static(value)
    if (type(value) == "string") then
        value = "\"" .. value .. "\""
    else
        value = tostring(value)
    end
    return {
        expression = value,
        cache = "STATIC"
    }
end

--- @param value string|number|boolean
--- @param jtype? JelType|nil
--- @param nowrap? boolean
--- @return CValue
function wl.tstatic(value, jtype, nowrap)
    if (not jtype or type(jtype) ~= "string") then
        local vtype = type(value)
        if (vtype == "string") then
            jtype = "STRING"
        elseif (vtype == "boolean") then
            jtype = "BOOLEAN"
        elseif (vtype == "number") then
            jtype = "DOUBLE"
        end
    end
    if (type(value) ~= "string") then
        value = tostring(value)
    end
    if (jtype == "STRING" and not nowrap) then
        value = "\"" .. value .. "\""
    end
    return --[[---@type CValue]] {
        type = jtype or 'ERROR',
        expression = --[[---@type string]] value,
        cache = 'STATIC'
    }
end

--- @param value string|number
--- @param jtype? JelType
--- @param nowrap? boolean
--- @return CValue
function wl.tframe(value, jtype, nowrap)
    local basic = wl.tstatic(value, jtype, nowrap)
    basic.cache = "PER_FRAME"
    return basic
end

local icon_label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/icon_button_expanding_label.xml")

--- @param root string|Widget root id of a Screen, or reference to an already loaded Widget
--- @param text string
--- @param onClick fun(arg: Widget, arg: number, arg: number, arg: number): boolean
--- @param x number
--- @param y number
--- @return Widget|boolean
function wl.loadCenteredButton(root, text, onClick, x, y)
    local r = theme.loadWidget(root, icon_label_button_frag, {
        text = wl.tstatic(text, "STRING"),
        xPos = wl.tframe("scaledwidth / 2 + " .. tostring(x), 'DOUBLE'),
        yPos = wl.tstatic(y),
    })
    if (r == false) then
        print('Could not load button')
    else
        local button = --[[---@type Widget]] r
        button.onClick = onClick
    end
    return r
end

return wl
