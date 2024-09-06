local theme = require 'theme'
local settings = require 'settings'
local wl = require 'widgetlib'
local util = require 'util'

--- @param id string ResourceLocation.string
local function settingsTabName(id)
    return 'format("mcui.screen.settings.theme", format("mcui.theme.' .. id:gsub(':', '.') .. '.name"))'
end

--- @param id string ResourceLocation.string
local function settingsCategoryName(id)
    return 'format("mcui.screen.settings.' .. string.gsub(id, ':', '.') .. '")'
end

--- @param receiver Widget
local function onThemeClick(receiver)
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
        local themeId, _ = receiver.getVariable('themeId').expression:gsub('"', '')
        settings.setTheme(themeId)
        return true
    else
        return false
    end
end

--- @param parent Widget the parent's **content** widget
local function addThemeSettings(parent)
    local currentTheme = settings.currentTheme().string
    local themeCount = 0
    for index, themeId in ipairs(settings.themes()) do
        local themeLocalKey = themeId.namespace .. '.' .. themeId.path
        if wl.loadButton(
                parent, {
                    label = wl.tframe('(isCurrentTheme? "> ": "" ) + formatOr("mcui.theme.' .. themeLocalKey .. '.name", "' .. themeId.path .. '")', 'STRING', true),
                    tooltip = wl.tstatic('format("mcui.theme.' .. themeLocalKey .. '.description")', 'STRING', true),
                    xPos = 0,
                    yPos = (index - 1) * 20,
                    width = 80,
                    onClick = onThemeClick,
                    variables = {
                        isCurrentTheme = wl.tstatic(themeId.string == currentTheme),
                        themeId = wl.tstatic(themeId.string)
                    }
                }
        ) then
            themeCount = themeCount + 1
        end
    end
    wl.centerCategoryContent(parent, themeCount)
end

local function gui(root)
    local themes = wl.loadCategory(root, 'scaledheight / 2 - 10', "scaledwidth / 2 - 120", 'themeselection', function()
        return 'format("mcui.screen.settings.themeselection")'
    end)
    if (themes) then
        local themesContent = wl.getChildWidget(themes, 'content')
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
        local topLevel = wl.loadCategory(root, yPosBase .. tostring(yPos), "scaledwidth / 2 - 120", topLevelName, settingsTabName)
        if topLevel then
            local tlN = 0
            local tlContent = wl.getChildWidget(topLevel, 'content')
            for catName, catValue in pairs(topLevelValue) do
                local category = wl.loadCategory(tlContent, tlN * 20, 0, catName, settingsCategoryName)
                local catN = 0
                if category then
                    local catContent = wl.getChildWidget(category, 'content')
                    for settingName, setting in pairs(catValue) do
                        wl.loadButton(
                                catContent, {
                                    xPos = 0, yPos = catN * 20,
                                    label = settingName .. ': ' .. type(setting),
                                    tooltip = setting.comment
                                }
                        )
                        catN = catN + 1
                    end
                    wl.centerCategoryContent(catContent, catN)
                end
                tlN = tlN + 1
            end
            wl.centerCategoryContent(tlContent, tlN)
        end
        yPos = yPos + 20
    end
end

-- split to avoid tail call, which does not seem to work
local ok = theme.registerScreen("mcui:settings", gui)
return ok
