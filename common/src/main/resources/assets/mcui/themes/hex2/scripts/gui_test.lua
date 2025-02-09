local theme = require 'theme'
local wl = require 'widgetlib'
local menuSupport = require 'menu_support'
local dropdownSupport = require 'choice_dropdown_support'

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

    local menu = menuSupport.addMenu(root) or error('no menu')
    local page1w = menuSupport.addMenuPage(menu, 'p1', nil) or error('no page 1')
    local page2w = menuSupport.addMenuPage(menu, 'p2', 'p1') or error('no page 2')

    local page1 = page1w.getChildByName('content')
    local page2 = page2w.getChildByName('content')

    local b = wl.loadCenteredButton(page1, "Orange button", function(_, _, _, mb)
        print("Hit Orange with " .. mb)
        colourButtons("0xDEA51FFFL")
        return true
    end, 12, 100)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
    b = wl.loadCenteredButton(page1, "Pink button", function(_, _, _, mb)
        print("Hit Pink with " .. mb)
        colourButtons("0xDE1FBAFFL")
        return true
    end, 0, 120)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
    b = wl.loadCenteredButton(page1, "Blue button", function(_, _, _, mb)
        print("Hit Blue with " .. mb)
        colourButtons("0x1F85DEFFL")
        return true
    end, 12, 140)
    if b then
        table.insert(buttons, --[[---@type Widget]] b)
    end
    b = wl.loadCenteredButton(page1, "Page 2", function(self)
        self.setVariable('isHovered', wl.tstatic(false))
        menu.extra.open('p2')
        return true
    end, 0, 180)

    dropdownSupport.createDropdown(page2, {
        currentValue = 'default',
        key = 'myDropdown',
        xPos = 50,
        yPos = 50,
        options = {
            { key = 'one' },
            { key = 'two' },
            { key = 'three' },
        },
        setValue = function(newValue)
            print('Selected ' .. newValue.expression)
        end
    })
    menuSupport.addBackButton(page2w, {
        xPos = 50, yPos = 70
    })

    menu.extra.open('p1')
end

-- split to avoid tail call, which does not seem to work
local ok = theme.registerScreen("mcui:testgui", gui)
return ok
