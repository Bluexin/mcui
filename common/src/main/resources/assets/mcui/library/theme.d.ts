import {CValue} from "./support";
import {Fragment} from "./Fragment";
import {Widget} from "./Widget";

/**
 * Theme API — reading/loading fragments and widgets, and registering screens.
 */
export declare const theme: {
    /**
     * Reads the Fragment pointed to by the resource location made from given namespace and path.
     * If only one parameter is given, it is considered to be the full resource location in
     * `namespace:path` format.
     * @returns A raw fragment table-like structure
     */
    readFragment(this: void, resourceLocation: string): Record<string, any>;
    readFragment(this: void, namespace: string, path: string): Record<string, any>;

    /**
     * Loads the Fragment into the target's children, optionally using variables to evaluate the Fragment.
     * @param target Root id of a Screen as string
     * @param fragment The raw fragment as returned by readFragment
     * @param variables Variables to use when evaluating the fragment
     * @returns The loaded Fragment instance if successful, or false otherwise
     */
    loadFragment(
        this: void,
        target: string,
        fragment: Record<string, any>,
        variables?: Record<string, CValue>
    ): Fragment | false;

    /**
     * Reads the Widget pointed to by the resource location made from given namespace and path.
     * If only one parameter is given, it is considered to be the full resource location in
     * `namespace:path` format.
     * @returns A raw widget table-like structure
     */
    readWidget(this: void, resourceLocation: string): Record<string, any>;
    readWidget(this: void, namespace: string, path: string): Record<string, any>;

    /**
     * Loads the Widget into the target's children, optionally using variables to evaluate the Widget.
     * @param target Root id of a Screen as string, or reference to an already loaded Widget
     * @param widget The raw widget as returned by readWidget
     * @param variables Variables to use when evaluating the widget
     * @returns The loaded Widget instance if successful, or false otherwise
     */
    loadWidget(
        this: void,
        target: string | Widget,
        widget: Record<string, any>,
        variables?: Record<string, CValue>
    ): Widget | false;

    /**
     * Registers a callback for initializing the Screen with the given id.
     * @param id Screen identifier (string)
     * @param callback Callback receiving the root id for manipulating the Screen
     * @returns Whether the operation was successful
     */
    registerScreen(this: void, id: string, callback: (rootId: string) => void): boolean;
};

declare global {
    const theme: typeof import("./theme").theme;
}

export {};
