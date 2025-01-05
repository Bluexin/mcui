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
            toprint = toprint .. util.tprint(v, indent) .. ",\r\n"
        else
            toprint = toprint .. "\"" .. tostring(v) .. "\",\r\n"
        end
    end
    toprint = toprint .. string.rep(" ", indent - 2) .. "}"
    return toprint
end

--- @generic K, V
--- @param a table<K, V>
--- @param b table<K, V>
--- @return table<K, V>
function util.merge(a, b)
    local c = util.shallow_copy(a)
    for k, v in pairs(b) do
        c[--[[---@type K]] k] = v
    end
    return c
end

--- @generic K, V
--- @param t table<K, V>
--- @return table<K, V>
function util.shallow_copy(t)
    --- @type table<K, V>
    local t2 = {}
    for k, v in pairs(t) do
        t2[--[[---@type K]] k] = v
    end
    return t2
end

--- @generic T, R
--- @param from T[]
--- @param mapper fun(input: T): R
--- @return R[]
function util.map(from, mapper)
    local r = {}
    for _, value in ipairs(from) do
        table.insert(r, mapper(value))
    end

    return r
end

--- @param tbl table
function util.clear(tbl)
    for k in pairs(tbl) do
        tbl[k] = nil
    end
end

--- @param input string
--- @return boolean
function util.string_to_boolean(input)
    return input and (input:lower() == 'true')
end

return util