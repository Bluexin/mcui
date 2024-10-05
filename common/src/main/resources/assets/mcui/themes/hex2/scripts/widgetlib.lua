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

local label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/label_button.xml")

--- @shape buttonArgs
--- @field key string|nil mapped to the widget's name
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
    if not r then
        print('Could not load button ' .. args.label)
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

--- @param self Widget
local function onClickBackButton(self)
    local cat = --[[--- @type Widget]] self.parentElement.parentElement

    cat.setVariable('isOpen', wl.tstatic(false))
    if (cat.extra.onClose) then
        cat.extra.onClose()
    end

    --- @type Element[]
    local peers = cat.peers
    for _, peer in ipairs(peers) do
        if type(peer) == 'Widget' then
            peer.setVariable('isPeerOpen', wl.tstatic(false))
        end
    end
    return true
end

local category_frag = theme.readWidget("mcui:themes/hex2/widgets/category_label_button.xml")

--- @param parent string|Widget
--- @param yPos string|number
--- @param xPos string|number
--- @param label string
--- @param display? fun(id: string): string
--- @param noBackButton? boolean
--- @return Widget|nil
function wl.loadCategory(parent, yPos, xPos, label, display, noBackButton)
    local r = theme.loadWidget(parent, category_frag, {
        text = wl.tstatic((display and display(label)) or '"' .. label .. '"', "STRING", true),
        xPos = wl.tframe(xPos, 'DOUBLE'),
        yPos = wl.tframe(yPos, "DOUBLE"),
    })
    if not r then
        print('Could not load category ' .. label)
        return nil
    else
        local w = --[[---@type Widget]] r
        w.name = 'cat_' .. label:lower():gsub(':', '_')

        if not noBackButton then
            local content = wl.getChildWidget(w, 'content')
            local bk = w.name .. '_back'
            local backButton = wl.loadButton(content, {
                key = bk,
                xPos = 0,
                yPos = -20,
                label = wl.tstatic('format("mcui.screen.back")', 'STRING', true),
                onClick = onClickBackButton,
                tooltip = bk,
                variables = {
                    isPeerOpen = wl.tstatic(false),
                },
            })
            if backButton then
                (--[[---@type Widget]] backButton).enabled = wl.tframe('!isPeerOpen', 'BOOLEAN')
            end
        end
    end

    return --[[---@type Widget]] r
end

return wl
