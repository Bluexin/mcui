import type {Widget} from "../../typings/Widget";
import {type ButtonArgs, loadButton, typedStaticValue} from "./widget_lib";

const menu_frag = theme.readWidget("mcui.hex-ts:menu_parent");
const page_frag = theme.readWidget("mcui.hex-ts:menu_page");

export interface Menu extends Widget {
    extra: {
        open: (this: void, pageKey: string) => boolean;
    }
}

/**
 * Adds a paged menu to given widget
 */
export function addMenu(parent: Widget | string): Menu {
    const r = theme.loadWidget(parent, menu_frag) as Menu || error(`Unable to load menu`);

    r.extra.open = (pageKey) => {
        const trimmedPageKey = pageKey.replaceAll('"', '');
        r.allChildren.forEach(childElement => {
            if (childElement.name === 'menu_page') {
                const child = childElement as Widget;
                const childKey = child.getVariable('pageKey').expression.replaceAll('"', '');
                child.setVariable('isOpen', typedStaticValue(childKey === trimmedPageKey));
            }
        })

        return true;
    }

    return r
}

export interface MenuPage extends Widget {
    extra: {
        close: (this: void) => boolean;
    }
}

/**
 * Adds a page to the menu
 */
export function addMenuPage(menuRoot: Widget | string, key: string, parentKey?: string): MenuPage {
    const r = theme.loadWidget(menuRoot, page_frag, {
        pageKey: typedStaticValue(key, "STRING"),
        parentPageKey: typedStaticValue(parentKey || '', "STRING"),
    }) as MenuPage || error(`Unable to load page ${key}`);
    r.extra.close = () => {
        (r.parentElement as Widget).extra.open?.(r.getVariable('parentPageKey').expression)
        return true;
    }
    return r;
}

/**
 * Adds a button to navigate to the parent page
 */
export function addBackButton(page: MenuPage, args?: ButtonArgs): void {
    const content = page.getChildByName('content') as Widget || page; // in case we want to give the content itself
    loadButton(
        content,
        {
            yPos: -10,
            width: 160,
            label: typedStaticValue('format("mcui.screen.back")', 'STRING', true),
            onClick: function (this: Widget) {
                this.setVariable('isHovered', typedStaticValue(false, "BOOLEAN"));
                return page.extra.close();
            },
            ...(args ?? {})
        }
    );
}

/**
 * Adds a button to navigate to another page
 */
export function addButtonToPage(
    menuRoot: Menu,
    onPageContent: Widget,
    toPage: string,
    label?: ((id: string) => string) | null,
    args?: Partial<ButtonArgs>
): void {
    loadButton(
        onPageContent,
        {
            key: toPage,
            label: typedStaticValue(
                (label ? label(toPage) : `"${toPage}"`),
                "STRING",
                true
            ),
            xPos: -80,
            width: 160,
            onClick: function () {
                return menuRoot.extra.open(toPage);
            },
            ...(args ?? {})
        }
    );
}
