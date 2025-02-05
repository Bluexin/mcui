--- @module checkbox_support
local cs = {}

local theme = require 'theme'
local util = require 'util'
local wl = require 'widgetlib'

local checkbox_frag = theme.readWidget("mcui.hex2:checkbox_button")

--- @param root Widget|string
--- @param args checkboxArgs
--- @return Widget|boolean
local function loadCheckbox(root, args)
    --- @type table<string, CValue>
    local baseArgs = {}

    local label = args.label or wl.tstatic('format("mcui.' .. args.key .. '.label")', 'STRING', true)
    wl.bind(baseArgs, 'label', label, wl.tstatic, 'STRING')
    wl.bind(baseArgs, 'currentValue', args.currentValue, wl.tstatic, 'BOOLEAN')
    wl.bind(baseArgs, 'xPos', args.xPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'yPos', args.yPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'initialWidth', args.width or 160, wl.tstatic, 'INT')

    local allArgs = util.merge(
            baseArgs,
    --- @type table<string, CValue>
            args.variables or {}
    )

    local r = theme.loadWidget(root, checkbox_frag, allArgs)
    if not r then
        print('Could not load button ' .. args.label)
    else
        local w = --[[---@type Widget]] r
        if args.tooltip then
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

--- @class checkboxArgs
--- @field currentValue boolean
--- @field key string mapped to the widget's name
--- @field xPos string|number|CDouble
--- @field yPos string|number|CDouble
--- @field width number|nil
--- @field label string|CString|nil
--- @field tooltip string|CString|nil
--- @field setValue nil|fun(newValue: CValue): void
--- @field onClick nil|fun(self: Widget, mouseX: number, mouseY: number, mouseButton: number): boolean
--- @field variables table<string, CValue>|nil

--- @param parent Widget
--- @param args checkboxArgs
--- @return Widget|boolean
function cs.createCheckbox(parent, args)
    return loadCheckbox(parent, args)
end

return cs
