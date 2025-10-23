import {Setting} from "./Setting";
import {LKResourceLocation} from "./support";

/**
 * Settings API — provides access to available settings and theme selections.
 * // TODO: should this be a TS module ?
 */
declare const settings: {
    /**
     * Lists all available settings.
     * @returns Array of settings definitions
     */
    listAll(this: void): Setting[];

    /**
     * Lists all available themes.
     * @returns Array of theme resource locations
     */
    themes(this: void): LKResourceLocation[];

    /**
     * Returns the id of the currently enabled theme.
     */
    currentTheme(this: void): LKResourceLocation;

    /**
     * Sets the currently enabled theme.
     * @param themeId Theme id as ResourceLocation or string in "namespace:path" format
     */
    setTheme(this: void, themeId: LKResourceLocation | string): void;

    /**
     * Lists all registered screen IDs.
     * @returns Array of screen resource locations
     */
    allScreenIds(this: void): LKResourceLocation[];

    /**
     * Lists the IDs of themes implementing a given screen.
     * @param screenId Screen id as ResourceLocation or string in "namespace:path" format
     * @returns Array of theme resource locations
     */
    getThemesImplementingScreenId(this: void, screenId: LKResourceLocation | string): LKResourceLocation[];

    /**
     * Gets the theme configured for a given screen, if any.
     * @param screenId Screen id as ResourceLocation or string in "namespace:path" format
     * @returns Theme id or null if not configured
     */
    getScreenConfiguration(this: void, screenId: LKResourceLocation | string): LKResourceLocation | null;

    /**
     * Configures the theme to use for the specified screen.
     * @param screenId Screen id as ResourceLocation or string in "namespace:path" format
     * @param themeId Theme id as ResourceLocation or string in "namespace:path" format
     */
    setScreenConfiguration(this: void, screenId: LKResourceLocation | string, themeId: LKResourceLocation | string): void;
}

declare global {
    const settings: typeof import("./settings").settings;
}
