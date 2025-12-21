# Chromatic Tooltips

🌐 **Языки:** [English](README.md) | [Русский](README.ru.md)

**Chromatic Tooltips** — клиентский мод для Minecraft, который превращает tooltip'ы в полностью настраиваемую UI-систему.
Вся кастомизация выполняется через ресурспаки с помощью декларативного JSON, без Java-кода и пересборки.

## 1. Overview — Что это такое

Chromatic Tooltips позволяет:

- менять фон, рамки и текстуры tooltip'ов
- настраивать отступы, размеры и выравнивание
- добавлять анимации и трансформации
- применять стили условно: по предметам, редкости, тегам, NBT и т.д.

Мод ориентирован как на простые визуальные правки, так и на сложные UI-композиции.

## 2. Quick Start — первый стиль за 5 минут

### Что вы создадите

Стиль tooltip'а для предметов с редкостью rare:

- полупрозрачный фон
- золотую рамку
- анимацию появления

### Структура ресурспака

```
.minecraft/resourcepacks/my_tooltips/
└── assets/
    └── chromatictooltips/
        └── tooltip.json
```

### Минимальный пример

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

### Что вы написали

1. первый блок со стилями применится ко всем тултипам
2. второй блок применится только к предметам с `rarity:rare`

### Проверка в игре

1. Сохраните файл
2. Нажмите **F3 + T**
3. Наведите курсор на rare-предмет


## 3. Styles — система применения

### Общий формат

```json
{
  "styles": [
    {
      "type": "item | default | <custom>",
      "filter": { ... },

      "offsetMain": "<int>",
      "offsetCross": "<int>",

      "<SectionBox>"
    }
  ]
}
```

или `style` если у вас нет несколько тултипов

```json
{
    "style": {
        "type": "item | default | <custom>",
        "filter": { ... },

        "offsetMain": "<int>",
        "offsetCross": "<int>",

        "<SectionBox>"
    }
}
```

### Фильтры

Принимает строку со следуюзими токенами:

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

## 4. SectionBox — базовая единица layout'а

**SectionBox** — ядро всей системы.
Любой tooltip, секция или элемент — это SectionBox.

### Полный формат

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

## 5. Компоненты

### 5.1 TooltipSpacing — отступы (padding/margin)

Поддерживаемые форматы:

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

### 5.2 TooltipFontContext — типографика

Поддерживаемые форматы:

```json
{
  "defaultColor": "0xFFFFFFFF",
  "fontShadow": true,
  "fontColors": {
    "gold": ["0xFFFFD700", "0xFFB8860B"]
  }
}
```

**Alias**

```json
{
    "font": {
        "defaultColor": "0xFFFFFFFF",
        "shadow": true,
        "colors": {
            "gold": ["0xFFFFD700", "0xFFB8860B"]
        }
    }
}
```

### Поля

| Поле | Тип | Описание | Алиас |
|------|-----|----------|-------|
| `defaultColor` | int, color | Цвет по умолчанию | — |
| `fontShadow` | boolean | Тень на шрифте | `font.shadow` |
| `fontColors` | объект | Кастомные цвета шрифтов | `font.colors` |

### fontColors / font.colors

Поддерживаются 32 цвета (0–15 базовые, 16–31 тени у базовых шрифтов):

- `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`
- `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

#### Форматы

**Один цвет**
```json
"red": "0xFF0000"
```
> Приглушённый цвет вычисляется автоматически.

**Два цвета [normal, shadow]**
```json
"red": ["0xFF0000", "0x802020"]
```

### 5.3 TooltipDecorator — визуальные элементы

**Общая структура:**

```json
{
  "type": "background|border|gradient-horizontal|gradient-vertical|texture|item",
  "margin": "<TooltipSpacing>",          // см. раздел 4
  "alignInline": "left|center|right|start|end",
  "alignBlock": "top|center|bottom|start|end",
  "width": "<int>",
  "height": "<int>",

  // если type = RECTANGLE или BORDER
  "color": "<color or array>",

  // если type = BORDER
  "thickness": "<int>",
  "corner": "<boolean>",

  // если type = TEXTURE
  "path": "<string>",           // + остальные поля TooltipTexture

  "transform": { ... }          // TooltipTransform JSON
}
```

#### 5.3.1. Поле "type" и поведение

| type | что создаётся | нужные поля |
|------|---------------|-------------|
| `"none"` | ничего | — |
| `"background"` | цветной прямоугольник | `"color"`, `"thickness"`, `"corner"` |
| `"border"` | рамка | `"color"`, `"thickness"`, `"corner"` |
| `"texture"` | рендер текстуры | все поля TooltipTexture |
| `"item"` | рендер предмета | нет спец-полей |
| `"gradient-horizontal"` | горизонтальный градиент | `"color"` массив |
| `"gradient-vertical"` | вертикальный градиент | `"color"` массив |


##### 5.3.1.1. Background Decorator

Прямоугольный фон с настраиваемым цветом.

```json
{
  "type": "rectangle",
  "color": "0x80202020"
}
```

##### 5.3.1.2. Border Decorator

Рамка вокруг tooltip'а.

```json
{
  "type": "border",
  "color": "0xFFFFFFFF",
  "thickness": 1,
  "corner": true
}
```

##### 5.3.1.3. Gradient Decorator

Градиентный фон (горизонтальный или вертикальный).

```json
{
  "type": "gradient-horizontal",
  "color": ["0xFFFF0000", "0xFF00FF00"]
}
```


##### 5.3.1.4. Texture Decorator

Текстурный декоратор для сложных визуальных эффектов.

```json
{
  "type": "texture",
  "path": "chromatictooltips:textures/tooltip_bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```


#### 5.3.2. Формат "color" для `background`/`border`/`gradient-horizontal`/`gradient-vertical`

Возможные варианты:

**1) Один цвет**
```json
"color": 16777215
```
→ превращается в массив из 4 углов: `[ lt, rt, rb, lb ]`

**2) Два цвета**
```json
"color": [ 10, 20 ]
```
→ `[ 10, 10, 20, 20 ]`

**3) Три цвета**
```json
"color": [ c1, c2, c3 ]
```
→ `[ c1, c2, c3, c1 ]`

**4) Четыре цвета**
```json
"color": [ lt, rt, rb, lb ]
```
(как есть)

**Для gradient-horizontal / gradient-vertical**

Если массив не задан:
```json
"color": "<int>"
```

генерируется:
```
[ transparent, 60% opacity, full opacity, 60% opacity, transparent]
```
> направление градиента зависит от его типа

### 5.4 TooltipTexture — текстуры

**Общая структура:**

```json
{
  "path": "<string>",           // путь к текстуре
  "color": "<int | color>",     // цвет (ARGB), например 0xFFFFFFFF
  "region": {
    "x": "<int>",
    "y": "<int>",
    "width": "<int>",
    "height": "<int>"
  },

  // Системы repeat / slice ↓ см. ниже
  "repeat": "...",
  "slice": "[...]",

  // Анимация ↓ см. ниже
  "animation": { ... }
}
```

**Пример:**

```json
{
  "path": "chromatictooltips:textures/bg.png",
  "repeat": true,
  "slice": [4, true, 4]
}
```

#### 5.4.1. Region
Поддерживаемые форматы:

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

Поддерживаемые форматы:

```json
{
    "repeat": true,
    "repeat": { "inline": true },
    "repeatInline": true,
}
```

Код поддерживает 4 формата:

### ✔ 1) Boolean
```json
"repeat": true
```

Эквивалентно:
```json
{ "limit": 2147483647, "gap": 0, "fit": "floor" }
```

### ✔ 2) Number
```json
"repeat": 4
```

Эквивалентно:
```json
{ "limit": 4, "gap": 0, "fit": "floor" }
```

### ✔ 3) String
```json
"repeat": "ceil"
```

Эквивалентно:
```json
{ "limit": 2147483647, "gap": 0, "fit": "ceil" }
```

Поле `fit` принимает:
- `"floor"`
- `"ceil"`
- `"clip"`
- `"stretch"`

### ✔ 4) Object
```json
"repeat": {
  "limit": "<int>",                // максимальное число повторов
  "gap": "<int>",                  // расстояние между блоками
  "fit": "floor|ceil|clip|stretch"
}
```

> **Примечание:** Если repeat не задан — система использует slice.


#### 5.4.3. Slice

Поддерживаемые форматы:

```json
{
    "slice": [ 10, 5, 20 ],
    "slice": { "inline": [ 10, 5, 20 ] },
    "sliceInline": [ 10, 5, 20 ],
}
```

Каждый slice — это пара чисел:
```
[ base, grow ]
```

- **base** — фиксированный размер участка
- **grow** — "доля роста" (0 или >0), определяет, можно ли растягивать участок

Slice может принимать значения:

### ✔ 1) Число
```json
"slice": [ 10, 5, 20 ]
```

Эквивалентно:
```json
{ "base": 10 }, { "base": 5 }, { "base": 20 }
```

### ✔ 2) Boolean
```json
"slice": [ true, false ]
```

Эквивалентно:
```json
{ "grow": 1 }, { "grow": 0 }
```

### ✔ 3) Объект
```json
{
  "base": "<int>",            // фиксированный размер
  "grow": "<int|bool>"        // >=0 или true/false
}
```

### Пример:
```json
"slice": [
  { "base": 5, "grow": 1 },
  { "grow": true },
  { "base": 10 }
]
```

> **Примечание:** Если slice пуст — используется fallback: `[ [ defaultSize, 1 ] ]`





#### 5.4.4. Анимация (animation)

Анимация описывает вертикальный спрайт-лист (каждый кадр — по высоте блока).

```json
"animation": {
  "frametime": "<int>",             // длительность кадра по умолчанию
  "frames": [ ... ],              // список кадров
  "framePingPong": "<boolean>"      // если frames нет
}
```

### Формат frames

Кадры могут быть:

#### ✔ 1) Число
```json
"frames": [0, 1, 2, 3]
```

Каждый кадр имеет:
- `index = value`
- `time = frametime`

#### ✔ 2) Объект
```json
"frames": [
  { "index": 0, "time": 2 },
  { "index": 3, "time": 10 }
]
```

### framePingPong

Если нет frames:
```json
"framePingPong": true
```

Создаёт последовательность: `0,1,2,3,2,1`

> Время — `frametime`.

#### 5.4.5. Полный пример JSON

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

### 5.5 TooltipTransform — анимации

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

#### Автоматическое дополнение:
Если последний keyframe не имеет `progress: 100`, автоматически добавляется:
```json
{ "progress": 100, "translateX": 0, "translateY": 0, "scale": 1, "rotate": 0 }
```

> **Важно:** Если transform без keyframes → считается НЕ animated → игнорируется компонентами.

#### Пример пульсирующего tooltip

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

## 6. Выравнивание и размеры

### Выравнивание:

#### Inline (горизонтальное)
```json
"alignInline": "left|center|right"
// или алиас
"align": {"inline": "center"}
```

#### Block (вертикальное)
```json
"alignBlock": "top|center|bottom"
// или алиас
"align": {"block": "center"}
```

### Минимальные размеры:
```json
{
  "minWidth": 80,
  "minHeight": 20
}
```

## 7. Enrichers

Список дефолтных секций:
1. title
2. itemTitle
3. stackSize
4. hotkeys
5. itemInfo
6. tooltip
7. modInfo
8. navigation

### 7.1. title

Применяется к первой строке тултипа, если тултип не принадлежит предмету. Можно использовать все стили, которые принимает `SectionBox`.

```json
{
    "defaultColor": "0xFFFFFFFF"
}
```

### 7.2. itemTitle

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`, а также пару собственных.

```json
{
    "titleColor": "0xffffffff",             // displayName
    "subtitleColor": "0xff555555",          // displaySubtitle
    "identifierColor": "0xff555555"         // item id
}
```

Вызывает событие `ItemTitleEnricherEvent`, через которое другие моды могут редактировать `displayName` и `displaySubtitle`.

### 7.3. stackSize

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`. Отображается при зажатом `Shift`.

Вызывает событие `StackSizeEnricherEvent`, через которое другие моды могут редактировать `stackSize` и `fluid`, чтобы обрабатывать её как жидкость.

**Настройки:**
- `stackSizeEnricherEnabled` — позволяет выключить эту секцию
- `playerInventoryStackSizeEnabled` — при наведении на предмет в инвентаре игрока будет показывать не только количество под курсором, но и общее количество предметов этого типа в инвентаре игрока

### 7.4. hotkeys

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`.

Вызывает событие `HotkeyEnricherEvent`, через которое другие моды могут добавить свои hotkeys для отображения. Сам тултип не имеет базовых hotkeys. Отображает список при зажатом `Alt`.

**Настройки:**
- `hotkeysEnricherEnabled` — позволяет выключить эту секцию
- `hotkeysHelpTextEnabled` — позволяет выключить подсказку о необходимости зажать `Alt`


### 7.5. itemInfo

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`. Отображает тултип предмета.

Вызывает событие `ItemInfoEnricherEvent`, через которое другие моды могут добавить свои строки в эту секцию.

### 7.6. tooltip

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`. Отображает строки, которые были переданы тултипу.

### 7.7. modInfo

Применяется к тултипам, которые принадлежат предмету. Можно использовать все стили, которые принимает `SectionBox`.


### 7.8. navigation

Отрисовывается у тултипов, когда содержимое тултипа не помещается на экран.


### 7.9. Расширение другими модами

Создание обработчика, который будет обогащать тултип своими секциями:

```java
TooltipHandler.instance().addEnricher(String id, ITooltipEnricher enricher)
```

### 7.10. Вызов тултипа в другом моде

```java
TooltipHandler.instance().drawHoveringText(ItemStack stack, int x, int y)
TooltipHandler.instance().drawHoveringText(List<?> textLines, int x, int y)
TooltipHandler.instance().drawHoveringText(ItemStack stack, List<?> textLines, int x, int y)
TooltipHandler.instance().drawHoveringText(String context, ItemStack stack, List<?> textLines, int x, int y)
```