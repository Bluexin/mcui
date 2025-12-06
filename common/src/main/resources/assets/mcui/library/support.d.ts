export declare enum MouseButtons {
    LEFT = 0,
    RIGHT = 1,
    MIDDLE = 2,
    BACK = 3,
    FORWARD = 4,
}

/**
 * How a computed value should be cached.
 */
export type CacheType = "PER_FRAME" | "STATIC" | "SIZE_CHANGE" | "NONE";

/**
 * The underlying MiniScript JEL type of a computed value.
 */
export type JelType = "STRING" | "DOUBLE" | "INT" | "BOOLEAN" | "UNIT" | "ERROR";

/**
 * A value with an expression and cache policy.
 */
export interface TypeSpecificCValue {
    /** Expression string in the MiniScript dialect. */
    expression: string;
    /** Cache policy for the value. */
    cache: CacheType;
}

/**
 * Represents a MiniScript value computing to a generic type.
 */
export interface CValue<T> extends TypeSpecificCValue {
    /** MiniScript JEL type. */
    type: JelType;
    // readonly value: T; // TODO
}

/** Represents a MiniScript value computing to int type */
export type CInt = (CValue<number> & { type: "INT" }) | string | number;

/** Represents a MiniScript value computing to boolean type */
export type CBoolean = (CValue<boolean> & { type: "BOOLEAN" }) | string | boolean;

/** Represents a MiniScript value computing to double type */
export type CDouble = (CValue<number> & { type: "DOUBLE" }) | string | number;

/** Represents a MiniScript value computing to string type */
export type CString = (CValue<string> & { type: "STRING" }) | string | number;

/** Represents a MiniScript value computing to void type */
export type CUnit = (CValue<void> & { type: "UNIT" }) | string;

/**
 * Minecraft ResourceLocation
 */
export interface LKResourceLocation {
    namespace: string;
    path: string;
    /** text representation in namespace:path format */
    string: string;
}

/**
 * Loads a module from the given mod/theme context.
 *
 * @param modname Module name or path
 * @param themeId Theme resource location (optional)
 * @returns Any loaded value
 * @deprecated TODO : check whether this is needed with tstl
 */
export declare function require(modname: string, themeId?: LKResourceLocation | null): any;

export enum HumanoidArm {
    LEFT = "LEFT",
    RIGHT = "RIGHT"
}

declare global {

    /**
     * Returns the type of the given value. MCUI objects will return their class name as type instead of 'userdata'.
     * @param v
     */
    export function type(
        v: any
    ): 'nil' | 'number' | 'string' | 'boolean' | 'table' | 'function' | 'thread' | 'userdata' | string;
}
