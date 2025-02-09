--- @module menu_support
local menu = {}

local theme = require 'theme'
local util = require 'util'
local wl = require 'widgetlib'

local menu_frag = theme.readWidget("mcui.hex2:menu_parent")
local page_frag = theme.readWidget("mcui.hex2:menu_page")

--- Adds a paged menu to given widget
--- @param parent Widget|string
--- @return Widget
function menu.addMenu(parent)
    local r = theme.loadWidget(parent, menu_frag)
    return r or error('Unable to load menu')
end

--- Adds a page to the menu
--- @param menuRoot Widget|string
--- @param key string
--- @param parentKey? string|nil
--- @return Widget
function menu.addMenuPage(menuRoot, key, parentKey)
    local r = theme.loadWidget(menuRoot, page_frag, {
        pageKey = wl.tstatic(key),
        parentPageKey = wl.tstatic(parentKey or ''),
    })
    return r or error('Unable to load page ' .. key)
end

--- Adds a button to navigate to the parent page
--- @param page Widget
--- @param args? buttonArgs
function menu.addBackButton(page, args)
    wl.loadButton(
        page.getChildByName('content') or page, -- in case we want to give the content itself
        util.merge(
            {
                yPos = -10,
                width = 160,
                label = wl.tstatic('format("mcui.screen.back")', 'STRING', true),
                onClick = function (self)
                    self.setVariable('isHovered', wl.tstatic(false))
                    return page.extra.close()
                end
            }, args or {}
        )
    )
end

--- Adds a button to navigate to another page
--- @param menuRoot Widget
--- @param onPageContent Widget
--- @param toPage string
--- @param label? fun(id: string): string
--- @param args? buttonArgs
function menu.addButtonToPage(menuRoot, onPageContent, toPage, label, args)
    wl.loadButton(
        onPageContent,
        util.merge(
            {
                key = toPage,
                label = wl.tstatic((label and label(toPage)) or '"' .. label .. '"', "STRING", true),
                xPos = -80,
                width = 160,
                onClick = function ()
                    return menuRoot.extra.open(toPage)
                end,
            }, args or {}
        )
    )
end

return menu
