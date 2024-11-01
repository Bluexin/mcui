--- @module choice_dropdown_support
local cds = {}

local theme = require 'theme'
local util = require 'util'
local wl = require 'widgetlib'

local dropdown_frag = theme.readWidget("mcui.hex2:choice_dropdown")
local option_frag = theme.readWidget("mcui.hex2:choice_option")

--- @param self Widget
--- @param setValue nil|fun(newValue: CValue): void
local function setValueFn(self, setValue)
    --- @param newValue CValue
    self.extra.setValue = function(newValue)
        self.setVariable('isOpen', wl.tstatic(false))
        self.setVariable('currentValue', wl.fromArg(newValue, wl.tstatic, 'STRING'))
        if setValue then
            setValue(newValue)
        end
    end
end

--- @param root Widget|string
--- @param args dropdownArgs
--- @return Widget|boolean
local function loadDropdown(root, args)
    --- @type table<string, CValue>
    local baseArgs = {}

    local label = args.label or wl.tstatic('format("mcui.' .. args.key .. '.label")', 'STRING', true)
    wl.bind(baseArgs, 'label', label, wl.tstatic, 'STRING')
    wl.bind(baseArgs, 'currentValue', args.currentValue, wl.tstatic, 'STRING')
    wl.bind(baseArgs, 'xPos', args.xPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'yPos', args.yPos, wl.tstatic, 'DOUBLE')
    wl.bind(baseArgs, 'initialWidth', args.width or 160, wl.tstatic, 'INT')

    local allArgs = util.merge(
            baseArgs,
            --- @type table<string, CValue>
            args.variables or {}
    )

    local r = theme.loadWidget(root, dropdown_frag, allArgs)
    if not r then
        print('Could not load button ' .. args.label)
    else
        local w = --[[---@type Widget]] r
        setValueFn(w, args.setValue)
        if args.tooltip then
            w.tooltip = wl.fromArg(args.tooltip, wl.tstatic, 'STRING')
        end

        if args.key then
            w.name = args.key
        end
    end
    return r
end

--- @param parent Widget the 'content' of the dropdown widget
--- @param args dropdownOption
--- @param index number
local function loadDropdownOption(parent, args, index)
    --- @type table<string, CValue>
    local baseArgs = {
        thisValue = wl.tstatic(args.key),
        yPos = wl.tstatic((index - 1) * 16)
    }

    if args.labelLocalization then
        baseArgs['translateValue'] = args.labelLocalization
    end
    if args.tooltipLocalization then
        baseArgs['valueTooltip'] = args.tooltipLocalization
    end

    local allArgs = util.merge(
            baseArgs,
            --- @type table<string, CValue>
            args.variables or {}
    )

    local r = theme.loadWidget(parent, option_frag, allArgs)
    if r then
        r.name = 'option_' .. args.key:gsub(':', '.')
    end
end

--- @shape dropdownArgs
--- @field currentValue string
--- @field key string mapped to the widget's name
--- @field xPos? string|number|CDouble
--- @field yPos? string|number|CDouble
--- @field width number|nil
--- @field label string|CString|nil
--- @field tooltip string|CString|nil
--- @field setValue nil|fun(newValue: CValue): void
--- @field variables table<string, CValue>|nil
--- @field options dropdownOption[]

--- @shape dropdownOption
--- @field key string
--- @field labelLocalization CValue|nil
--- @field tooltipLocalization CValue|nil
--- @field variables table<string, CValue>|nil

--- @param parent Widget
--- @param args dropdownArgs
--- @return Widget|boolean
function cds.createDropdown(parent, args)
    local dropdown = loadDropdown(parent, args)
    if dropdown then
        local parentContent = wl.getChildWidget(--[[---@type Widget]] dropdown, 'content')
        for index, option in ipairs(args.options) do
            loadDropdownOption(parentContent, option, index)
        end
    end

    return dropdown
end

return cds
