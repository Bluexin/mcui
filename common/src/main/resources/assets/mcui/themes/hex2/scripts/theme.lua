local theme = require 'theme'

-- TODO : maybe this should be a load(), which needs to be safeguarded similarly to require
theme.registerScreen("mcui:settings", require 'gui_settings')
theme.registerScreen("mcui:testgui", require 'gui_test')

return 'Fun times'
