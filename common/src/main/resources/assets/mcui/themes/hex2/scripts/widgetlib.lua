--- @module widgetlib
local wl = {}

local theme = require 'theme'
local util = require 'util'

--- @param parent Widget|nil
--- @param name string
--- @return Widget
function wl.getChildWidget(parent, name)
    if parent then
        local child = (--[[---@type Widget]] parent).getChildByName(name)
        if child and type(child) == 'Widget' then
            return --[[---@type Widget]] child
        end
    end
    return error('No child widget with name ' .. name .. " on " .. ((parent and (--[[---@type Widget]] parent).name) or 'missing parent'))
end

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
--- @param jtype? JelType
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
    basic.cache = 'PER_FRAME'
    return basic
end

local icon_label_button_frag = theme.readWidget("mcui.hex2:icon_button_expanding_label")

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
    if (not r) then
        print('Could not load button')
    else
        local button = --[[---@type Widget]] r
        button.onClick = onClick
    end
    return r
end

--- @param arg string|number|boolean|CValue
--- @param default fun(value: string|number|boolean, jtype: JelType|nil, nowrap: boolean): CValue
--- @param valueType JelType
--- @return CValue
function wl.fromArg(arg, default, valueType)
    if (type(arg) == 'table') then
        return --[[---@type CValue]] arg
    else
        return default(--[[---@type string|number|boolean]] arg, valueType, false)
    end
end

--- @param self table<string, CValue>
--- @param key string
--- @param source string|number|boolean|CValue|nil
--- @param default fun(value: string|number|boolean, jtype: JelType|nil, nowrap: boolean): CValue
--- @param valueType JelType
function wl.bind(self, key, source, default, valueType)
    if (source) then
        self[key] = wl.fromArg(source, default, valueType)
    end
end

local label_button_frag = theme.readWidget("mcui.hex2:label_button")

--- @class buttonArgs
--- @field key string|nil mapped to the widget's name
--- @field xPos string|number|CDouble|nil
--- @field yPos string|number|CDouble|nil
--- @field width number|nil
--- @field label string|CString|nil
--- @field tooltip string|CString|nil
--- @field onClick nil|fun(self: Widget, mouseX: number, mouseY: number, mouseButton: number): boolean
--- @field variables table<string, CValue>|nil

--- @param parent Widget|string
--- @param args buttonArgs
--- @return Widget|boolean
function wl.loadButton(parent, args)
    --- @type table<string, CValue>
    local baseArgs = {}

    wl.bind(baseArgs, 'text', args.label, wl.tstatic, 'STRING')
    wl.bind(baseArgs, 'xPos', args.xPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'yPos', args.yPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'initialWidth', args.width or 160, wl.tstatic, 'INT')

    local allArgs = util.merge(
            baseArgs,
            --- @type table<string, CValue>
            args.variables or {}
    )

    local r = theme.loadWidget(parent, label_button_frag, allArgs)
    if not r then
        print('Could not load button ' .. args.label.expression)
    else
        local w = --[[---@type Widget]] r
        if (args.tooltip) then
            w.tooltip = wl.fromArg(args.tooltip, wl.tstatic, 'STRING')
        end

        if args.onClick then
            w.onClick = args.onClick
        end

        if args.key then
            w.name = args.key
        end
    end
    return r
end

local cancel_apply_button_frag = theme.readWidget("mcui.hex2:cancel_apply_buttons")

--- @param parent Widget|string
--- @return Widget|boolean
function wl.loadCancelApplyButtons(parent)
    local r = theme.loadWidget(parent, cancel_apply_button_frag, {
        yPos = wl.tstatic(-10)
    })
    if not r then
        print('Could not load cancel/apply buttons for ' .. parent.hierarchyName)
    end
    return r
end

return wl
