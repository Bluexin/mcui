local label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/label_button.xml")
local icon_label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/icon_button_expanding_label.xml")

local function static(value)
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

local function tstatic(value, jtype)
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
    if (jtype == "STRING") then
        value = "\"" .. value .. "\""
    end
    return {
        type = jtype,
        expression = value,
        cache = "STATIC"
    }
end

local function tframe(value, jtype)
    local basic = tstatic(value, jtype)
    basic.cache = "PER_FRAME"
    return basic
end

--- @type Widget[]
local buttons = {}

local colourMask = ' & 0xAFAFAFFF'
local function colourButtons(colour)
    for _, v in ipairs(buttons) do
        local newColour = '(int) ' .. colour
        v.setVariable('colour', tstatic(newColour, 'INT'))
        local newHoverColour = '(int) (' .. colour .. colourMask .. ')'
        v.setVariable('hoverColour', tstatic(newHoverColour, 'INT'))
    end
end

local function loadCenteredButton(root, text, onClick, x, y)
    local r = theme.loadWidget(root, icon_label_button_frag, {
        text = tstatic(text, "STRING"),
        xPos = tframe("scaledwidth / 2 + " .. tostring(x), 'DOUBLE'),
        yPos = tstatic(y),
    })
    if (r == false) then
        print('Could not load button')
    else
        r.onClick = onClick
        table.insert(buttons, r)
    end
end

local function testGui(root)
    loadCenteredButton(root, "Orange button", function(_, _, _, mb)
        print("Hit Orange with " .. mb)
        colourButtons("0xDEA51FFFL")
        return true
    end, 12, 100)
    loadCenteredButton(root, "Pink button", function(_, _, _, mb)
        print("Hit Pink with " .. mb)
        colourButtons("0xDE1FBAFFL")
        return true
    end, 0, 120)
    loadCenteredButton(root, "Blue button", function(_, _, _, mb)
        print("Hit Blue with " .. mb)
        colourButtons("0x1F85DEFFL")
        return true
    end, 12, 140)
end

theme.registerScreen("mcui:testgui", testGui)
