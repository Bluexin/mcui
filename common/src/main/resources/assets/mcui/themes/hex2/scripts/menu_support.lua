--- @module menu_support
local menu = {}

local theme = require 'theme'
local util = require 'util'
local wl = require 'widgetlib'

local menu_frag = theme.readWidget("mcui.hex2:menu_parent")
local page_frag = theme.readWidget("mcui.hex2:menu_page")

--- @param parent Widget|string
--- @return Widget
function menu.addMenu(parent)
    local r = theme.loadWidget(parent, menu_frag)
    return r
end

--- @param parent Widget|string
--- @param key string
--- @param parentKey string|nil
--- @return Widget
function menu.addMenuPage(parent, key, parentKey)
    local r = theme.loadWidget(parent, page_frag, {
        pageKey = wl.tstatic(key),
        parentPageKey = wl.tstatic(parentKey or ''),
    })
    return r
end

--- @param page Widget
--- @param args? buttonArgs
function menu.addBackButton(page, args)
    wl.loadButton(
        page.getChildByName('content'),
        util.merge(
            {
                label = wl.tstatic('format("mcui.screen.back")', 'STRING', true),
                onClick = function (self)
                    self.setVariable('isHovered', wl.tstatic(false))
                    return page.extra.close()
                end
            }, args or {}
        )
    )
end

return menu
