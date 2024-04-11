local theme = require("theme")
local settings = require('settings')

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

--- @param value string|number
--- @param jtype? JelType|nil
--- @param nowrap? boolean
--- @return CValue
local function tstatic(value, jtype, nowrap)
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
    return {
        type = jtype or 'ERROR',
        expression = value,
        cache = 'STATIC'
    }
end

--- @param value string|number
--- @param jtype? JelType
--- @return CValue
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

local label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/label_button.xml")
local icon_label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/icon_button_expanding_label.xml")
local category_frag = theme.readWidget("mcui:themes/hex2/widgets/category_label_button.xml")

local function loadCenteredButton(root, text, onClick, x, y)
    local r = theme.loadWidget(root, icon_label_button_frag, {
        text = tstatic(text, "STRING"),
        xPos = tframe("scaledwidth / 2 + " .. tostring(x), 'DOUBLE'),
        yPos = tstatic(y),
    })
    if (r == false) then
        print('Could not load button')
    else
        local button = --[[---@type Widget]] r
        button.onClick = onClick
        table.insert(buttons, button)
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

--- @param tbl table
--- @param indent? number
local function tprint (tbl, indent)
    if not indent then
        indent = 0
    end
    local indent = --[[---@type number]] indent
    local toprint = "{\r\n"
    indent = indent + 2
    for k, v in pairs(tbl) do
        toprint = toprint .. string.rep(" ", indent)
        if (type(k) == "number") then
            toprint = toprint .. "[" .. k .. "] = "
        elseif (type(k) == "string") then
            toprint = toprint .. k .. " = "
        end
        if (type(v) == "number") then
            toprint = toprint .. v .. ",\r\n"
        elseif (type(v) == "string") then
            toprint = toprint .. "\"" .. v .. "\",\r\n"
        elseif (type(v) == "table") then
            toprint = toprint .. tprint(v, indent) .. ",\r\n"
        else
            toprint = toprint .. "\"" .. tostring(v) .. "\",\r\n"
        end
    end
    toprint = toprint .. string.rep(" ", indent - 2) .. "}"
    return toprint
end

--- @generic T
--- @param t table<string, T>
--- @param key string
--- @return T
local function getOrCreate(t, key)
    if (t[key] == nil) then
        t[key] = --[[---@type T]] {}
    end
    return t[key]
end

--- @param parent string|Widget
--- @param yPos string|number
--- @param xPos string|number
--- @param label string
--- @param display? fun(id: string): string
--- @return Widget|nil
local function loadCategory(parent, yPos, xPos, label, display)
    category_frag.name = 'cat_' .. string.gsub(string.lower(label), ':', '_')
    local r = theme.loadWidget(parent, category_frag, {
        text = tstatic((display and display(label)) or '"' .. label .. '"', "STRING", true),
        xPos = tframe(xPos, 'DOUBLE'),
        yPos = tframe(yPos, "DOUBLE"),
    })
    if (r == false) then
        print('Could not load category')
        return nil
    else
        print('Loaded ' .. label .. ' into ' .. tostring(parent) .. " at " .. tostring(xPos) .. ' / ' .. tostring(yPos))
    end
    return --[[---@type Widget]] r
end

--- @param parent Widget
--- @param yPos string|number
--- @param xPos string|number
--- @param label string
--- @return Widget|nil
local function loadSetting(parent, yPos, xPos, label)
    local r = theme.loadWidget(parent, label_button_frag, {
        text = tstatic(label, 'STRING'),
        xPos = tframe(xPos, 'DOUBLE'),
        yPos = tframe(yPos, 'DOUBLE'),
        initialWidth = tstatic(160, 'INT'),
    })
    if (r == false) then
        print('Could not load category')
        return nil
    end
    return --[[---@type Widget]] r
end

--- @param parent Widget|nil
--- @param name string
local function getChildWidget(parent, name)
    if parent ~= nil then
        local child = (--[[---@type Widget]] parent).getChildByName(name)
        if child ~= nil and type(child) == 'Widget' then
            return --[[---@type Widget]] child
        end
    end
    error('No child widget with name ' .. name .. " on " .. ((parent and (--[[---@type Widget]] parent).name) or 'missing parent'))
end

--- @param categoryContent Widget
--- @param n number amount of children
local function centerCategoryContent(categoryContent, n)
    if n > 2 then
        categoryContent.y = -20 * math.floor((n - 1) / 2)
    end
end

--- @param id string ResourceLocation.string
local function settingsTabName(id)
    return 'format("mcui.screen.settings.theme", format("mcui.theme.' .. string.gsub(id, ':', '.') .. '.name"))'
end

--- @param id string ResourceLocation.string
local function settingsCategoryName(id)
    return 'format("mcui.screen.settings.' .. string.gsub(id, ':', '.') .. '")'
end

theme.registerScreen("mcui:settings", function(root)
    --- @type table<string, table<string, table<string, Setting>>>
    local topCategories = {}
    for i, v in ipairs(settings.listAll()) do
        print("Found setting " .. v.namespace.string .. " / " .. v.key.string)

        local topLevel = getOrCreate(topCategories, v.namespace.string)
        local category = getOrCreate(topLevel, v.namespace.string .. '/' .. v.key.namespace)
        category[v.key.path] = v
    end

    print("Organised settings : ")
    print(tprint(topCategories))

    local yPosBase = 'scaledheight / 2 - 10 + '
    local yPos = 0

    for topLevelName, topLevelValue in pairs(topCategories) do
        local topLevel = loadCategory(root, yPosBase .. tostring(yPos), "scaledwidth / 2 - 120", topLevelName, settingsTabName)
        if topLevel ~= nil then
            local tlN = 0
            local tlContent = getChildWidget(topLevel, 'content')
            for catName, catValue in pairs(topLevelValue) do
                local category = loadCategory(tlContent, tlN * 20, 0, catName, settingsCategoryName)
                local catN = 0
                if category ~= nil then
                    local catContent = getChildWidget(category, 'content')
                    for settingName, setting in pairs(catValue) do
                        loadSetting(catContent, catN * 20, 0, settingName .. ': ' .. type(setting))
                        catN = catN + 1
                    end
                    centerCategoryContent(catContent, catN)
                end
                tlN = tlN + 1
            end
            centerCategoryContent(tlContent, tlN)
        end
        yPos = yPos + 20
    end
end)
