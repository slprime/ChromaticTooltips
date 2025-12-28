package com.slprime.chromatictooltips.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;

/**
 * @formatter:off
 *
 * parts:
 * modname:itemid          - identify: matches any part of the target, so minecraft:lava matches minecraft:lava_bucket
 * $orename                - ore dictionary: matches any part of the target, so $ingot matches ingotIron, ingotGold, etc.
 * tag.color=red           - tag
 * rarity:common           - rarity
 * 0 or 0-12               - damage
 *
 * modifiers:
 * ! - logical not. exclude items that match the following expression (!minecraft:portal)
 * r/.../ - standard java regex (r/^m\w{6}ft$/ = minecraft)
 * , - logical or in token (minecraft:potion 16384-16462,!16386)
 * | - logical or multi-item search (wrench|hammer)
 *
 *
 * example: minecraft:potion 16384-16462,!16386 | $oreiron | tag.color=red
 */
public class ItemStackFilterParser {

    protected static final Map<String, Function<String, Predicate<ItemStack>>> customFilters = new HashMap<>();

    private ItemStackFilterParser() {}

    public static Predicate<ItemStack> parse(String filterText) {
        Predicate<ItemStack> filter = stack -> false;
        boolean hasFilters = false;
        filterText = filterText.trim();

        if (!filterText.isEmpty()) {
            for (String part : filterText.split("\\s*\\|\\s*")) {
                Predicate<ItemStack> ruleFilter = parsePart(part);
                if (ruleFilter != null) {
                    filter = filter.or(ruleFilter);
                    hasFilters = true;
                }
            }
        }

        return hasFilters ? filter : null;
    }

    public static void registerCustomFilter(String name, Function<String, Predicate<ItemStack>> filterFunction) {
        ItemStackFilterParser.customFilters.put(name, filterFunction);
    }

    private static Predicate<ItemStack> parsePart(String part) {
        Predicate<ItemStack> partFilter = stack -> true;
        boolean hasFilters = false;

        for (String token : part.split("\\s+")) {
            final Predicate<ItemStack> ruleFilter = parseRules(token);

            if (ruleFilter != null) {
                partFilter = partFilter.and(ruleFilter);
                hasFilters = true;
            }
        }

        return hasFilters ? partFilter : null;
    }

    protected static Predicate<ItemStack> parseRules(String token) {
        Predicate<ItemStack> orFilter = stack -> false;
        Predicate<ItemStack> orNotFilter = stack -> false;
        Predicate<ItemStack> ruleFilter = stack -> true;
        boolean hasFilters = false;
        boolean hasNotFilters = false;

        for (String rule : token.split(",")) {
            boolean ignore = rule.startsWith("!");
            Predicate<ItemStack> filter = null;

            if (ignore) {
                rule = rule.substring(1);
            }

            if (rule.startsWith("$")) {
                filter = getOreDictFilter(rule.substring(1));
            } else if (rule.startsWith("tag.")) {
                filter = getTagFilter(rule.substring(4));
            } else if (rule.startsWith("rarity:")) {
                filter = getRarityFilter(rule.substring(7));
            } else if (Pattern.matches("^\\d+(-\\d+)?$", rule)) {
                filter = getDamageFilter(rule);
            } else {

                if (rule.contains(":")) {
                    final String[] parts = rule.split(":", 2);
                    final Function<String, Predicate<ItemStack>> customFilter = ItemStackFilterParser.customFilters.get(parts[0]);

                    if (customFilter != null) {
                        filter = customFilter.apply(parts[1]);
                    }
                }

                if (filter == null) {
                    filter = getStringIdentifierFilter(rule);
                }
            }

            if (filter == null) {
                continue;
            }

            if (ignore) {
                orNotFilter = orNotFilter.or(filter);
                hasNotFilters = true;
            } else {
                orFilter = orFilter.or(filter);
                hasFilters = true;
            }
        }

        if (hasFilters) {
            ruleFilter = ruleFilter.and(orFilter);
        }

        if (hasNotFilters) {
            ruleFilter = ruleFilter.and(orNotFilter.negate());
        }

        return hasFilters || hasNotFilters ? ruleFilter : null;
    }

    protected static Predicate<String> getMatcher(String searchText) {

        if (searchText.length() >= 3 && searchText.startsWith("r/") && searchText.endsWith("/")) {

            try {
                Pattern pattern = Pattern.compile(
                        searchText.substring(2, searchText.length() - 1),
                        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                return value -> pattern.matcher(value).find();
            } catch (PatternSyntaxException ignored) {}

        } else if (!searchText.isEmpty()) {
            final String lowerCase = searchText.toLowerCase();
            return value -> value.toLowerCase().contains(lowerCase);
        }

        return null;
    }

    protected static Predicate<ItemStack> getOreDictFilter(String rule) {
        final Predicate<String> matcher = getMatcher(rule);

        if (matcher == null) {
            return null;
        }

        return stack -> IntStream.of(OreDictionary.getOreIDs(stack))
                .anyMatch(id -> matcher.test(OreDictionary.getOreName(id)));
    }

    protected static Predicate<ItemStack> getTagFilter(String rule) {
        final String[] parts = rule.split("=", 2);
        final String[] path = parts[0].split("\\.");
        final Predicate<String> value = getMatcher(parts[1]);

        return stack -> {
            Object tag = stack.getTagCompound();

            for (int i = 0; i < path.length && tag != null; i++) {
                if (tag instanceof NBTTagCompound compound) {
                    tag = compound.getTag(path[i]);
                } else if (tag instanceof NBTTagList list) {
                    tag = list.tagList.get(Integer.parseInt(path[i]));
                } else {
                    tag = null;
                }
            }

            return tag == null ? value == null : value != null && value.test(tag.toString());
        };
    }

    protected static Predicate<ItemStack> getRarityFilter(String rule) {
        final Predicate<String> matcher = getMatcher(rule);

        return stack -> matcher != null && matcher.test(stack.getRarity().rarityName);
    }

    protected static Predicate<ItemStack> getDamageFilter(String rule) {
        final String[] range = rule.split("-");
        final IntPredicate matcher;

        if (range.length == 1) {
            final int damage = Integer.parseInt(range[0]);
            matcher = dmg -> dmg == damage;
        } else {
            final int damageStart = Integer.parseInt(range[0]);
            final int damageEnd = Integer.parseInt(range[1]);
            matcher = dmg -> dmg >= damageStart && dmg <= damageEnd;
        }

        return stack -> matcher.test(stack.getItemDamage());
    }

    protected static Predicate<ItemStack> getStringIdentifierFilter(String rule) {
        final FMLControlledNamespacedRegistry<Item> iItemRegistry = GameData.getItemRegistry();
        final Predicate<String> matcher = getMatcher(rule);

        if (matcher == null) {
            return null;
        }

        return stack -> {
            String name = iItemRegistry.getNameForObject(stack.getItem());
            return name != null && !name.isEmpty() && matcher.test(name);
        };
    }
}
