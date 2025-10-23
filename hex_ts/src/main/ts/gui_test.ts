import type {Widget} from "../../typings/Widget";
import {addBackButton, addMenu, addMenuPage} from "./menu_support";
import {loadExpandingButton, typedStaticValue} from "./widget_lib";

const buttons: Widget[] = []

const colourMask = ' & 0xAFAFAFFF'

const colourButtons = (colour: string) => {
    buttons.forEach(button => {
        button.setVariable('colour', typedStaticValue(`(int) ${colour}`, 'INT'))
        button.setVariable('hoverColour', typedStaticValue(`(int) ${colour} ${colourMask}`, 'INT'))
    })
}

const gui = (root: string) => {
    const menu = addMenu(root)
    const page1w = addMenuPage(menu, 'p1')
    const page2w = addMenuPage(menu, 'p2', 'p1')

    const page1 = page1w.getChildByName('content') as Widget
    const page2 = page2w.getChildByName('content') as Widget

    let b = loadExpandingButton(page1, "Orange button", (xPos, yPos, mb) => {
        print("Hit Orange with " + mb)
        colourButtons('0xDEA51FFFL')
        return true
    }, 12, -30)
    if (b) buttons.push(b)

    b = loadExpandingButton(page1, "Pink button", (xPos, yPos, mb) => {
        print("Hit Pink with " + mb)
        colourButtons('0xDE1FBAFFL')
        return true
    }, 0, -10)
    if (b) buttons.push(b)

    b = loadExpandingButton(page1, "Blue button", (xPos, yPos, mb) => {
        print("Hit Blue with " + mb)
        colourButtons('0x1F85DEFFL')
        return true
    }, 12, 10)
    if (b) buttons.push(b)

    loadExpandingButton(page1, "Page 2", function onClick() {
        this.setVariable('isHovered', typedStaticValue(false))
        menu.extra.open('p2')
        return true
    }, 0, 30)

    // TODO : dropdownSupport

    addBackButton(page2w, {
        xPos: 50, yPos: 70
    })

    menu.extra.open('p1')
}

theme.registerScreen('mcui:tstestgui', gui)
