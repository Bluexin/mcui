import type {Widget} from "../../typings/Widget";
import type {CDouble, CInt, CString, CValue, JelType, TypeSpecificCValue} from "../../typings/support"

export const getChildWidget = (parent: Widget | null, name: string) => {
    if (parent) {
        const child = parent.getChildByName(name)
        if (child && type(child) === 'Widget') {
            return child
        }
    }
    return error(`No child widget with name ${name} found in ${parent?.name ?? 'missing parent'}`)
}

export type Value = string | number | boolean

export function staticValue(value: Value): TypeSpecificCValue & { cache: 'STATIC' } {
    if (typeof value === 'string') {
        value = `"${value}"`
    } else {
        value = value.toString()
    }
    return {
        expression: value,
        cache: 'STATIC'
    }
}

export function typedStaticValue<JTYPE extends JelType>(
    value: Value,
    jtype?: JTYPE,
    nowrap?: boolean
): CValue & { type: JTYPE, cache: 'STATIC' } {
    if (!jtype) {
        switch (typeof value) {
            // Casting as in this case JTYPE can not be more strict
            case 'string':
                jtype = "STRING" as JTYPE
                break
            case 'boolean':
                jtype = "BOOLEAN" as JTYPE
                break
            case 'number':
                jtype = "DOUBLE" as JTYPE
                break
            default:
                return error(`Unhandled type ${typeof value}`)
        }
    }
    if (typeof value !== 'string') {
        value = value.toString()
    }
    if (jtype === 'STRING' && !nowrap) {
        value = `"${value}"`
    }
    return {
        expression: value,
        cache: 'STATIC',
        type: jtype
    }
}

function typedFrameValue(value: string | number, jtype?: JelType, nowrap?: boolean): CValue & { cache: 'PER_FRAME' } {
    return {
        ...typedStaticValue(value, jtype, nowrap),
        cache: "PER_FRAME"
    }
}

const icon_label_button_frag = theme.readWidget("mcui.hex-ts:icon_button_expanding_label")

export function loadExpandingButton(
    parent: Widget | string,
    text: string,
    onClick: Widget['onClick'],
    x: number,
    y: number
): Widget | false {
    const r = theme.loadWidget(parent, icon_label_button_frag, {
        text: typedStaticValue(text, 'STRING'),
        xPos: typedStaticValue(x),
        yPos: typedStaticValue(y)
    })
    if (!r) {
        print(`Could not load button ${text}`);
    } else {
        r.onClick = onClick
    }
    return r
}

export function fromArg(
    arg: Value | CValue,
    valueType: JelType,
    defaultValue: (value: Value, type: JelType, nowrap: boolean) => CValue = typedStaticValue,
): CValue {
    if (typeof arg === 'object') {
        return arg as CValue
    } else {
        return defaultValue(arg as Value, valueType, false)
    }
}

export function argToVariable<OUT>(
    source: Value | CValue | undefined,
    key: keyof OUT,
    valueType: JelType,
): OUT | {} {
    return (source !== undefined ? {[key]: fromArg(source, valueType)} : {})
}

function stringFromExpr<IN>(value: IN | undefined): string {
    if (value === undefined) {
        return ''
    } else if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        return value.toString()
    } else {
        return (value as TypeSpecificCValue).expression
    }
}

/** Arguments for button configuration */
export interface ButtonArgs {
    key?: string;
    xPos?: CDouble;
    yPos?: CDouble;
    width?: CInt;
    label?: CString;
    onClick?: Widget['onClick'];
    tooltip?: string;
    variables?: Record<string, CValue>;
}

export function loadButton(parent: Widget | string, args: ButtonArgs): Widget | false {
    const label_button_frag = theme.readWidget("mcui.hex-ts:label_button");

    const variables: Record<string, CValue> = {
        ...argToVariable(args.label, 'label', 'STRING'),
        ...argToVariable(args.xPos, 'xPos', 'STRING'),
        ...argToVariable(args.yPos, 'yPos', 'STRING'),
        ...argToVariable(args.width, 'initialWidth', 'STRING'),
        ...args.variables
    };

    const r = theme.loadWidget(parent, label_button_frag, variables);
    if (!r) {
        print(`Could not load button ${stringFromExpr(args.label)}`);
    } else {
        const w = r as Widget;
        if (args.tooltip) {
            w.tooltip = typedStaticValue(args.tooltip, 'STRING');
        }
        if (args.onClick) {
            w.onClick = args.onClick;
        }
        if (args.key) {
            w.name = args.key;
        }
    }
    return r
}

const cancel_apply_button_frag = theme.readWidget("mcui.hex-ts:cancel_apply_buttons")

export function loadCancelApplyButtons(parent: Widget | string): Widget | false {
    const r = theme.loadWidget(parent, cancel_apply_button_frag, {
        yPos: typedStaticValue(-10)
    })
    if (!r) {
        print(`Could not load cancel/apply buttons for ${(parent as Widget).hierarchyName ?? parent}`);
    }
    return r
}
