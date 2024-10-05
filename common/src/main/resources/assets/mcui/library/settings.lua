--- @module settings
local settings = {}

--- Lists all available settings
--- @return Setting[]
function settings.listAll()
    return {}
end

--- Lists all available themes
--- @return LKResourceLocation[]
function settings.themes()
    return {}
end

--- Returns the id of the currently enabled theme
--- @return LKResourceLocation
function settings.currentTheme()
    return nil
end

--- Lists all available settings
--- @param themeId LKResourceLocation|string
function settings.setTheme(themeId) end

--- Lists all registered screen IDs
--- @return LKResourceLocation[]
function settings.allScreenIds()
    return {}
end

--- Lists the IDs of themes implementing a given screen
--- @param screenId LKResourceLocation|string
--- @return LKResourceLocation[]
function settings.getThemesImplementingScreenId(screenId)
    return {}
end

--- Lists the IDs of themes implementing a given screen
--- @param screenId LKResourceLocation|string
--- @return LKResourceLocation|nil
function settings.getScreenConfiguration(screenId)
    return nil
end

--- Configures the theme to use for specified screen
--- @param screenId LKResourceLocation|string
--- @param themeId LKResourceLocation|string
function settings.setScreenConfiguration(screenId, themeId)
end

return settings