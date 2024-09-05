local theme = require 'theme'
local settings = require 'settings'
local wl = require 'widgetlib'
local util = require 'util'

local label_button_frag = theme.readWidget("mcui:themes/hex2/widgets/label_button.xml")
local category_frag = theme.readWidget("mcui:themes/hex2/widgets/category_label_button.xml")

--- @param parent string|Widget
--- @param yPos string|number
--- @param xPos string|number
--- @param label string
--- @param display? fun(id: string): string
--- @return Widget|nil
local function loadCategory(parent, yPos, xPos, label, display)
    category_frag.name = 'cat_' .. string.lower(label):gsub(':', '_')
    local r = theme.loadWidget(parent, category_frag, {
        text = wl.tstatic((display and display(label)) or '"' .. label .. '"', "STRING", true),
        xPos = wl.tframe(xPos, 'DOUBLE'),
        yPos = wl.tframe(yPos, "DOUBLE"),
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
--- @param tooltip string
--- @return Widget|nil
local function loadSetting(parent, yPos, xPos, label, tooltip)
    local r = theme.loadWidget(parent, label_button_frag, {
        text = wl.tstatic(label, 'STRING'),
        xPos = wl.tframe(xPos, 'DOUBLE'),
        yPos = wl.tframe(yPos, 'DOUBLE'),
        initialWidth = wl.tstatic(160, 'INT'),
    })
    if (r == false) then
        print('Could not load category')
        return nil
    end
    local w = --[[---@type Widget]] r
    w.tooltip = wl.tstatic(tooltip)
    return w
end

--- @param parent Widget|nil
--- @param name string
local function getChildWidget(parent, name)
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
local function centerCategoryContent(categoryContent, n)
    if n > 2 then
        categoryContent.y = -20 * math.floor((n - 1) / 2)
    end
end

--- @param id string ResourceLocation.string
local function settingsTabName(id)
    return 'format("mcui.screen.settings.theme", format("mcui.theme.' .. id:gsub(':', '.') .. '.name"))'
end

--- @param id string ResourceLocation.string
local function settingsCategoryName(id)
    return 'format("mcui.screen.settings.' .. string.gsub(id, ':', '.') .. '")'
end

--- @param parent Widget the parent's **content** widget
local function addThemeSettings(parent)
    local currentTheme = settings.currentTheme().string
    local themeCount = 0
    for index, themeId in ipairs(settings.themes()) do
        local themeLocalKey = themeId.namespace .. '.' .. themeId.path
        local r = theme.loadWidget(parent, label_button_frag, {
            isCurrentTheme = wl.tstatic(themeId.string == currentTheme),
            --text = tstatic('tmp'),
            text = wl.tframe('(isCurrentTheme? "> ": "" ) + formatOr("mcui.theme.' .. themeLocalKey .. '.name", "' .. themeId.path .. '")', 'STRING', true),
            --active = tframe('!isCurrentTheme', 'BOOLEAN'),
            xPos = wl.tstatic(0),
            yPos = wl.tframe((index - 1) * 20, 'DOUBLE'),
            initialWidth = wl.tstatic(80, 'INT'),
            -- for LuaJC
            --themeId = tstatic(themeId.string)
        })
        if r then
            themeCount = themeCount + 1
            local b = --[[---@type Widget]] r
            --b.setVariable('text', tframe('(isCurrentTheme? "> ": "" ) + format("mcui.theme.' .. themeId.namespace .. '.' .. themeId.path .. '.name")', 'STRING', true))
            --b.setVariable('active', tframe('!isCurrentTheme', 'BOOLEAN'))
            b.tooltip = wl.tstatic('format("mcui.theme.' .. themeLocalKey .. '.description")', 'STRING', true)
            b.onClick = function(receiver)
                if receiver.getVariable('isCurrentTheme').expression == 'false' then
                    receiver.setVariable('isCurrentTheme', wl.tstatic(true))
                    --receiver.setVariable('active', tstatic(false))
                    for _, v in ipairs(receiver.peers) do
                        if (type(v) == 'Widget') then
                            -- TODO: disabled themes that failed to load to show helpful tooltip ?
                            --(--[[---@type Widget]] v).setVariable('active', tstatic(true))
                            (--[[---@type Widget]] v).setVariable('isCurrentTheme', wl.tstatic(false))
                        end
                    end
                    -- for LuaJC
                    --settings.setTheme(--[[---@type string]] receiver.getVariable('themeId').expression)
                    settings.setTheme(themeId)
                    return true
                else
                    return false
                end
            end
        end
    end
    centerCategoryContent(parent, themeCount)
end

local function gui(root)
    local themes = loadCategory(root, 'scaledheight / 2 - 10', "scaledwidth / 2 - 120", 'themeselection', function()
        return 'format("mcui.screen.settings.themeselection")'
    end)
    if (themes) then
        local themesContent = getChildWidget(themes, 'content')
        addThemeSettings(themesContent)
    end

    --- @type table<string, table<string, table<string, Setting>>>
    local topCategories = {}
    for _, v in ipairs(settings.listAll()) do
        --print("Found setting " .. v.namespace.string .. " / " .. v.key.string)

        local topLevel = util.getOrCreate(topCategories, v.namespace.string)
        local category = util.getOrCreate(topLevel, v.namespace.string .. '/' .. v.key.namespace)
        category[v.key.path] = v
    end

    --print("Organised settings : ")
    --print(tprint(topCategories))

    local yPosBase = 'scaledheight / 2 - 10 + '
    local yPos = 20

    for topLevelName, topLevelValue in pairs(topCategories) do
        local topLevel = loadCategory(root, yPosBase .. tostring(yPos), "scaledwidth / 2 - 120", topLevelName, settingsTabName)
        if topLevel then
            local tlN = 0
            local tlContent = getChildWidget(topLevel, 'content')
            for catName, catValue in pairs(topLevelValue) do
                local category = loadCategory(tlContent, tlN * 20, 0, catName, settingsCategoryName)
                local catN = 0
                if category then
                    local catContent = getChildWidget(category, 'content')
                    for settingName, setting in pairs(catValue) do
                        loadSetting(catContent, catN * 20, 0, settingName .. ': ' .. type(setting), setting.comment)
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
end

-- split to avoid tail call, which does not seem to work
local ok = theme.registerScreen("mcui:settings", gui)
return ok
