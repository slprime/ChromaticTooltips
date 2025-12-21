package com.slprime.chromatictooltips.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;

import com.slprime.chromatictooltips.ChromaticTooltips;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class BlacklistLines {

    protected static final String CONFIG_FILE = "chromatictooltips.blacklist";
    protected static Predicate<String> blacklistPredicate = s -> false;

    public static void loadBlacklist() {
        BlacklistLines.blacklistPredicate = s -> false;
        loadBacklistFromConfig();
        loadBacklistFromResourcepack();
    }

    protected static void loadBacklistFromConfig() {
        final File resource = new File((File) FMLInjectionData.data()[6], "config/" + CONFIG_FILE);

        if (!resource.exists()) {
            final URL defaultResource = BlacklistLines.class.getResource("/assets/chromatictooltips/" + CONFIG_FILE);

            try (FileWriter writer = new FileWriter(resource)) {
                IOUtils.copy(defaultResource.openStream(), writer);
            } catch (IOException e) {
                ChromaticTooltips.LOG.error("Failed to save default '{}' to file {}", CONFIG_FILE, resource, e);
            }
        }

        try (FileReader reader = new FileReader(resource)) {
            IOUtils.readLines(reader)
                .stream()
                .forEach(BlacklistLines::addRule);
        } catch (IOException e) {
            ChromaticTooltips.LOG.error("Failed to load '{}' file {}", CONFIG_FILE, resource, e);
        }

    }

    protected static void loadBacklistFromResourcepack() {
        final ResourceLocation location = new ResourceLocation(ChromaticTooltips.MODID, CONFIG_FILE);

        try {
            final IResource res = ClientUtil.mc()
                .getResourceManager()
                .getResource(location);

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                IOUtils.readLines(reader)
                    .stream()
                    .forEach(BlacklistLines::addRule);
            }

        } catch (Exception io) {
            ChromaticTooltips.LOG.error("Failed to load '{}' resourcepack {}", CONFIG_FILE, location);
        }

    }

    protected static void addRule(String rule) {
        rule = rule.contains("#") ? rule.substring(0, rule.indexOf('#')) : rule;
        final Predicate<String> matcher = getMatcher(rule.trim());

        if (matcher != null) {
            BlacklistLines.blacklistPredicate = BlacklistLines.blacklistPredicate.or(matcher);
        }
    }

    protected static Predicate<String> getMatcher(String searchText) {

        if (searchText.length() >= 3 && searchText.startsWith("r/") && searchText.endsWith("/")) {

            try {
                Pattern pattern = Pattern.compile(
                    searchText.substring(2, searchText.length() - 1),
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                return value -> pattern.matcher(value)
                    .find();
            } catch (PatternSyntaxException ignored) {}

        } else if (!searchText.isEmpty()) {
            final String lowerCase = searchText.toLowerCase();
            return value -> value.toLowerCase()
                .contains(lowerCase);
        }

        return null;
    }

    public static boolean test(String line) {
        return BlacklistLines.blacklistPredicate.test(line);
    }

}
