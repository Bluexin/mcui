local theme = require 'theme'
local wl = require 'widgetlib'

--- @type Widget[]
local buttons = {}

local colourMask = ' & 0xAFAFAFFF'
local function colourButtons(colour)
    for _, v in ipairs(buttons) do
        local newColour = '(int) ' .. colour
        v.setVariable('colour', wl.tstatic(newColour, 'INT'))
        local newHoverColour = '(int) (' .. colour .. colourMask .. ')'
        v.setVariable('hoverColour', wl.tstatic(newHoverColour, 'INT'))
    end
end

local function gui(root)
    local b = wl.loadCenteredButton(root, "Orange button", function(_, _, _, mb)
        print("Hit Orange with " .. mb)
        colourButtons("0xDEA51FFFL")
        return true
    end, 12, 100)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
    b = wl.loadCenteredButton(root, "Pink button", function(_, _, _, mb)
        print("Hit Pink with " .. mb)
        colourButtons("0xDE1FBAFFL")
        return true
    end, 0, 120)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
    b = wl.loadCenteredButton(root, "Blue button", function(_, _, _, mb)
        print("Hit Blue with " .. mb)
        colourButtons("0x1F85DEFFL")
        return true
    end, 12, 140)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
end

-- split to avoid tail call, which does not seem to work
local ok = theme.registerScreen("mcui:testgui", gui)
return ok
