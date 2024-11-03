--- @module widgetlib
local wl = {}

local theme = require 'theme'
local util = require 'util'

--- @param parent Widget|nil
--- @param name string
function wl.getChildWidget(parent, name)
    if parent then
        local child = (--[[---@type Widget]] parent).getChildByName(name)
        if child and type(child) == 'Widget' then
            return --[[---@type Widget]] child
        end
    end
    error('No child widget with name ' .. name .. " on " .. ((parent and (--[[---@type Widget]] parent).name) or 'missing parent'))
end

--- @param categoryContent Widget
--- @param n number amount of children
function wl.centerCategoryContent(categoryContent, n)
    if n > 2 then
        categoryContent.y = -20 * math.floor((n - 1) / 2)
    end
    local maxWidth = -1
    local children = categoryContent.allChildren
    for _, v in ipairs(children) do
        if type(v) == 'Widget' then
            local width = tonumber(v.getVariable('initialWidth').expression)
            --print('Checking width of ' .. util.tprint(v.getVariable('initialWidth')) .. ' in ' .. v.name)
            if width > maxWidth then
                maxWidth = width
            end
        end
    end
    --print('Resizing ' .. tostring(n) .. ' children of ' .. categoryContent.hierarchyName .. ' to a width of ' .. maxWidth)
    if (maxWidth > 0) then
        for _, v in ipairs(children) do
            if type(v) == 'Widget' then
                v.setVariable('initialWidth', wl.tstatic(maxWidth, 'INT'))
            end
        end
    end
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

local label_button_frag = theme.readWidget("saoui.sao:label_button")

--- @shape buttonArgs
--- @field xPos string|number|CDouble
--- @field yPos string|number|CDouble
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
    wl.bind(baseArgs, 'initialWidth', args.width or 160, wl.tstatic, 'DOUBLE')

    local allArgs = util.merge(
            baseArgs,
            --- @type table<string, CValue>
            args.variables or {}
    )

    local r = theme.loadWidget(parent, label_button_frag, allArgs)
    if (not r) then
        print('Could not load button ' .. args.label)
    else
        local w = --[[---@type Widget]] r
        if (args.tooltip) then
            w.tooltip = wl.fromArg(args.tooltip, wl.tstatic, 'STRING')
        end

        if args.onClick then
            w.onClick = args.onClick
        end
    end
    return r
end

local category_frag = theme.readWidget("saoui.sao:category_label_button")

--- @param parent string|Widget
--- @param yPos string|number
--- @param xPos string|number
--- @param label string
--- @param display? fun(id: string): string
--- @return Widget|nil
function wl.loadCategory(parent, yPos, xPos, label, display)
    category_frag.name = 'cat_' .. label:lower():gsub(':', '_')
    local r = theme.loadWidget(parent, category_frag, {
        text = wl.tstatic((display and display(label)) or '"' .. label .. '"', "STRING", true),
        xPos = wl.tframe(xPos, 'DOUBLE'),
        yPos = wl.tframe(yPos, "DOUBLE"),
    })
    if (r == false) then
        print('Could not load category ' .. label)
        return nil
    end
    return --[[---@type Widget]] r
end

return wl
