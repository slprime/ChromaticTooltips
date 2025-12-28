# Chromatic Tooltips

🌐 **Languages:** [English](README.md) | [Русский](README.ru.md)

**Chromatic Tooltips** is a client-side mod for Minecraft that transforms tooltips into a fully customizable UI system.
All customization is done through resource packs using declarative JSON, without Java code and recompilation.

## 1. Overview — What is it

Chromatic Tooltips allows you to:

- change backgrounds, borders and textures of tooltips
- configure margins, sizes and alignment
- add animations and transformations
- apply styles conditionally: by items, rarity, tags, NBT etc.

The mod is oriented towards both simple visual tweaks and complex UI compositions.

## 2. Quick Start — first style in 5 minutes

### What you will create

A tooltip style for items with rare rarity:

- semi-transparent background
- golden border
- appearance animation

### Resource pack structure

```
.minecraft/resourcepacks/my_tooltips/
└── assets/
    └── chromatictooltips/
        └── tooltip.json
```

### Minimal example

```json
{
  "styles": [{
        "margin": 2,
        "padding": 4,

        "offsetMain": 6,
        "offsetCross": -18,

        "title": {
            "defaultColor": "0xFFFFFFFF"
        },

        "defaultColor": "0x808080",

        "decorators": [{
            "type": "background",
            "color": "0xF0100010",
            "corner": false
        },{
            "type": "border",
            "color": ["0x505000ff", "0x5028007F"],
            "margin": 1
        }]
    }, {
        "type": "item",
        "filter": "rarity:rare",

        "margin": 2,
        "padding": 4,

        "offsetMain": 6,
        "offsetCross": -18,

        "defaultColor": "0xFFFFFFFF",

        "decorators": [
            { "type": "background", "color": "0x80000020" },
            { "type": "border", "color": "0xFFFFD700", "thickness": 1, "corner": true }
        ],

        "transform": {
            "duration": 300,
            "function": "easeOut",
            "keyframes": [
              { "progress": 0, "scale": 0.8 },
              { "progress": 100, "scale": 1.0 }
            ]
        }
    }
  ]
}
```

### What you wrote

1. the first block with styles will apply to all tooltips
2. the second block will apply only to items with `rarity:rare`

### Testing in game

1. Save the file
2. Press **F3 + T**
3. Hover cursor over a rare item


## 3. Styles — application system

### General format

```json
{
  "styles": [
    {
      "type": "item | default | <custom>",
      "filter": "...",

      "offsetMain": "<int>",
      "offsetCross": "<int>",

      "sectionSpacing": "<int>",

      "hr": {
        "decorators": [ "<TooltipDecorator>" ],
        "transform": "<TooltipTransform>",
        "height" "<int>"
      },

      "<SectionBox>"
    }
  ]
}
```

or `style` if you don't have multiple tooltips

```json
{
    "style": {
        "type": "item | default | <custom>",
        "filter": "...",

        "offsetMain": "<int>",
        "offsetCross": "<int>",

        "<SectionBox>"
    }
}
```

### Filters

Accepts a string with the following tokens:

##### Parts:

```
modname:itemid          - identify: matches any part of the target, so minecraft:lava matches minecraft:lava_bucket
$orename                - ore dictionary: matches any part of the target, so $ingot matches ingotIron, ingotGold, etc.
tag.color=red           - tag
rarity:common           - rarity
0 or 0-12               - damage
```

##### Modifiers:

```
! - logical not. exclude items that match the following expression (!minecraft:portal)
r/.../ - standard java regex (r/^m\w{6}ft$/ = minecraft)
, - logical or in token (minecraft:potion 16384-16462,!16386)
| - logical or multi-item search (wrench|hammer)
```

##### Example:

```
example: minecraft:potion 16384-16462,!16386 | $oreiron | tag.color=red
```

## 4. SectionBox — basic layout unit

**SectionBox** is the core of the entire system.
Any tooltip, section or element is a SectionBox.

### Full format

```json
{
  "margin": "<TooltipSpacing>",
  "padding": "<TooltipSpacing>",

  "font": "<TooltipFontContext>",
  "decorators": [ "<TooltipDecorator>" ],
  "transform": "<TooltipTransform>",

  "alignInline": "left|center|right",
  "alignBlock": "top|center|bottom",
  "minWidth": "<int>",
  "minHeight": "<int>"
}
```

## 5. Components

### 5.1 TooltipSpacing — margins (padding/margin)

Supported formats:

```json
{
    "padding": 4,
    "padding": [4, 8, 4, 8],
    "padding": { "top": 4, "right": 6, "bottom": 4, "left": 6 },
    "padding": { "inline": 4, "block": 6},
    "paddingInline": 4,
    "paddingTop": 6
}
```

### 5.2 TooltipFontContext — typography

Supported formats:

```json
{
  "fontShadow": true,
  "fontParagrath": "<int>",
  "fontColors": {
    "gold": ["0xFFFFD700", "0xFFB8860B"]
  }
}
```

**Alias**

```json
{
    "font": {
        "shadow": true,
        "paragrath": "<int>",
        "colors": {
            "gold": ["0xFFFFD700", "0xFFB8860B"]
        }
    }
}
```

### Fields

| Field | Type | Description | Alias |
|-------|------|-------------|-------|
| `fontShadow` | boolean | Font shadow | `font.shadow` |
| `fontParagrath` | int | Height for empty line | `font.paragrath` |
| `fontColors` | object | Custom font colors | `font.colors` |

### fontColors / font.colors

Supports 32 colors (0–15 basic, 16–31 shadows of basic fonts):

- `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`
- `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

#### Formats

**Single color**
```json
"red": "0xFF0000"
```
> Muted color is calculated automatically.

**Two colors [normal, shadow]**
```json
"red": ["0xFF0000", "0x802020"]
```

### 5.3 TooltipDecorator — visual elements

**General structure:**

```json
{
  "type": "background|border|gradient-horizontal|gradient-vertical|texture|item",
  "margin": "<TooltipSpacing>",          // see section 4
  "alignInline": "left|center|right|start|end",
  "alignBlock": "top|center|bottom|start|end",
  "width": "<int>",
  "height": "<int>",

  // if type = RECTANGLE or BORDER
  "color": "<color or array>",

  // if type = BORDER
  "thickness": "<int>",
  "corner": "<boolean>",

  // if type = TEXTURE
  "path": "<string>",           // + other TooltipTexture fields

  "transform": { ... }          // TooltipTransform JSON
}
```

#### 5.3.1. "type" field and behavior

| type | what is created | required fields |
|------|-----------------|----------------|
| `"none"` | nothing | — |
| `"background"` | colored rectangle | `"color"`, `"thickness"`, `"corner"` |
| `"border"` | border | `"color"`, `"thickness"`, `"corner"` |
| `"texture"` | texture render | all TooltipTexture fields |
| `"item"` | item render | no special fields |
| `"gradient-horizontal"` | horizontal gradient | `"color"` array |
| `"gradient-vertical"` | vertical gradient | `"color"` array |


##### 5.3.1.1. Background Decorator

Rectangular background with customizable color.

```json
{
  "type": "rectangle",
  "color": "0x80202020"
}
```

##### 5.3.1.2. Border Decorator

Border around the tooltip.

```json
{
  "type": "border",
  "color": "0xFFFFFFFF",
  "thickness": 1,
  "corner": true
}
```

##### 5.3.1.3. Gradient Decorator

Gradient background (horizontal or vertical).

```json
{
  "type": "gradient-horizontal",
  "color": ["0xFFFF0000", "0xFF00FF00"]
}
```


##### 5.3.1.4. Texture Decorator

Texture decorator for complex visual effects.

```json
{
  "type": "texture",
  "path": "chromatictooltips:textures/tooltip_bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```


#### 5.3.2. "color" format for `background`/`border`/`gradient-horizontal`/`gradient-vertical`

Possible options:

**1) Single color**
```json
"color": 16777215
```
→ becomes array of 4 corners: `[ lt, rt, rb, lb ]`

**2) Two colors**
```json
"color": [ 10, 20 ]
```
→ `[ 10, 10, 20, 20 ]`

**3) Three colors**
```json
"color": [ c1, c2, c3 ]
```
→ `[ c1, c2, c3, c1 ]`

**4) Four colors**
```json
"color": [ lt, rt, rb, lb ]
```
(as is)

**For gradient-horizontal / gradient-vertical**

If array is not specified:
```json
"color": "<int>"
```

generates:
```
[ transparent, 60% opacity, full opacity, 60% opacity, transparent]
```
> gradient direction depends on its type

### 5.4 TooltipTexture — textures

**General structure:**

```json
{
  "path": "<string>",           // path to texture
  "color": "<int | color>",     // color (ARGB), e.g. 0xFFFFFFFF
  "region": {
    "x": "<int>",
    "y": "<int>",
    "width": "<int>",
    "height": "<int>"
  },

  // repeat / slice systems ↓ see below
  "repeat": "...",
  "slice": "[...]",

  // Animation ↓ see below
  "animation": { ... }
}
```

**Example:**

```json
{
  "path": "chromatictooltips:textures/bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```

#### 5.4.1. Region
Supported formats:

```json
{
    "region": 4,
    "region": [4, 8, 4, 8],
    "region": { "x": 4, "y": 6, "width": 4, "height": 6 },
    "regionX": 4,
    "regionWidth": 6
}
```


#### 5.4.2. Repeat

Supported formats:

```json
{
    "repeat": true,
    "repeat": { "inline": true },
    "repeatInline": true,
}
```

The code supports 4 formats:

### ✔ 1) Boolean
```json
"repeat": true
```

Equivalent to:
```json
{ "limit": 2147483647, "gap": 0, "fit": "floor" }
```

### ✔ 2) Number
```json
"repeat": 4
```

Equivalent to:
```json
{ "limit": 4, "gap": 0, "fit": "floor" }
```

### ✔ 3) String
```json
"repeat": "ceil"
```

Equivalent to:
```json
{ "limit": 2147483647, "gap": 0, "fit": "ceil" }
```

The `fit` field accepts:
- `"floor"`
- `"ceil"`
- `"clip"`
- `"stretch"`

### ✔ 4) Object
```json
"repeat": {
  "limit": "<int>",                // maximum number of repetitions
  "gap": "<int>",                  // distance between blocks
  "fit": "floor|ceil|clip|stretch"
}
```

> **Note:** If repeat is not specified — the system uses slice.


#### 5.4.3. Slice

Supported formats:

```json
{
    "slice": [ 10, 5, 20 ],
    "slice": { "inline": [ 10, 5, 20 ] },
    "sliceInline": [ 10, 5, 20 ],
}
```

Each slice is a pair of numbers:
```
[ base, grow ]
```

- **base** — fixed size of the section
- **grow** — "growth factor" (0 or >0), determines if the section can be stretched

Slice can accept values:

### ✔ 1) Number
```json
"slice": [ 10, 5, 20 ]
```

Equivalent to:
```json
{ "base": 10 }, { "base": 5 }, { "base": 20 }
```

### ✔ 2) Boolean
```json
"slice": [ true, false ]
```

Equivalent to:
```json
{ "grow": 1 }, { "grow": 0 }
```

### ✔ 3) Object
```json
{
  "base": "<int>",            // fixed size
  "grow": "<int|bool>"        // >=0 or true/false
}
```

### Example:
```json
"slice": [
  { "base": 5, "grow": 1 },
  { "grow": true },
  { "base": 10 }
]
```

> **Note:** If slice is empty — fallback is used: `[ [ defaultSize, 1 ] ]`


#### 5.4.4. Animation (animation)

Animation describes a vertical sprite sheet (each frame is by block height).

```json
"animation": {
  "frametime": "<int>",             // default frame duration
  "frames": [ ... ],              // list of frames
  "framePingPong": "<boolean>"      // if frames are not present
}
```

### frames format

Frames can be:

#### ✔ 1) Number
```json
"frames": [0, 1, 2, 3]
```

Each frame has:
- `index = value`
- `time = frametime`

#### ✔ 2) Object
```json
"frames": [
  { "index": 0, "time": 2 },
  { "index": 3, "time": 10 }
]
```

### framePingPong

If there are no frames:
```json
"framePingPong": true
```

Creates sequence: `0,1,2,3,2,1`

> Time is `frametime`.

#### 5.4.5. Full JSON example

```json
{
  "path": "textures/tooltip.png",
  "color": 4294967295,
  "region": {
    "x": 0,
    "y": 0,
    "width": 32,
    "height": 32
  },
  "repeatInline": {
    "limit": 999,
    "gap": 1,
    "fit": "stretch"
  },
  "repeatBlock": "ceil",
  "slice": {
    "inline": [
      { "base": 4, "grow": true },
      8,
      { "grow": false }
    ],
    "block": [
      6,
      { "base": 10, "grow": 1 }
    ],
  },

  "animation": {
    "frametime": 2,
    "frames": [
      0,
      { "index": 1, "time": 4 },
      2,
      { "index": 3, "time": 6 }
    ]
  }
}
```

### 5.5 TooltipTransform — animations

```json
{
  "duration": 600,
  "function": "easeInOut",
  "originInline": "center",
  "originBlock": "center",
  "keyframes": [
    { "progress": 0, "scale": 0.9 },
    { "progress": 100, "scale": 1.0 }
  ]
}
```

#### Automatic completion:
If the last keyframe doesn't have `progress: 100`, it's automatically added:
```json
{ "progress": 100, "translateX": 0, "translateY": 0, "scale": 1, "rotate": 0 }
```

> **Important:** If transform has no keyframes → considered NOT animated → ignored by components.

#### Pulsing tooltip example

```json
{
  "transform": {
    "duration": 800,
    "iterationCount": -1,
    "pingPong": true,
    "keyframes": [
      { "progress": 0, "scale": 1.0 },
      { "progress": 100, "scale": 1.05 }
    ]
  }
}
```

## 6. Alignment and dimensions

### Alignment:

#### Inline (horizontal)
```json
"alignInline": "left|center|right"
// or alias
"align": {"inline": "center"}
```

#### Block (vertical)
```json
"alignBlock": "top|center|bottom"
// or alias
"align": {"block": "center"}
```

### Minimum dimensions:
```json
{
  "minWidth": 80,
  "minHeight": 20
}
```

## 7. Basic tooltip sections

In the Chromatic Tooltips system, basic sections define the structure and placement of information within tooltips. Each section can contain various components and styles, and can be extended through Enrichers.

### List of basic sections:

1. **header**
  Top part of the tooltip. Usually contains the item name or title. Components like `title` are added here, as well as any additional elements that should be displayed at the beginning of the tooltip.

2. **body**
  Main part of the tooltip. Here detailed information about the item, its properties, description, quantity, hotkeys, Ore Dictionary and other content-related sections are displayed.

3. **footer**
  Bottom part of the tooltip. Used to display information about the mod that added the item, as well as other auxiliary data that should be at the end of the tooltip.

4. **navigation**
  Additional section for navigation elements. Drawn only if the tooltip doesn't fit on screen.

Each section is configured through JSON styles and can be supplemented or modified using Enrichers and configuration files.


## 8. Enrichers

Enrichers are a system of components that add various sections to tooltips. Each enricher defines where and when its section should be displayed.

### 8.0. Enricher Settings

Each enricher can be configured through the following parameters:

#### 8.0.1. Place (Location)

Defines which part of the tooltip the section will be displayed in:

- **HEADER** — in the tooltip header (top)
- **BODY** — in the tooltip body (main part)
- **FOOTER** — in the tooltip footer (bottom)

#### 8.0.2. Mode (Display mode)

Defines the conditions under which the section will be shown:

- **NONE** — section never displays
- **ALWAYS** — section displays always
- **DEFAULT** — section displays by default (without pressed keys or if there are no sections for the pressed key)
- **SHIFT** — section displays only when Shift is pressed
- **CTRL** — section displays only when Ctrl is pressed
- **ALT** — section displays only when Alt is pressed

Modes can be configured through configuration files using the key `sections.<sectionId>.modes`.

List of default sections:
1. title
1. amount
1. hotkeys:help-text
1. hotkeys
1. oreDictionary
1. itemInfo
1. contextInfo
1. modInfo

### 8.1. title

**Place:** HEADER
**Mode:** ALWAYS

Applies to the first line of the tooltip, or to the item name. You can use all styles that `SectionBox` accepts.

Triggers `ItemTitleEnricherEvent` through which other mods can edit `displayName` if it's an item.

### 8.2. amount

**Place:** BODY
**Mode:** SHIFT

Applies to tooltips that belong to an item. You can use all styles that `SectionBox` accepts. Displayed when `Shift` is pressed.

Triggers `StackSizeEnricherEvent` through which other mods can edit `stackSize` and `fluid` to handle it as a liquid.

**Settings:**
- `stackSizeEnricherEnabled` — allows disabling this section
- `playerInventoryStackSizeEnabled` — when hovering over an item in the player's inventory, will show not only the quantity under the cursor, but also the total quantity of items of this type in the player's inventory

### 8.3. hotkeys:help-text

**Place:** BODY
**Mode:** DEFAULT (when there are hotkeys to display)

Displays a hint that you need to press Alt to view hotkeys.

**Settings:**
- `hotkeysHelpTextEnabled` — allows disabling the hint

### 8.4. hotkeys

**Place:** BODY
**Mode:** ALT

Applies to tooltips that belong to an item. You can use all styles that `SectionBox` accepts.

Triggers `HotkeyEnricherEvent` through which other mods can add their hotkeys for display. The tooltip itself has no base hotkeys. Displays list when `Alt` is pressed.

**Settings:**
- `hotkeysEnricherEnabled` — allows disabling this section

### 8.5. oreDictionary

**Place:** BODY
**Mode:** CTRL

Displays a list of Ore Dictionary names for the item.

### 8.6. itemInfo

**Place:** BODY
**Mode:** DEFAULT

Applies to tooltips that belong to an item. You can use all styles that `SectionBox` accepts. Displays item tooltip.

Triggers `ItemInfoEnricherEvent` through which other mods can add their lines to this section.

### 8.7. contextInfo

**Place:** BODY
**Mode:** DEFAULT

Applies to tooltips that belong to an item. You can use all styles that `SectionBox` accepts. Displays lines that were passed to the tooltip.

### 8.8. modInfo

**Place:** FOOTER
**Mode:** ALWAYS

Applies to tooltips that belong to an item. You can use all styles that `SectionBox` accepts. Displays information about the mod that added the item.

## 9. Configuration

Each enricher can be configured through configuration files, changing its display modes and location:

```json
{
    "sections": {
        "amount": {
            "modes": ["SHIFT", "CTRL"],
            "place": "BODY"
        },
        "hotkeys": {
            "modes": ["ALT"],
            "place": "BODY"
        },
        "modInfo": {
            "modes": ["DEFAULT"],
            "place": "FOOTER"
        }
    }
}
```

## 10. Extension by other mods

Creating a handler that will enrich the tooltip with its own sections:

```java
public class CustomEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "custom_section"; // Unique section ID
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY; // Where to display the section
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.SHIFT); // When to display
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        // Logic for creating section components
        return components;
    }
}
```

Registering an enricher:

```java
TooltipHandler.addEnricher(String id, ITooltipEnricher enricher)
```

## 11. Tooltip calls in other mods

```java
TooltipHandler.drawHoveringText(List<?> textLines)
TooltipHandler.drawHoveringText(ItemStack stack, List<?> textLines)
TooltipHandler.drawHoveringText(TooltipRequest request)
```