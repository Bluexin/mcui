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

local dropdownSupport = require 'choice_dropdown_support'
local checkboxSupport = require 'checkbox_support'

--- @param parent Widget the parent's **content** widget
--- @param changes table
local function addThemeSettings(parent, changes)
    local settingsCount = 0
    for index, screenId in ipairs(settings.allScreenIds()) do
        local screenLocalKey = screenId.namespace .. '.screen.' .. screenId.path
        local currentValue = settings.getScreenConfiguration(screenId)
        local dropdown = dropdownSupport.createDropdown(parent, {
            currentValue = 'mcui.theme.' .. (currentValue and currentValue.string or 'none'),
            key = screenLocalKey,
            label = wl.tstatic('format("' .. screenLocalKey .. '.name")', 'STRING', true),
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
                changes[screenId] = themeId
            end,
        })
        if dropdown then
            dropdown.extra.reloadValue = function()
                local newValue = settings.getScreenConfiguration(screenId)
                dropdown.setVariable('currentValue', wl.tstatic('mcui.theme.' .. (newValue and newValue.string or 'none')))
            end
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

--- @param parent Widget
--- @param setting BooleanSetting
--- @param catN number
local function booleanSettingButton(parent, setting, catN)
    local screenLocalKey = ('mcui.theme.' .. setting.namespace.string .. '.setting.' .. setting.key.string .. '.name'):gsub(':', '/')
    local fullLabel = 'formatOr("' .. screenLocalKey .. '", "' .. setting.key.path .. '")'

    return checkboxSupport.createCheckbox(
            parent, {
                yPos = catN * 20,
                label = wl.tframe(fullLabel, 'STRING', true),
                tooltip = settingTooltip(setting),
                onClick = function(widget)
                    local newVal = not setting.getValue()
                    setting.setValue(newVal)
                    widget.setVariable('currentValue', wl.tstatic(newVal))
                    return true
                end,
                variables = {
                    currentValue = wl.tframe('settings.boolean("' .. setting.namespace.string .. '", "' .. setting.key.string .. '")', 'BOOLEAN')
                }
            }
    )
end

--- @param parent Widget
--- @param setting ChoiceSetting
--- @param catN number
local function choiceSettingButton(parent, setting, catN)
    local screenLocalKey = ('mcui.theme.' .. setting.namespace.string .. '.setting.' .. setting.key.string):gsub(':', '/')

    return dropdownSupport.createDropdown(parent, {
        currentValue = wl.tframe('settings.string("' .. setting.namespace.string .. '", "' .. setting.key.string .. '")', 'STRING', true),
        key = screenLocalKey,
        label = wl.tstatic('format("' .. screenLocalKey .. '.name")', 'STRING', true),
        yPos = catN * 20,
        options = util.map(setting.values, function(it)
            local i18nKey = screenLocalKey .. '/' .. it
            return {
                key = it,
                variables = {
                    initialWidth = wl.tstatic(80, 'INT'),
                    translateValue = wl.tframe('format("' .. screenLocalKey .. '" + "/" + thisValue + ".name")', 'STRING', true)
                }
            }
        end),
        setValue = function(newValue)
            setting.setValue(newValue.expression:gsub('"', ''))
        end,
        variables = {
            translateValue = wl.tframe('format("' .. screenLocalKey .. '" + "/" + currentValue + ".name")', 'STRING', true)
        }
    })
end

--- @param parent Widget
--- @param setting Setting
--- @param catN number
local function settingButton(parent, setting, catN)
    if type(setting) == 'BooleanSetting' then
        return booleanSettingButton(parent, setting, catN)
    elseif type(setting) == 'ChoiceSetting' then
        return choiceSettingButton(parent, setting, catN)
    else
        local screenLocalKey = ('mcui.theme.' .. setting.namespace.string .. '.setting.' .. setting.key.string .. '.name'):gsub(':', '/')
        local valueString = tostring(setting.getValue()) -- static fallback
        local settingArgs = '("' .. setting.namespace.string .. '", "' .. setting.key.string .. '")'
        if type(setting) == 'StringSetting' then
            valueString = 'settings.string' .. settingArgs
        elseif type(setting) == 'IntSetting' then
            valueString = 'settings.int' .. settingArgs
        elseif type(setting) == 'DoubleSetting' then
            valueString = 'settings.double' .. settingArgs
        elseif type(setting) == 'ResourceLocationSetting' then
            valueString = 'settings.resourceLocation' .. settingArgs
        end

        local fullLabel = 'format("mcui.screen.settinglabel", formatOr("' .. screenLocalKey .. '", "' .. setting.key.path .. '"), ' .. valueString .. ')'

        return wl.loadButton(
                parent, {
                    yPos = catN * 20,
                    label = wl.tframe(fullLabel, 'STRING', true),
                    tooltip = settingTooltip(setting),
                    variables = {
                        active = wl.tstatic(false)
                    }
                }
        )
    end
end

local function gui(root)
    print('Starting to analyse settings')
    local themes = wl.loadCategory(root, 'scaledheight / 2 - 10', xPos, 'themeselection', function()
        return 'format("mcui.screen.settings.themeselection")'
    end, true)
    if (themes) then
        local changes = {}
        local themesContent = wl.getChildWidget(themes, 'content')

        themes.extra.cancel = function()
            wl.onCloseCategory(themes)
            if next(changes) then
                util.clear(changes)
                for _, v in ipairs(themesContent.allChildren) do
                    if v.extra.reloadValue then
                        v.extra.reloadValue()
                    end
                end
            end
        end
        themes.extra.apply = function()
            for screenId, themeId in pairs(changes) do
                settings.setScreenConfiguration(screenId, themeId)
            end
            wl.onCloseCategory(themes)
            util.clear(changes)
        end

        wl.loadCancelApplyButtons(themesContent)
        addThemeSettings(themesContent, changes)
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
                local category = wl.loadCategory(tlContent, wl.tstatic(tlN * 20), nil, catName, settingsCategoryName)
                local catN = 0
                if category then
                    local catContent = wl.getChildWidget(category, 'content')
                    for _, setting in pairs(catValue) do
                        settingButton(catContent, setting, catN)
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
