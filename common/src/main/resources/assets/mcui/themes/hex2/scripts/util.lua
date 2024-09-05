--- @module util
local util = {}

--- @generic T
--- @param t table<string, T>
--- @param key string
--- @return T
function util.getOrCreate(t, key)
    if (t[key] == nil) then
        t[key] = --[[---@type T]] {}
    end
    return t[key]
end

--- @param tbl table
--- @param indent? number
function util.tprint (tbl, indent)
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

return util