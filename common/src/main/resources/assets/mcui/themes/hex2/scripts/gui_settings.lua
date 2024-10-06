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
        local themeId, _ = receiver.getVariable('themeId').expression:gsub('"', '')
        settings.setTheme(themeId)
        receiver.setVariable('isCurrentTheme', wl.tstatic(true))
        --receiver.setVariable('active', tstatic(false))
        for _, v in ipairs(receiver.peers) do
            if (type(v) == 'Widget') then
                -- TODO: disabled themes that failed to load to show helpful tooltip ?
                --(--[[---@type Widget]] v).setVariable('active', tstatic(true))
                (--[[---@type Widget]] v).setVariable('isCurrentTheme', wl.tstatic(false))
            end
        end
        return true
    else
        return false
    end
end

local dropdownSupport = require 'choice_dropdown_support'

--- @param parent Widget the parent's **content** widget
local function addThemeSettings(parent)
    local settingsCount = 0
    for index, screenId in ipairs(settings.allScreenIds()) do
        local screenLocalKey = screenId.namespace .. '.screen.' .. screenId.path
        local currentValue = settings.getScreenConfiguration(screenId)
        if dropdownSupport.createDropdown(parent, {
            currentValue = 'mcui.theme.' .. (currentValue and currentValue.string or 'none'),
            key = screenLocalKey,
            label = wl.tstatic('format("' .. screenLocalKey .. '.name")', 'STRING', true),
            xPos = 0,
            yPos = (index - 1) * 20,
            options = util.map(
                    settings.getThemesImplementingScreenId(screenId),
                    function(it)
                        local i18nKey = 'mcui.theme.' .. it.string
                        return {
                            key = i18nKey,
                            variables = {
                                initialWidth = wl.tstatic(80, 'INT')
                            }
                        }
                    end
            ),
            setValue = function(newValue)
                local themeId, _ = newValue.expression:gsub('mcui.theme.', ''):gsub('"', '')
                --print('Setting screen ' .. screenId.string .. ' to ' .. themeId)
                settings.setScreenConfiguration(screenId, themeId)
            end,
        }) then
            settingsCount = settingsCount + 1
        end
    end
    wl.centerCategoryContent(parent, settingsCount)
end

--- @param setting Setting
--- @return CValue|string
local function settingTooltip(setting)
    return (setting.commentKey and wl.tstatic('format("' .. setting.commentKey .. '")', 'STRING', true)) or setting.comment
end

local xPos = 'scaledwidth / 2 - 60'

local function gui(root)
    print('Starting to analyse settings')
    local themes = wl.loadCategory(root, 'scaledheight / 2 - 10', xPos, 'themeselection', function()
        return 'format("mcui.screen.settings.themeselection")'
    end)
    if (themes) then
        themes.extra.onClose = function()
            print('Closing themes, add callback to cache screens here ?')
        end
        local themesContent = wl.getChildWidget(themes, 'content')
        addThemeSettings(themesContent)
    end

    --- @type table<string, table<string, table<string, Setting>>>
    local topCategories = {}
    local allSettings = settings.listAll()
    print('Analyzing ' .. tostring(rawlen(allSettings)) .. ' settings')
    for _, v in ipairs(allSettings) do
        --print("Found setting " .. v.namespace.string .. " / " .. v.key.string)

        local topLevel = util.getOrCreate(topCategories, v.namespace.string)
        local category = util.getOrCreate(topLevel, v.namespace.string .. '/' .. v.key.namespace)
        category[v.key.path] = v
    end

    print("Analyzed settings")
    --print(tprint(topCategories))

    local yPosBase = 'scaledheight / 2 - 10 + '
    local yPos = 20

    for topLevelName, topLevelValue in pairs(topCategories) do
        local topLevel = wl.loadCategory(root, yPosBase .. tostring(yPos), xPos, topLevelName, settingsTabName)
        if topLevel then
            local tlN = 0
            local tlContent = wl.getChildWidget(topLevel, 'content')
            for catName, catValue in pairs(topLevelValue) do
                local category = wl.loadCategory(tlContent, tlN * 20, 0, catName, settingsCategoryName)
                local catN = 0
                if category then
                    local catContent = wl.getChildWidget(category, 'content')
                    for settingName, setting in pairs(catValue) do
                        if (type(setting) == 'BooleanSetting') then
                            local bs = --[[---@type BooleanSetting]] setting
                            wl.loadButton(
                                    catContent, {
                                        xPos = 0, yPos = catN * 20,
                                        -- miniscript settings currently resolve to "currentTheme" so not great
                                        label = wl.tframe('"' .. settingName .. ': " + (state? "yes": "no")', 'STRING', true),
                                        --label = settingName .. ': ' .. tostring(bs.getValue()),
                                        tooltip = settingTooltip(setting),
                                        onClick = function(widget)
                                            local newVal = not bs.getValue()
                                            bs.setValue(newVal)
                                            widget.setVariable('state', wl.tstatic(newVal))
                                            return true
                                        end,
                                        variables = {
                                            state = wl.tstatic(bs.getValue())
                                        }
                                    }
                            )
                        else
                            wl.loadButton(
                                    catContent, {
                                        xPos = 0, yPos = catN * 20,
                                        label = settingName .. ': ' .. type(setting),
                                        tooltip = settingTooltip(setting)
                                    }
                            )
                        end

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
