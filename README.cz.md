# Chromatic Tooltips

🌐 **Jazyky:** [English](README.md) | [Русский](README.ru.md) | [Čeština](README.cz.md)

**Chromatic Tooltips** je klientský mod pro Minecraft, který přetváří tooltipy na plně přizpůsobitelný UI systém.
Veškerá konfigurace se provádí pomocí resource packů přes deklarativní JSON, bez psaní Java kódu a rekompilace.

# Instalace
1. Nainstaluj  [ChromaticTooltips](https://github.com/slprime/ChromaticTooltips/releases)
1. Nainstaluj  [ChromaticTooltipsCompat](https://github.com/slprime/ChromaticTooltipsCompat/releases)
1. Vyber a nainstaluj jeden z resource packů uvedených v [prvním issue](https://github.com/slprime/ChromaticTooltips/issues/1)

## 1. Přehled — co to je

Chromatic Tooltips ti umožňuje:

- měnit pozadí, okraje a textury tooltipů
- nastavovat odsazení, velikosti a zarovnání
- přidávat animace a transformace
- aplikovat styly podmíněně: podle itemu, vzácnosti, tagů, NBT atd.

Mod je určen jak pro jednoduché vizuální úpravy, tak pro složité UI kompozice.

## 2. Rychlý start — první styl za 5 minut

### Co vytvoříš

Styl tooltipu pro itemy s vzácností rare:

- poloprůhledné pozadí
- zlatý okraj
- animace při zobrazení

### Struktura resource packu

```
.minecraft/resourcepacks/my_tooltips/
└── assets/
    └── chromatictooltips/
        └── tooltip.json
```

### Minimální příklad

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
        "context": "item",
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

### Co jsi napsal

1. první blok se styly se aplikuje na všechny tooltipy
2. druhý blok se aplikuje pouze na itemy s `rarity:rare`

### Testování ve hře

1. Ulož soubor
2. Stiskni **F3 + T**
3. Najeď myší na vzácný item


## 3. Styly — systém aplikace

### Obecný formát

```json
{
  "styles": [
    {
      "context": "item | default | <custom>",
      "filter": "...",

      "offsetMain": "<int>",
      "offsetCross": "<int>",

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

nebo `style`, pokud nemáš více tooltipů

```json
{
    "style": {
        "context": "item | default | <custom>",
        "filter": "...",

        "offsetMain": "<int>",
        "offsetCross": "<int>",

        "<SectionBox>"
    }
}
```

### Filtry

Přijímá řetězec s následujícími tokeny:

##### Části:

```
modname:itemid          - identifikátor: odpovídá jakékoli části cíle, takže minecraft:lava odpovídá minecraft:lava_bucket
$orename                - ore dictionary: odpovídá jakékoli části cíle, takže $ingot odpovídá ingotIron, ingotGold atd.
tag.color=red           - tag
rarity:common           - vzácnost
0 or 0-12               - poškození
```

##### Modifikátory:

```
! - logické NE. vyloučí itemy odpovídající následujícímu výrazu (!minecraft:portal)
r/.../ - standardní java regex (r/^m\w{6}ft$/ = minecraft)
, - logické NEBO v tokenu (minecraft:potion 16384-16462,!16386)
| - logické NEBO pro více itemů (wrench|hammer)
```

##### Příklad:

```
example: minecraft:potion 16384-16462,!16386 | $oreiron | tag.color=red
```

## 4. SectionBox — základní layoutová jednotka

**SectionBox** je jádro celého systému.
Každý tooltip, sekce nebo element je SectionBox.

### Plný formát

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
  "minHeight": "<int>",

  "order": "<int>",

  "spacing": "<int>",
  "sectionSpacing": "<int>"
}
```

## 5. Komponenty

### 5.1 TooltipSpacing — odsazení (padding/margin)

Podporované formáty:

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

### 5.2 TooltipFontContext — typografie

Podporované formáty:

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

### Pole

| Pole | Typ | Popis | Alias |
|-------|------|-------------|-------|
| `fontShadow` | boolean | Stín písma | `font.shadow` |
| `fontParagrath` | int | Výška prázdného řádku | `font.paragrath` |
| `fontColors` | object | Vlastní barvy písma | `font.colors` |

### fontColors / font.colors

Podporuje 32 barev (0–15 základních, 16–31 stíny základních fontů):

- `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`
- `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

#### Formáty

**jedna barva**
```json
"red": "0xFF0000"
```
> Ztlumená barva se vypočítá automaticky.

**Dvě barvy [normální, stín]**
```json
"red": ["0xFF0000", "0x802020"]
```

### 5.3 TooltipDecorator — vizuální elementy

**Obecná struktura:**

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

#### 5.3.1. Pole "type" a chování

| type | co se vytvoří | povinná pole |
|------|-----------------|----------------|
| `"none"` | nic | — |
| `"background"` | barevný obdélník | `"color"`, `"thickness"`, `"corner"` |
| `"border"` | okraj | `"color"`, `"thickness"`, `"corner"` |
| `"texture"` | vykreslení textury | všechna pole TooltipTexture |
| `"item"` | vykreslení itemu | žádná speciální pole |
| `"gradient-horizontal"` | horizontální přechod | pole `"color"` |
| `"gradient-vertical"` | vertikální přechod | pole `"color"` |


##### 5.3.1.1. Background Decorator

Obdélníkové pozadí s nastavitelnou barvou.

```json
{
  "type": "background",
  "color": "0x80202020"
}
```

##### 5.3.1.2. Border Decorator (Okrajový dekorátor)

Okraj kolem tooltipu.

```json
{
  "type": "border",
  "color": "0xFFFFFFFF",
  "thickness": 1,
  "corner": true
}
```

##### 5.3.1.3. Gradient Decorator (Přechodový dekorátor)

Přechodové pozadí (horizontální nebo vertikální).

```json
{
  "type": "gradient-horizontal",
  "color": ["0xFFFF0000", "0xFF00FF00"]
}
```


##### 5.3.1.4. Texture Decorator (Texturový dekorátor)

Dekorátor textury pro složité vizuální efekty.

```json
{
  "type": "texture",
  "path": "chromatictooltips:textures/tooltip_bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```


#### 5.3.2. Formát "color" pro `background`/`border`/`gradient-horizontal`/`gradient-vertical`

Možné varianty:

**1) Jedna barva**
```json
"color": 16777215
```
→ stane se polem 4 rohů: `[ lt, rt, rb, lb ]`

**2) Dvě barvy**
```json
"color": [ 10, 20 ]
```
→ `[ 10, 10, 20, 20 ]`

**3) Tři barvy**
```json
"color": [ c1, c2, c3 ]
```
→ `[ c1, c2, c3, c1 ]`

**4) Čtyři barvy**
```json
"color": [ lt, rt, rb, lb ]
```
(tak jak jsou)

**Pro gradient-horizontal / gradient-vertical**

Pokud není zadáno pole:
```json
"color": "<int>"
```

vygeneruje se:
```
[ transparent, 60% opacity, full opacity, 60% opacity, transparent]
```
([průhledná, 60% průhlednost, plná průhlednost, 60% průhlednost, průhledná ])

> směr přechodu závisí na jeho typu

### 5.4 TooltipTexture — textury

**Obecná struktura:**

```json
{
  "path": "<string>",           // cesta k textuře
  "color": "<int | color>",     // barva (ARGB), např. 0xFFFFFFFF
  "region": {
    "x": "<int>",
    "y": "<int>",
    "width": "<int>",
    "height": "<int>"
  },

  // opakovací / řezací systémy ↓ viz. níž
  "repeat": "...",
  "slice": "[...]",

  // Animace ↓ viz. níž
  "animation": { ... }
}
```

**Příklad:**

```json
{
  "path": "chromatictooltips:textures/bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```

#### 5.4.1. Region
Podporované formáty:

```json
{
    "region": 4,
    "region": [4, 8, 4, 8],
    "region": { "x": 4, "y": 6, "width": 4, "height": 6 },
    "regionX": 4,
    "regionWidth": 6
}
```


#### 5.4.2. Repeat (Opakování)

Podporované formáty:

```json
{
    "repeat": true,
    "repeat": { "inline": true },
    "repeatInline": true,
}
```

Kód podporuje 4 typy formátu:

### ✔ 1) Boolean
```json
"repeat": true
```

Je stejné jako:
```json
{ "limit": 2147483647, "gap": 0, "fit": "floor" }
```

### ✔ 2) Číslo
```json
"repeat": 4
```

Je stejné jako:
```json
{ "limit": 4, "gap": 0, "fit": "floor" }
```

### ✔ 3) String
```json
"repeat": "ceil"
```

Je stejné jako:
```json
{ "limit": 2147483647, "gap": 0, "fit": "ceil" }
```

Pole `fit` přijímá:
- `"floor"`
- `"ceil"`
- `"clip"`
- `"stretch"`

### ✔ 4) Objekt
```json
"repeat": {
  "limit": "<int>",                // maximální počet opakování
  "gap": "<int>",                  // vzdálenost mezi bloky
  "fit": "floor|ceil|clip|stretch"
}
```

> **Poznámka:** Pokud repeat není zadáno — systém použije slice.


#### 5.4.3. Slice (Řezy)

Podporované formáty:

```json
{
    "slice": [ 10, 5, 20 ],
    "slice": { "inline": [ 10, 5, 20 ] },
    "sliceInline": [ 10, 5, 20 ],
}
```

Každý řez je dvojice čísel:
```
[ base, grow ]
```

- **base** — pevná velikost části
- **grow** — "faktor růstu" (0 nebo >0), určuje zda se část může roztáhnout

Řez může přijímat:

### ✔ 1) Číslo
```json
"slice": [ 10, 5, 20 ]
```

Je stejné jako:
```json
{ "base": 10 }, { "base": 5 }, { "base": 20 }
```

### ✔ 2) Boolean
```json
"slice": [ true, false ]
```

Je stejné jako:
```json
{ "grow": 1 }, { "grow": 0 }
```

### ✔ 3) Objekt
```json
{
  "base": "<int>",            // pevná velikost
  "grow": "<int|bool>"        // >=0 nebo true/false
}
```

### Příklad:
```json
"slice": [
  { "base": 5, "grow": 1 },
  { "grow": true },
  { "base": 10 }
]
```

> **Poznámka:** Pokud je slice prázdné — použije se fallback: `[ [ defaultSize, 1 ] ]`


#### 5.4.4. Animation (Animace)

Animace popisuje vertikální sprite sheet (každý snímek má výšku bloku).

```json
"animation": {
  "frametime": "<int>",             // základní délka jednoho snímku
  "frames": [ ... ],              // pole snímků
  "framePingPong": "<boolean>"      // pokud nejsou snímky přítomny
}
```

### Formát frames (snímků)

Snímky mohou být:

#### ✔ 1) Číslo
```json
"frames": [0, 1, 2, 3]
```

Každý snímek má:
- `index = value`
- `time = frametime`

#### ✔ 2) Objekt
```json
"frames": [
  { "index": 0, "time": 2 },
  { "index": 3, "time": 10 }
]
```

### framePingPong

Pokud nejsou žádné snímky:
```json
"framePingPong": true
```

Vytvoří řadu: `0,1,2,3,2,1`

> Čas je `frametime`.

#### 5.4.5. Celý JSON příklad

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

### 5.5 TooltipTransform — animace

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

#### Automatické dokončení:
Pokud poslední keyframe nemá `progress: 100`, automaticky se přidá:
```json
{ "progress": 100, "translateX": 0, "translateY": 0, "scale": 1, "rotate": 0 }
```

> **Důležité:** Pokud transform nemá žádné keyframes → považuje se za NEanimovaný → komponenty ho ignorují.

#### Příklad pulsujícího tooltipu

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

## 6. Zarovnání a rozměry

### Zarovnání:

#### Inline (horizontální)
```json
"alignInline": "left|center|right"
// nebo alias
"align": {"inline": "center"}
```

#### Block (vertikální)
```json
"alignBlock": "top|center|bottom"
// nebo alias
"align": {"block": "center"}
```

### Minimální rozměry:
```json
{
  "minWidth": 80,
  "minHeight": 20
}
```

## 7. Základní sekce tooltipu

V systému Chromatic Tooltips základní sekce definují strukturu a umístění informací v rámci tooltipů. Každá sekce může obsahovat různé komponenty a styly a lze ji rozšířit pomocí Enricherů.

### Seznam základních sekcí:

1. **header**
  Horní část tooltipu. Obvykle obsahuje název itemu nebo nadpis. Komponenty jako `title` jsou zde přidány, stejně jako jakékoliv další věci, které by měly být zobrazeny na začátku tooltipu.

2. **body**
  Hlavní část tooltipu. Zobrazují se zde detailní informace o itemu, jeho vlastnosti, popis, množství, zkratky, Ore Dictionary a další.

3. **footer**
  Spodní část tooltipu. Zobrazuje informace o modu, který item přidal, nebo další data, které by měly být na spodku.

4. **navigation**
  Doplňková sekce pro navigační elementy. Vykresluje se pouze pokud se tooltip nevejde na obrazovku.

Každá sekce se konfiguruje přes JSON styly a lze ji doplnit nebo upravit pomocí Enricherů a konfiguračních souborů.


## 8. Enrichery

Enrichery jsou systém komponent, které přidávají různé sekce do tooltipů. Každý enricher definuje kde a kdy se jeho sekce zobrazí.

### 8.0. Nastavení Enricheru

Každý enricher může být nastaven pomocí těchto parametrů:

#### 8.0.1. Place (Umístění)

Definuje ve které části tooltipu se sekce zobrazí:

- **HEADER** — v hlavičce tooltipu (nahoře)
- **BODY** — v těle tooltipu (hlavní část)
- **FOOTER** — v patičce tooltipu (dole)

#### 8.0.2. Mode (Režim zobrazení)

Definuje za jakých podmínek se sekce zobrazí:

- **NONE** — sekce se nikdy nezobrazí
- **ALWAYS** — sekce se zobrazuje vždy
- **DEFAULT** — sekce se zobrazuje výchozně (bez stisknutých kláves nebo pokud neexistuje sekce pro stisknutou klávesu)
- **SHIFT** — sekce se zobrazí pouze při stisknutém Shiftu
- **CTRL** — sekce se zobrazí pouze při stisknutém Ctrl
- **ALT** — sekce se zobrazí pouze při stisknutém Altu

Režimy mohou být nakonfigurovány skrze konfigurační soubory pomocí klíče `sections.<sectionId>.modes`.

Sehnam základních sekcí:
1. title
1. stacksize
1. keyboard-modifier
1. hotkeys
1. oreDictionary
1. itemInfo
1. contextInfo
1. modInfo

### 8.1. title

**Umístění:** HEADER
**Režim:** ALWAYS

Aplikuje se na první řádek tooltipu, nebo na název itemu. Jsou použitelné všechny styly, které `SectionBox` přijímá.

Spouští `ItemTitleEnricherEvent`, přes který mohou jiné mody upravit `displayName`, pokud je to item.

### 8.2. stacksize

**Umístění:** BODY
**Režim:** SHIFT

Zobrazuje se při stisknutém Shiftu. Jsou použitelné všechny styly, které `SectionBox` přijímá. Zobrazeno, pokud je `Shift` zmáčknut.

Spouští `StackSizeEnricherEvent`, přes který mohou jiné mody upravit `stackSize` a `fluid`, pro pracování s kapalinou.

**Nastavení:**
- `stackSizeEnricherEnabled` — umožňuje tuto sekci vypnout
- `playerInventoryStackSizeEnabled` — při najetí na item v inventáři hráče zobrazí nejen množství pod kurzorem, ale i celkové množství itemů tohoto typu v inventáři

### 8.3. keyboard-modifier

**Umístění:** BODY
**Režim:** DEFAULT (pokud existují zkratky k zobrazení)

Zobrazuje nápovědu, že je třeba stisknout Alt/Shift/Ctrl.

### 8.4. hotkeys

**Umístění:** BODY
**Režim:** ALT

Použije se na tooltipy, které patří itemu. Jsou použitelné všechny styly, které `SectionBox` přijímá.

Spouští `HotkeyEnricherEvent`, přes který mohou jiné mody přidat vlastní zkrtku pro zobrazení. Tooltip sám o sobě nemá základní zkratku. Ukazuje seznam, pokud je `Alt` zmáčknut.

**Nastavení:**
- `hotkeysEnricherEnabled` — umožňuje tuto sekci vypnout

### 8.5. oreDictionary

**Umístění:** BODY
**Režim:** CTRL

Zobrazuje seznam názvů z Ore Dictionary pro daný item.

### 8.6. itemInfo

**Umístění:** BODY
**Režim:** DEFAULT

Aplikován pro tooltipy, které patří itemu. Jsou použitelné všechny styly, které `SectionBox` přijímá. Zobrazuje tooltip itemů.

Spouští `ItemInfoEnricherEvent`, přes který mohou jiné mody přidat vlastní řádky.

### 8.7. fluidInfo

**Umístění:** BODY
**Režim:** DEFAULT

Aplikován pro tooltipy, které patří kapalině. Jsou použitelné všechny styly, které `SectionBox` přijímá. Zobrazuje tooltip kapaliny.

Spouští `FluidInfoEnricherEvent`, přes který mohou jiné mody přidat vlastní řádky.

### 8.8. contextInfo

**Umístění:** BODY
**Režim:** DEFAULT

Aplikován pro tooltipy, které patří itemu. Jsou použitelné všechny styly, které `SectionBox` přijímá. Zobrazuje řádky, které byly odeslány tooltipu.

### 8.9. modInfo

**Umístění:** FOOTER
**Režim:** ALWAYS

Aplikován pro tooltipy, které patří kapalině. Jsou použitelné všechny styly, které `SectionBox` přijímá. Zobrazuje informace o modu, který item přidal.

## 9. Konfigurace

Každý enricher může být nastaven skrze konfigurační soubory, měnící jeho zobrazení a umístění:

```json
{
    "sections": {
        "stacksize": {
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

## 10. Rozšíření jinými mody

Vytvoření handleru, který obohatí tooltip vlastními sekcemi:

```java
public class CustomEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "custom_section"; // Jedinečné ID sekce
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY; // Kde zobrazit sekci
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.SHIFT); // Kdy zobrazit
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        // Logika pro tvrobu komponentů sekcí
        return components;
    }
}
```

Registrace enricheru:

```java
TooltipHandler.addEnricher(String id, ITooltipEnricher enricher)
```

## 11. Volání tooltipů v jiných modech

```java
TooltipHandler.drawHoveringText(List<?> textLines)
TooltipHandler.drawHoveringText(ItemStack stack, List<?> textLines)
TooltipHandler.drawHoveringText(TooltipRequest request)
```