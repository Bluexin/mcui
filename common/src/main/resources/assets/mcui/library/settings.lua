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

return settings